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
package cn.sharesdk.analysis.server;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import cn.sharesdk.analysis.db.MessageModel;
import cn.sharesdk.analysis.db.MessageUtils;
import cn.sharesdk.analysis.model.PostResult;
import cn.sharesdk.analysis.net.NetworkHelper;
import cn.sharesdk.analysis.util.DeviceHelper;
import cn.sharesdk.analysis.util.Ln;
import cn.sharesdk.analysis.util.PreferencesHelper;

public class ServiceHelper extends HandlerThread implements Callback {

	private final static int UPLOAD_LOG = 5;
	private final static int SAVE_SEND_LOG = 7;
	private final static int EXIT_APP = 10;

	private final static String PLATFORM_ID = "1";// android:1,ios:2
	private final static String SDK_VERSION = "1.0";// android:1,ios:2
	private final static String uploadUrl = "";

	private int appExitCount = 0;
	private String appkey = "";
	private String channel = "";
	private String preUrl = "http://192.168.1.195/statistics.sharesdk.cn/api/index.php";

	private boolean appBackRunning = false;
	private boolean autoLocation = false;
	private boolean setBaseURL = false;

	private Context context;
	private Handler handler;
	private PreferencesHelper preference;
	private DeviceHelper deviceHelper;
	private static ServiceHelper serviceHelper;

	private ServiceHelper(RemoteService service) {
		super("ShareSDK Statistics Service");
		start();
		handler = new Handler(this.getLooper(), this);
		this.context = service.getApplicationContext();
		preference = PreferencesHelper.getInstance(context);
		deviceHelper = DeviceHelper.getInstance(context);
		sendIsAppExitMsg();
	}

	public static ServiceHelper getInstance(RemoteService service) {
		if (serviceHelper == null) {
			serviceHelper = new ServiceHelper(service);
		}
		return serviceHelper;
	}

	// upload log
	public void sendUploadLogMsg() {
		Message msg = new Message();
		msg.what = UPLOAD_LOG;
		handler.sendMessage(msg);
	}

	// send log to server
	public void saveAndSendLogMsg(Bundle bundle) {
		Message msg = new Message();
		msg.what = SAVE_SEND_LOG;
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	public synchronized void sendIsAppExitMsg() {
		Message msg = new Message();
		msg.what = EXIT_APP;
		handler.sendMessageDelayed(msg, 1000);
	}

	public void setAutoLocation(boolean autoLocation) {
		this.autoLocation = autoLocation;
	}

	public void setAppKey(String appkey) {
		if (!TextUtils.isEmpty(appkey)) {
			this.appkey = appkey;
		}
	}

	public String getAppKey() {
		if (TextUtils.isEmpty(appkey)) {
			appkey = deviceHelper.getAppKey();
		}
		return appkey;
	}

	public void setChannel(String channel) {
		if (!TextUtils.isEmpty(channel)) {
			this.channel = channel;
		}
	}

	private String getChannel() {
		if (TextUtils.isEmpty(channel)) {
			channel = deviceHelper.getChannel();
		}
		return channel;
	}

	public void setBaseURL(String url) {
		if (!TextUtils.isEmpty(url)) {
			preUrl = url;
			setBaseURL = true;
		}
	}

	/** get upload-log url */
	private String getUploadLogUrl() {
		if (!setBaseURL) {
			String apiPath = preference.getReportApiPath();
			if (!TextUtils.isEmpty(apiPath)) {
				preUrl = apiPath;
			}
		}
		return preUrl + uploadUrl;
	}

	/**
	 * Getting latitude of location
	 * 
	 * @return
	 */
	private String getLatitude() {
		String latitude = "";
		if (autoLocation) {
			latitude = deviceHelper.getLatitude();
		}
		return latitude;
	}

	/**
	 * Getting longtitude of location
	 * 
	 * @return
	 */
	private String getLongitude() {
		String longtitude = "";
		if (autoLocation) {
			longtitude = deviceHelper.getLongitude();
		}
		return longtitude;
	}

	/**
	 * get event data from db,and send to server
	 */
	private void uploadAllLog() {
		ArrayList<MessageModel> msgList = MessageUtils.getEventMsg(context);
		for (MessageModel model : msgList) {
			uploadLog(model);
		}
	}

	/** upload all log to server */
	private boolean uploadLog(MessageModel eventData) {
		String content = eventData.data;
		if (!TextUtils.isEmpty(content) && deviceHelper.isNetworkAvailable()) {
			try {
				JSONObject object = new JSONObject(content);
				object.put(MessageUtils.DEVICE_DATA, getDeviceJSONObject());
				content = object.toString();
				if (Ln.DebugMode) {
					Toast.makeText(context, "Server address : " + getUploadLogUrl(), 1000).show();
				}
				Ln.i("server address ==>>>", getUploadLogUrl());
				PostResult post = NetworkHelper.post(getUploadLogUrl(), content, getAppKey());
				if (post != null && post.isSuccess()) {
					boolean success = parseResponseData(post.getResponseMsg());
					if (success) {
						if (Ln.DebugMode) {
							Toast.makeText(context, "Send msg successfully!", 1000).show();
						}
						MessageUtils.deleteManyMsg(context, eventData.idList);
					} else {
						if (Ln.DebugMode) {
							Toast.makeText(context, "Fail to send msg !", 1000).show();
						}
					}
					return success;
				} else {
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
	private boolean parseResponseData(String jsonMsg) {
		// To solve the coding problems. eg. utf-8
		if (jsonMsg.startsWith("\ufeff")) {
			Ln.w(" parseResponseData jsonMsg.startsWith(\\ufeff) == >>", "jsonMsg error");
			jsonMsg = jsonMsg.substring(1);
		}

		try {
			JSONObject object = new JSONObject(jsonMsg);
			int status = Integer.parseInt(object.getString("status"));
			if (status == 200) {
				if (object.isNull("res")) {
					return true;
				} else {
					object = object.getJSONObject("res");
				}

				if (object.isNull("config")) {
					return true;
				} else {
					object = object.getJSONObject("config");
				}

				if (object.isNull("api_path")) {
					return true;
				}

				String apiPath = object.getString("api_path");
				if (!TextUtils.isEmpty(apiPath)) {
					preUrl = apiPath;
					preference.setReportApiPath(apiPath);
				}

				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Ln.e("parseResponseData", "Exception occurred in postEventInfo()");
			return false;
		}
	}

	/** get json of device data msg */
	private JSONObject getDeviceJSONObject() {

		JSONObject clientData = new JSONObject();
		try {
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
			clientData.put("is_pirate", 0);
			clientData.put("is_jailbroken", deviceHelper.isRooted() ? 1 : 0);
			clientData.put("longitude", getLongitude());
			clientData.put("latitude", getLatitude());
			clientData.put("language", deviceHelper.getLanguage());
			clientData.put("timezone", deviceHelper.getTimeZone());
			clientData.put("cpu", deviceHelper.getCpuName());
			clientData.put("manuid", deviceHelper.getManuID());

			String manutime = deviceHelper.getTime(Long.parseLong(deviceHelper.getManuTime()));
			clientData.put("manutime", manutime);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return clientData;
	}

	public boolean isAppExit() {
		if (context == null) {
			Ln.e("getActivityName", "context is null that do not get the package's name ");
			return true;
		}
		String packageName = context.getPackageName();
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (deviceHelper.checkPermissions("android.permission.GET_TASKS")) {
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			boolean isEqual = packageName.equals(cn.getPackageName());
			return !isEqual;
		} else {
			Ln.e("lost permission", "android.permission.GET_TASKS");
			return true;
		}
	}

	/** get json of exit app msg */
	private String getExitAppString() {
		JSONObject exitData = new JSONObject();
		try {
			exitData.put("create_date", preference.getAppStartDate());
			exitData.put("end_date", preference.getAppExitDate());
			exitData.put("session_id", preference.getSessionID());

			Ln.i("launchData---------->", exitData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return exitData.toString();
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (!Thread.currentThread().isInterrupted()) {
			// 判断下载线程是否中断
			switch (msg.what) {
			case UPLOAD_LOG:
				uploadAllLog();
				break;
			case SAVE_SEND_LOG:
				Bundle bundle = msg.getData();
				String jsonString = bundle.getString("value");
				// Ln.i("insert msg ==>>", jsonString);
				sendIsAppExitMsg();
				MessageUtils.insertMsg(context, bundle.getString("action"), jsonString);
				uploadAllLog();
				break;
			case EXIT_APP:
				// exit app when the app in the background 15s
				if (appBackRunning) {
					return true;
				}
				appBackRunning = true;
				appExitCount = 0;
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (appBackRunning) {
							try {
								if (isAppExit()) {
									appExitCount++;
									Ln.i("exit app after background seconds ==>>", appExitCount + "");
									if (appExitCount == 15) {
										preference.setAppExitDate();
										Ln.i("exit app ==>>", " upload all log ");
										MessageUtils.insertMsg(context, MessageUtils.EXIT_DATA, getExitAppString());
										sendUploadLogMsg();
									} else if (appExitCount >= 30) {
										appBackRunning = false;
									}
								} else {
									appExitCount = 0;
								}
								Thread.sleep(1000);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
				break;
			}
		}
		return false;
	}

}
