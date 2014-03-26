/**
 * ************************************************************
 * ShareSDKStatistics
 * An open source analytics android sdk for mobile applications
 * ************************************************************
 * 
 * @package		ShareSDK Statistics
 * @author		ShareSDK Limited Liability Company
 * @copyright	Copyright 2014-2016, ShareSDK Limited Liability Company
 * @since		Version 1.0
 * @filesource  https://github.com/OSShareSDK/OpenStatistics/tree/master/Android
 *  
 * *****************************************************
 * This project is available under the following license
 * *****************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package cn.sharesdk.open.statistics;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;
import cn.sharesdk.open.statistics.model.PostEvent;
import cn.sharesdk.open.statistics.model.PostResult;
import cn.sharesdk.open.statistics.net.NetworkHelper;
import cn.sharesdk.open.statistics.net.UpdateManager;
import cn.sharesdk.open.statistics.util.CrashHandler;
import cn.sharesdk.open.statistics.util.DBHelper;
import cn.sharesdk.open.statistics.util.DeviceHelper;
import cn.sharesdk.open.statistics.util.Ln;

public class EventManager {

	private final static String PLATFORM_ID = "1";// android:1,ios:2
	private final static String SDK_VERSION = "1.0";// android:1,ios:2

	private static Context context;
	private static String start_date = null;// The start time point
	private static long start = 0;
	private static String end_date = null;// The end time point
	private static long end = 0;//
	private static String duration = null;// run time

	private static String session_id = null;
	private static String activities = null;// currnet activity's name
	private static String appkey = "";
	private static String channel = "";

	// session continue millis
	private static long sessionContinueMillis = 30000L;

	private static boolean isFirst = true;
	private static boolean isPageStart = false;
	private static boolean setBaseURL = false;
	private static boolean wifiReportPolicy = false;

	private static DBHelper dbHelper;
	private static DeviceHelper deviceHelper;
	private static HashMap<String, Long> eventDurationMap = new HashMap<String, Long>();
	private static HashMap<String, String> eventLabelMap = new HashMap<String, String>();

	// the url of post data to server
	private static String preUrl = "http://192.168.1.195/statistics.sharesdk.cn/api/index.php";
	private static final String uploadUrl = "/client/send/setdata";
	private static final String onlineConfigUrl = "/client/config/getpolicy";

	public static void init(Context c) {
		if (c != null && context == null) {
			context = c.getApplicationContext();
			dbHelper = DBHelper.getInstance(context);
			deviceHelper = DeviceHelper.getInstance(context);
			// create session inder to solve the problem of set errorListener
//			if (TextUtils.isEmpty(session_id)) {
//				generateSeesion();
//			}
		} else if (c == null){
			Ln.e("Context is null", "call setContext to set it");
		}
	}
	
	public static void setBaseURL(String url){
		preUrl = url;
		setBaseURL = true;
	}
	
	public static void setAppKey(String appkey){
		if(!TextUtils.isEmpty(appkey)){
			EventManager.appkey = appkey;
		}
	}
	
	private static String getAppKey(){
		if(TextUtils.isEmpty(appkey)){
			appkey = deviceHelper.getAppKey();
		}
		return appkey;
	}

	public static void setChannel(String channel){
		if(!TextUtils.isEmpty(channel)){
			EventManager.channel = channel;
		}
	}
	
	private static String getChannel(){
		if(TextUtils.isEmpty(channel)){
			channel = deviceHelper.getChannel();
		}
		return channel;
	}

	public static void setSessionContinueMillis(long interval){
		sessionContinueMillis = interval;
	}

	/** get upload-log url */
	private static String getUploadLogUrl() {
		String apiPath = dbHelper.getReportApiPath();
		if (!setBaseURL && !TextUtils.isEmpty(apiPath)) {
			preUrl = apiPath;
		}
		return preUrl + uploadUrl;
	}

	/** get online-config url */
	private static String getOnlineConfigUrl() {
		String apiPath = dbHelper.getReportApiPath();
		if (!setBaseURL && !TextUtils.isEmpty(apiPath)) {
			preUrl = apiPath;
		}
		return preUrl + onlineConfigUrl;
	}

	/** get isupdate url */
	private static String getUpdateUrl() {
		String apiPath = dbHelper.getReportApiPath();
		if (!setBaseURL && !TextUtils.isEmpty(apiPath)) {
			preUrl = apiPath;
		}
		return preUrl + onlineConfigUrl;
	}

	/** post errors' log */
	public static void onError(String error) {
		dbHelper.saveInfoToFile(dbHelper.ERROR_DATA, getErrorJSONObject(error));
		uploadLog();
	}
	
	/** set error listener */
	public static void onError() {
		CrashHandler handler = CrashHandler.getInstance();
		handler.init(context);
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}

	public static void onEventBegin(Context context, String event_id) {
		eventDurationMap.put(event_id, System.currentTimeMillis());
	}
	
	public static void onEventBegin(Context context, String event_id, String label) {
		eventLabelMap.put(event_id, label);
		eventDurationMap.put(event_id, System.currentTimeMillis());
	}

	public static long onEventEnd(Context context, String event_id) {
		long start = eventDurationMap.remove(event_id);
		if (start == 0) {
			Ln.e("error : onEventEnd ===>>> ", "do not call onEventBegin, duration is 0");
			return 0;
		}
		long duration = System.currentTimeMillis() - start;
		return duration;

	}
	
	public static long onEventEnd(Context context, String event_id, String label) {
		String mLabel = eventLabelMap.remove(event_id);
		if (!mLabel.equals(label)) {
			Ln.e("error : onEventEnd ===>>> ", "do not call onEventBegin or label is not equal");
			return 0;
		}
		long start = eventDurationMap.remove(event_id);
		if (start == 0) {
			Ln.e("error : onEventEnd ===>>> ", "do not call onEventBegin, duration is 0");
			return 0;
		}
		long duration = System.currentTimeMillis() - start;
		return duration;

	}

	/** set the wifi report policy */
	public static void setWifiReportPolicy(boolean isUpdateonlyWifi) {
		wifiReportPolicy = isUpdateonlyWifi;
		dbHelper.setReportPolicy(dbHelper.WIFI_SEND_POLICY, 0);
	}

	public static void onPageStart(String activityName) {
		isPageStart = true;
		activities = activityName;
	}

	public static void onPageEnd(String activityName) {
		isPageStart = false;
		activities = activityName;
	}

	/** Dose update the application or not , the method is kept to use in the future
	 * install app need this permission - <android.permission.INSTALL_PACKAGES/>
	 * */
	public static void isUpdate() {
		
		JSONObject updateObject = new JSONObject();

		try {
			updateObject.put("appkey", getAppKey());
			updateObject.put("version_code", deviceHelper.getCurVersion());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (deviceHelper.isNetworkAvailable() && deviceHelper.isNetworkTypeWifi()) {
			PostResult post = NetworkHelper.post(getUpdateUrl(), updateObject.toString(), getAppKey());
			if (post!=null && post.isSuccess()) {
				try {
					JSONObject object = new JSONObject(post.getResponseMsg());
					String flag = object.getString("flag");
					if (Integer.parseInt(flag) > 0) {
						String fileurl = object.getString("fileurl");
						// String msg = object.getString("msg");
						String forceupdate = object.getString("forceupdate");
						String description = object.getString("description");
						// String time = object.getString("time");
						String version = object.getString("version");
						UpdateManager manager = new UpdateManager(context, version, forceupdate, fileurl, description);
						manager.showNoticeDialog(context);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Ln.e("update software error", post.getResponseMsg());

			}
		}
	}

	private static void isCreateNewSessionID() {
		try {
			if (session_id == null) {
				generateSeesion();
				return;
			}

			long currenttime = System.currentTimeMillis();
			long session_save_time = dbHelper.getSessionTime();

			if (currenttime - session_save_time > sessionContinueMillis) {
				generateSeesion();
			}else{
				Ln.i("MobclickAgent: ", "Extend current session :"+session_id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * create sessionID
	 */
	public static String generateSeesion() {
		String sessionId = "";
		String str = getAppKey();
		if (str != null) {
			str = str + deviceHelper.getTime() + deviceHelper.getDeviceID();
			sessionId = deviceHelper.md5(str);
			dbHelper.setSessionID(sessionId);
			dbHelper.setSessionTime();
			session_id = sessionId;
			Ln.i("MobclickAgent: ", "Start new session :"+session_id);
			
			// post device and launch data
			dbHelper.saveInfoToFile(dbHelper.LAUNCH_DATA, getLaunchJSONObject());
			uploadLog();

			return sessionId;
		} else {
			Ln.e("MobclickAgent", "protocol Header need Appkey or Device ID ,Please check AndroidManifest.xml ");
		}
		return sessionId;
	}

	/**
	 * post app's pause log
	 */
	public static void onPause() {
		isPageStart = false;
		dbHelper.setSessionTime();

		end_date = deviceHelper.getTime();
		end = Long.valueOf(System.currentTimeMillis());
		duration = end - start + "";

		dbHelper.saveInfoToFile(dbHelper.PAGE_DATA, getPauseJSONObject());
		uploadLog();
	}
	
	/**
	 * post app's resume log
	 */
	public static void onResume() {

		isCreateNewSessionID();
		if (!isPageStart)
			activities = deviceHelper.getActivityName();

		start_date = deviceHelper.getTime();
		start = Long.valueOf(System.currentTimeMillis());

	}

	/** upload all log */
	public static void uploadLog() {

		// the app first launch
		if (isFirst) {
			uploadAllLog();
			isFirst = false;
			return;
		}
		
		//set wifi report policy, the policy of server is first
		int reportPolicy = dbHelper.WIFI_SEND_POLICY;
		if(!wifiReportPolicy){
			reportPolicy = dbHelper.getReportPolicy();
			Ln.i(" upload all log reportPolicy ===>>>", reportPolicy+"");
		}
		 
		int delayMillis = dbHelper.getReportDelay();
		if (reportPolicy == dbHelper.WIFI_SEND_POLICY && deviceHelper.isWiFiActive()) {
			Ln.i("wifi upload all log ===>>>", "wifi");
			uploadAllLog();
		} else if (reportPolicy == dbHelper.DELAY_SEND_POLICY) {
			long currentTiem = System.currentTimeMillis();
			long lastTime = dbHelper.getLastReportTime();
			if (currentTiem - lastTime > delayMillis) {
				uploadAllLog();
				dbHelper.setLastReportTime(currentTiem);
			}
			// if not add below code, the delay post log will not action
			LoggerThread.getInstance().uploadLogDelay(context, delayMillis);
		}
	}

	/** post launch data to server */
	public static void postLaunchDatas() {
		dbHelper.saveInfoToFile(dbHelper.LAUNCH_DATA, getLaunchJSONObject());
		uploadLog();
	}

	/** post event info */
	public static void onEvent(PostEvent event) {
		//TODO 
//		if (!event.verification()) {
//			Log.w("MobclickAgent", "Illegal value of acc in postEventInfo");
//			return;
//		}
		if(event.getStringMap() != null){
			dbHelper.saveInfoToFile(dbHelper.HASH_EVENT_DATA, event.eventToJOSNObj());
		}else{
			dbHelper.saveInfoToFile(dbHelper.EVENT_DATA, event.eventToJOSNObj());
		}
		uploadLog();

	}
	
	/** post event info */
	public static void onEventDuration(PostEvent event) {
		
		if (event.getDuration() == 0) {
			Ln.e("MobclickAgent", "onEventDuration the duration is 0");
			return;
		}
		if(event.getStringMap() != null){
			dbHelper.saveInfoToFile(dbHelper.HASH_EVENT_DATA, event.eventToJOSNObj());
		}else{
			dbHelper.saveInfoToFile(dbHelper.EVENT_DATA, event.eventToJOSNObj());
		}
		uploadLog();

	}

	/**
	 * update the config from the server
	 */
	public static void updateOnlineConfig() {

		if (deviceHelper.isNetworkAvailable()) {
			PostResult post = NetworkHelper.post(getOnlineConfigUrl(), null, getAppKey());

			if (post!=null && post.isSuccess()) {
				parseResponseData(post.getResponseMsg());
			} else {
				Ln.e("error", post.getResponseMsg());
			}
		} else {
			Ln.e("updateOnlineConfigs error ==>>", "network error or appkey is null");
		}

	}

	/**
	 * upload all events' log
	 */
	private static boolean uploadAllLog() {
	
		String content = dbHelper.GetInfoFromFile();
		if (!TextUtils.isEmpty(content) && deviceHelper.isNetworkAvailable()) {
			try {
				JSONObject object = new JSONObject(content);
				object.put(dbHelper.DEVICE_DATA, getDeviceJSONObject());				
				content = object.toString();
				
				PostResult post = NetworkHelper.post(getUploadLogUrl(), content, getAppKey());
				if (post!=null && post.isSuccess()) {
					boolean success = parseResponseData(post.getResponseMsg());
					if (success) {
						dbHelper.deleteCacheFile();
					}
					return success;
				}else {
					Ln.e("error", post.getResponseMsg());
				}
			} catch (Exception e) {
				Ln.e("uploadLog", "Exception occurred in postEventInfo()");
			}
		}
		return false;
	}

	/**
	 * parse the response from server
	 * 
	 * @param jsonMsg
	 */
	private static boolean parseResponseData(String jsonMsg) {
		//To solve the coding problems. eg. utf-8
		if(jsonMsg.startsWith("\ufeff")) {
			Ln.w(" parseResponseData jsonMsg.startsWith(\\ufeff) == >>", "jsonMsg error");
			jsonMsg = jsonMsg.substring(1);
		}
		
		try {
			JSONObject object = new JSONObject(jsonMsg);
			int status = Integer.parseInt(object.getString("status"));
			if (status == 200){
				if(Ln.DebugMode){
					Toast.makeText(context, "Send msg successfully!", 500).show();
				}
				object = object.getJSONObject("res");
				if (object == null)
					return true;
	
				object = object.getJSONObject("config");
				if (object == null)
					return true;
	
				Ln.i("parseResponseData ", object.toString());
				String apiPath = object.getString("api_path");
				int policy = object.getInt("policy");
				int delay = object.getInt("duration")*1000;
				
				if(dbHelper.WIFI_SEND_POLICY != policy){
					wifiReportPolicy = false;
					Ln.i("reportPolicy from server ===>>>", policy+"");
				}
				
				preUrl = apiPath; 
				dbHelper.setReportApiPath(apiPath);
				dbHelper.setReportPolicy(policy, delay);
				return true;
			}else{
				if(Ln.DebugMode){
					Toast.makeText(context, "Fail to send msg!", 500).show();
				}
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Ln.e("parseResponseData", "Exception occurred in postEventInfo()");
			if(Ln.DebugMode){
				Toast.makeText(context, "Fail to send msg!", 500).show();
			}
			return false;
		}
	}

	/** get json of launch msg */
	private static JSONObject getLaunchJSONObject() {
		JSONObject launchData = new JSONObject();
		try {
			String create_date = deviceHelper.getTime(dbHelper.getSessionTime());
			launchData.put("create_date", create_date);
			launchData.put("session_id", session_id);

			Ln.i("launchData---------->", launchData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return launchData;
	}

	/** get json of error msg */
	private static JSONObject getErrorJSONObject(String error) {
		JSONObject errorData = new JSONObject();
		try {
			String headstring = "";
			if (error.contains("Caused by:")) {
				String ssString = error.substring(error.indexOf("Caused by:"));
				String[] ss = ssString.split("\n\t");
				if (ss.length >= 1)
					headstring = ss[0];
			}

			errorData.put("session_id", session_id);
			errorData.put("create_date", deviceHelper.getTime());
			errorData.put("page", deviceHelper.getActivityName());
			errorData.put("error_log", headstring);
			errorData.put("stack_trace", error);

			Ln.i("errorData---------->", errorData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return errorData;
	}

	/** get json of activity onPause msg */
	private static JSONObject getPauseJSONObject() {
		JSONObject pauseData = new JSONObject();
		try {
			pauseData.put("session_id", session_id);
			pauseData.put("start_date", start_date);
			pauseData.put("end_date", end_date);
			pauseData.put("page", activities);
			pauseData.put("duration", duration);

			Ln.i("pauseData---------->", pauseData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pauseData;
	}

	/** get json of device data msg */
	private static JSONObject getDeviceJSONObject() {

		JSONObject clientData = new JSONObject();
		try {
			Ln.e("device_id deviceHelper.getDeviceKey() ===>>>", deviceHelper.getDeviceKey());
			clientData.put("device_id", deviceHelper.getDeviceKey());
			clientData.put("appver", deviceHelper.getVersionName());
			clientData.put("apppkg", deviceHelper.getPackageName());
			clientData.put("platform_id", PLATFORM_ID);
			clientData.put("sdkver", SDK_VERSION);
			clientData.put("channel_name", getChannel());
			clientData.put("mac", deviceHelper.getMacAddress());
			clientData.put("model", deviceHelper.getModel());
			clientData.put("sysver", deviceHelper.getSysVersion());
			clientData.put("carrier", deviceHelper.getCarrier());
			clientData.put("screensize", deviceHelper.getScreenSize());
			clientData.put("factory", deviceHelper.getFactory());
			clientData.put("networktype", deviceHelper.getNetworkType());
			clientData.put("is_jailbroken", deviceHelper.isRooted());
			clientData.put("language", deviceHelper.getLanguage());
			clientData.put("timezone", deviceHelper.getTimeZone());
			clientData.put("cpu", deviceHelper.getCpuName());
			clientData.put("manuid", deviceHelper.getManuID());

			String manutime = deviceHelper.getTime(Long.parseLong(deviceHelper.getManuTime()));
			clientData.put("manutime", manutime);

			Ln.i("deviceData---------->", clientData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return clientData;
	}
}
