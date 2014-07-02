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
package cn.sharesdk.analysis;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import cn.sharesdk.analysis.db.MessageUtils;
import cn.sharesdk.analysis.model.AIDLCacheEvent;
import cn.sharesdk.analysis.model.EventType;
import cn.sharesdk.analysis.model.PostEvent;
import cn.sharesdk.analysis.server.RemoteService;
import cn.sharesdk.analysis.util.CrashHandler;
import cn.sharesdk.analysis.util.DeviceHelper;
import cn.sharesdk.analysis.util.Ln;
import cn.sharesdk.analysis.util.PreferencesHelper;
import cn.sharesdk.analysis.server.AIDLService;

public class EventManager {

	private static Context context;
	private static String start_date = null;// The start time point
	private static long start = 0;
	private static String end_date = null;// The end time point
	private static long end = 0;//
	private static String duration = null;// run time

	private static String session_id = null;
	private static String last_activity = "APP_START";// currnet activity's name
	private static String current_activity = null;// currnet activity's name
	private static String appkey = "";

	// session continue millis
	private static long sessionContinueMillis = 30000L;

	private static boolean activityTrack = true;

	private static PreferencesHelper dbHelper;
	private static DeviceHelper deviceHelper;
	//存储事件时长
	private static HashMap<String, Long> eventDurationMap = new HashMap<String, Long>();
	//匹配事件的label是否一致
	private static HashMap<String, String> eventLabelMap = new HashMap<String, String>();
	//存储页面的时长
	private static HashMap<String, Long> pageDurationMap = new HashMap<String, Long>();
	//存储一些操作事件
	//存储一些事件
	private static ArrayList<AIDLCacheEvent> settingEventList = new ArrayList<AIDLCacheEvent>();
	private static ArrayList<AIDLCacheEvent> cacheEventList = new ArrayList<AIDLCacheEvent>();

	private static AIDLService aidlService;
	
	public static synchronized void init(Context c) {
		if (context == null && c != null) {
			context = c.getApplicationContext();
			dbHelper = PreferencesHelper.getInstance(context);
			deviceHelper = DeviceHelper.getInstance(context);
			isServiceConnect(context);
			
		} else if (context == null && c == null){
			Ln.e("Context is null", "call setContext to set it");
		}
	}
	
	private static ServiceConnection connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			aidlService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			aidlService = AIDLService.Stub.asInterface(service);
			//TOTO setting base params after server connect
			try {
				
				for(AIDLCacheEvent cacheEvent : settingEventList){
					//the setting must be done at first
					if(EventType.SETTING == cacheEvent.eventType){
						aidlService.setting(cacheEvent.key, cacheEvent.value);
					}
				}
				
				for (AIDLCacheEvent cacheEvent : cacheEventList) {
					if(EventType.SAVELOG == cacheEvent.eventType){
						aidlService.saveLog(cacheEvent.key, cacheEvent.value);
					}else if(EventType.UPLOAD_LOG == cacheEvent.eventType){
						aidlService.uploadLog();
					}					
				}
				settingEventList.clear();
				cacheEventList.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	public static void openActivityDurationTrack(boolean activityTrack){
		EventManager.activityTrack = activityTrack;
	}
	
	public static void setBaseURL(String url){
		if(!TextUtils.isEmpty(url)){
			startSettingService(RemoteService.SET_PREURL, url);
		}
	}
	
	public static void setAppKey(String appkey){
		if(!TextUtils.isEmpty(appkey)){
			EventManager.appkey = appkey;
			startSettingService(RemoteService.SET_APP_KEY, appkey);
		}
	}
	
	public static void setDebugMode(boolean isdebug) {
		Ln.DebugMode = isdebug;
		startSettingService(RemoteService.SET_ISDEBUG, String.valueOf(isdebug));
	}
	
	public static String getAppKey(){
		if(TextUtils.isEmpty(appkey)){
			appkey = deviceHelper.getAppKey();
		}
		return appkey;
	}

	public static void setChannel(String channel){
		if(!TextUtils.isEmpty(channel)){
			startSettingService(RemoteService.SET_CHANNEL, channel);
		}
	}
	

	public static void setSessionContinueMillis(long interval){
		sessionContinueMillis = interval;
	}

	/** post errors' log */
	public static void onError(Context context, String error) {
		init(context);
		startLogService(MessageUtils.ERROR_DATA, getErrorJSONObject(error));
	}
	
	/** set error listener */
	public static void onError(Context context) {
		init(context);
		CrashHandler handler = CrashHandler.getInstance();
		handler.init(context);
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}

	public static void onEventBegin(Context context, String event_id) {
		init(context);
		eventDurationMap.put(event_id, System.currentTimeMillis());
	}
	
	public static void onEventBegin(Context context, String event_id, String label) {
		init(context);
		eventLabelMap.put(event_id, label);
		eventDurationMap.put(event_id, System.currentTimeMillis());
	}

	public static long onEventEnd(Context context, String event_id) {
		init(context);
		if(eventDurationMap.containsKey(event_id)){
			long start = eventDurationMap.remove(event_id);
			long duration = System.currentTimeMillis() - start;
			return duration;
		}else{
			Ln.e("error : onEventEnd ===>>> ", "do not call onEventBegin, duration is 0");
			return 0;
		}

	}
	
	public static long onEventEnd(Context context, String event_id, String label) {
		init(context);
		if(eventDurationMap.containsKey(event_id) && eventLabelMap.containsKey(event_id)){
			String mLabel = eventLabelMap.remove(event_id);
			if (!mLabel.equals(label)) {
				Ln.e("error : onEventEnd ===>>> ", "the param of label is not equal");
				return 0;
			}
			long start = eventDurationMap.remove(event_id);
			long duration = System.currentTimeMillis() - start;
			return duration;
		}else{
			Ln.e("error : onEventEnd ===>>> ", "do not call onEventBegin or label is not equal");
			return 0;
		}

	}
	
	public static void setAutoLocation(boolean isLocation) {
		startSettingService(RemoteService.SET_LOCATION, String.valueOf(isLocation));
	}
	
	public static void onPageStart(String pageName) {
		if (context == null){
			Ln.e("Context is null", "call onResume() to initsdk");
			return;
		}
		//TODO umeng 只统计页面（是否应该删除activityTrack）,需验证
		current_activity = pageName;
		pageDurationMap.put(pageName, System.currentTimeMillis());
		if(context != null){
			onResume(context, null, null);
		}
	}

	public static void onPageEnd(String pageName) {
		if (context == null){
			Ln.e("Context is null", "call onResume() to initsdk");
			return;
		}
		//TODO umeng 只统计页面（是否应该删除activityTrack）,需验证
		long pageStart = pageDurationMap.remove(pageName);
		if(pageStart == 0){
			Ln.e("error : onPageEnd ===>>> ", "do not call onPageStart or the param of pageName is not equal");
			return;
		}else{
			if(context != null){
				onPause(context);
				current_activity = null;
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
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * create sessionID
	 */
	private static String generateSeesion() {
		dbHelper.setAppStartDate();
		String sessionId = "";
		String str = getAppKey();
		if (str != null) {
			str = str + deviceHelper.getTime() + deviceHelper.getDeviceID();
			sessionId = deviceHelper.md5(str);
			dbHelper.setSessionTime();
			dbHelper.setSessionID(sessionId);
			session_id = sessionId;
			Ln.i("MobclickAgent: ", "Start new session :"+session_id);
			
			// post device and launch data
			startLogService(MessageUtils.LAUNCH_DATA, getLaunchJSONObject());

			return sessionId;
		} else {
			Ln.e("MobclickAgent", "protocol Header need Appkey or Device ID ,Please check AndroidManifest.xml ");
		}
		return sessionId;
	}

	/**
	 * post app's pause log
	 */
	public static void onPause(Context context) {
		init(context);
		//TODO umeng 自动统计activity页面时长（是否应该删除activityTrack）
		dbHelper.setSessionTime();

		end_date = deviceHelper.getTime();
		end = Long.valueOf(System.currentTimeMillis());
		duration = end - start + "";

		if(!TextUtils.isEmpty(current_activity)){
			startLogService(MessageUtils.PAGE_DATA, getPauseJSONObject());
		}
	}
	
	/**
	 * post app's resume log
	 */
	public static void onResume(Context context,String appkey, String channel) {
		init(context);
		if(!TextUtils.isEmpty(appkey)){
			setAppKey(appkey);
		}
		if(!TextUtils.isEmpty(channel)){
			setChannel(channel);
		}
		
		isCreateNewSessionID();
		if (activityTrack)
			current_activity = deviceHelper.getActivityName();

		start_date = deviceHelper.getTime();
		start = Long.valueOf(System.currentTimeMillis());

	}

	/** post launch data to server */
	public static void postLaunchDatas(Context context) {
		init(context);
		startLogService(MessageUtils.LAUNCH_DATA, getLaunchJSONObject());
	}

	/** post event info */
	public static void onEvent(Context context, PostEvent event) {
		init(context);
		//TODO 
//		if (!event.verification()) {
//			Log.w("MobclickAgent", "Illegal value of acc in postEventInfo");
//			return;
//		}
		if(event.getStringMap() != null){
			startLogService(MessageUtils.HASH_EVENT_DATA, event.eventToJOSNObj());
		}else{
			startLogService(MessageUtils.EVENT_DATA, event.eventToJOSNObj());
		}

	}
	
	/** post event info */
	public static void onEventDuration(Context context, PostEvent event) {
		init(context);
		if (event.getDuration() == 0) {
			Ln.e("onEventDuration", "onEventDuration the duration is 0");
			return;
		}
		if(event.getStringMap() != null){
			startLogService(MessageUtils.HASH_EVENT_DATA, event.eventToJOSNObj());
		}else{
			startLogService(MessageUtils.EVENT_DATA, event.eventToJOSNObj());
		}

	}

	private static void isServiceConnect(Context context) {
		Ln.e("isServiceConnect ==>>", "bindService");
		if(context != null){
			Intent service = new Intent(context, RemoteService.class);
			service.setAction("cn.sharesdk.analysis.server.AIDLService");
			context.startService(service);
			context.bindService(service, connection, Context.BIND_AUTO_CREATE);
		}
	}
	
	/** upload all log */
	public static void uploadLog(Context context) {
		init(context);
		try {
			if(aidlService != null) {
				aidlService.uploadLog();
			}else{
				cacheEventList.add(new AIDLCacheEvent(EventType.UPLOAD_LOG));
				isServiceConnect(context);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**Save and send log to server on the service*/
	public static void startLogService(String action, JSONObject jsonObject){	
		try {
			if(jsonObject == null){
				return;
			} 
			String json = jsonObject.toString();
			if(TextUtils.isEmpty(json)){
				return;
			}
			if (aidlService != null) {
				aidlService.saveLog(action, json);
			}else{
				cacheEventList.add(new AIDLCacheEvent(EventType.SAVELOG, action, json));
				isServiceConnect(context);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**Save and send log to server on the service*/
	private static void startSettingService(String action, String extraValue){		
		try {
			if(aidlService != null){
				aidlService.setting(action, extraValue);
			}else{
				settingEventList.add(new AIDLCacheEvent(EventType.SETTING, action, extraValue));
				isServiceConnect(context);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/** get json of launch msg */
	private static JSONObject getLaunchJSONObject() {
		JSONObject launchData = new JSONObject();
		try {
			launchData.put("create_date", dbHelper.getAppStartDate());
			launchData.put("last_end_date", dbHelper.getAppExitDate());
			launchData.put("session_id", session_id);

			//Ln.i("launchData---------->", launchData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return launchData;
	}

	/** get json of error msg */
	private static JSONObject getErrorJSONObject(String error) {
		if(TextUtils.isEmpty(error)){
			return null;
		}
		JSONObject errorData = new JSONObject();
		try {
			String headstring = error;
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

			//Ln.i("errorData---------->", errorData.toString());
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
			pauseData.put("page", current_activity);
			pauseData.put("from_page", last_activity);
			pauseData.put("duration", duration);
			//save the last activity name
			last_activity = current_activity;
			//Ln.i("pauseData---------->", pauseData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pauseData;
	}

}
