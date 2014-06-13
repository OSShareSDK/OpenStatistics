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
package cn.sharesdk.open.statistics.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import cn.sharesdk.open.statistics.R;
import cn.sharesdk.open.statistics.db.MessageModel;
import cn.sharesdk.open.statistics.db.MessageUtils;
import cn.sharesdk.open.statistics.model.PostResult;
import cn.sharesdk.open.statistics.net.NetworkHelper;
import cn.sharesdk.open.statistics.net.UpdateManager;
import cn.sharesdk.open.statistics.util.DeviceHelper;
import cn.sharesdk.open.statistics.util.Ln;
import cn.sharesdk.open.statistics.util.PreferencesHelper;

public class ServiceHelper extends HandlerThread implements Callback{

	private final static int REFRESH_NOTIFICATION = 1;
	private final static int INSTALL_APK = 2;
	private final static int TOAST_MSG = 3;
	private final static int CANCEL_NOTIFICATION = 4;
	private final static int UPLOAD_LOG = 5;
	private final static int DOWNLOAD_APK = 6;
	private final static int SAVE_SEND_LOG = 7;
	private final static int IS_UPDATE_APK = 8;
	private final static int UPDATE_CONFIG = 9;
	private final static int EXIT_APP = 10;
	
	private final static String PLATFORM_ID = "1";// android:1,ios:2
	private final static String SDK_VERSION = "1.0";// android:1,ios:2
	
	private final static String uploadUrl = "";//"/client/send/setdata";
	private final static String onlineConfigUrl = "";//"/client/config/getpolicy";
	
	//已下载的apk列表,name-path
	private HashMap<String, String> downloadAPK = new HashMap<String, String>();
	//正在下载的apk任务列表
	private HashMap<String, String> downloadUrl = new HashMap<String, String>();
	//url-notification,
	private Map<String, Notification> notificationCache = new HashMap<String, Notification>();
	//url-downloadState
	private HashMap<Integer, Boolean> deletState = new HashMap<Integer, Boolean>();
	
	private int flag = 0;
	private int appExitCount = 0;
	private String appkey = "";
	private String channel = "";
	private String preUrl = "http://192.168.1.195/statistics.sharesdk.cn/api/index.php";//"http://192.168.9.32:8080/api";

	private boolean appBackRunning = false;
	private boolean autoLocation = false;
	private boolean setBaseURL = false;

	private Context context;
	private Handler handler;
	private PreferencesHelper preference;
	private DeviceHelper deviceHelper;	
	private static ServiceHelper serviceHelper;
	private NotificationManager notificationMrg;
	
	private ServiceHelper(RemoteService service){
		super("ShareSDK Statistics Service");
		start();
		handler = new Handler(this.getLooper(), this);
		this.context = service.getApplicationContext();
		notificationMrg = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		//TODO 扫描已下载的apk的名字，然后保存起来
		//downloadAPK = getDownloadApkList();
		preference = PreferencesHelper.getInstance(context);
		deviceHelper = DeviceHelper.getInstance(context);
		sendIsAppExitMsg();
	}
	
	public static ServiceHelper getInstance(RemoteService service){
		if(serviceHelper == null){
			serviceHelper = new ServiceHelper(service);
		}
		return serviceHelper;
	}	

	/** 获取下载apk目录路径
	 *  get the file path of download apk
	 *  */
	private  String getDownloadPath() {
		String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File parentFile = new File(sdcardPath, "ShareSDKStatistics");
		if(!parentFile.exists()){
			parentFile.mkdirs();
		}
		return parentFile.getAbsolutePath();
	}
	
	/**
	 * 获取已下载的apk列表
	 * get the apk list which have been download
	 */
	@Deprecated
	public HashMap<String, String> getDownloadApkList(){
		HashMap<String, String> downloadAPK = new HashMap<String, String>();
		File parentFile = new File(getDownloadPath());
		
		File[] filesList = parentFile.listFiles();
		if(filesList != null){
			for(File file : filesList){
				if(file.getName().endsWith(".apk")){
					downloadAPK.put(file.getName(), file.getAbsolutePath());
					Log.e("download apk name == path >>>", file.getName() + "===" + file.getAbsolutePath());
				}
			}
		}
		return downloadAPK;
	}
	
	/**
	 * get the file name from the url
	 * @param url
	 * @return
	 */
	private String getApkName(String url){
		if(!url.endsWith(".apk")){
			return null;
		}
		int first = url.lastIndexOf("/") + 1;
		String filename = url.substring(first, url.length()).toLowerCase();
		Log.i("filename", "从url中截取的filename  " + filename);
		return filename;
	}
	
	//update notification state
	private void sendNotificationMsg(int c, String url, Notification notification, int flag, String filename) {
		Message msg = new Message();
		msg.what = REFRESH_NOTIFICATION;// 用来识别发送消息的类型
		Holder holder = new Holder();
		holder.count = c;
		holder.url = url;
		holder.flag = flag;
		holder.notify = notification;
		holder.filename = filename;
		msg.obj = holder;// 消息传递的自定义对象信息
		handler.sendMessage(msg);
	}
	
	//install APK
	private void sendInstallApkMsg(Uri uri){
		Message msg = new Message();
		msg.what = INSTALL_APK;
		msg.obj = uri;
		handler.sendMessage(msg);
	}

	//cancel the file of download 
	public void sendCancelMsg(int flag){
		deletState.put(flag, true);
		Message msg = new Message();
		msg.what = CANCEL_NOTIFICATION;
		msg.arg1 = flag;
		handler.sendMessage(msg);
	}
	
	//upload log
	public void sendUploadLogMsg(){
		
		Message msg = new Message();
		msg.what = UPLOAD_LOG;
		handler.sendMessage(msg);
	}

	//download APK
	public void sendDownloadApkMsg(String url){
		Message msg = new Message();
		msg.what = DOWNLOAD_APK;
		msg.obj = url;
		handler.sendMessage(msg);
	}
	
	//send log to server
	public void saveAndSendLogkMsg(Bundle bundle){
		Message msg = new Message();
		msg.what = SAVE_SEND_LOG;
		msg.setData(bundle);
		handler.sendMessage(msg);
	}
	
	//upload log
	public void sendUpdateApkMsg(){
		Message msg = new Message();
		msg.what = IS_UPDATE_APK;
		handler.sendMessage(msg);
	}
		
	//upload log
	public void sendUpdateConfigMsg(){
		Message msg = new Message();
		msg.what = UPDATE_CONFIG;
		handler.sendMessage(msg);
	}
	
	//upload log
	public synchronized void sendIsAppExitMsg(){
		Message msg = new Message();
		msg.what = EXIT_APP;
		handler.sendMessageDelayed(msg, 1000);
	}
	
	public void setAutoLocation(boolean autoLocation){
		this.autoLocation = autoLocation;
	}
	
	public void setAppKey(String appkey){
		if(!TextUtils.isEmpty(appkey)){
			this.appkey = appkey;
		}
	}
	
	public String getAppKey(){
		if(TextUtils.isEmpty(appkey)){
			appkey = deviceHelper.getAppKey();
		}
		return appkey;
	}

	public void setChannel(String channel){
		if(!TextUtils.isEmpty(channel)){
			this.channel = channel;
		}
	}
	
	private String getChannel(){
		if(TextUtils.isEmpty(channel)){
			channel = deviceHelper.getChannel();
		}
		return channel;
	}

	public void setBaseURL(String url){
		if(!TextUtils.isEmpty(url)){
			preUrl = url;
			setBaseURL = true;
		}
	}
	
	/** get upload-log url */
	private String getUploadLogUrl() {
		if(!setBaseURL){
			String apiPath = preference.getReportApiPath();
			if(!TextUtils.isEmpty(apiPath)){
				preUrl = apiPath;
			}
		}		
		return preUrl + uploadUrl;
	}

	/** get online-config url */
	private String getOnlineConfigUrl() {
		if(!setBaseURL){
			String apiPath = preference.getReportApiPath();
			if(!TextUtils.isEmpty(apiPath)){
				preUrl = apiPath;
			}
		}		
		return preUrl + onlineConfigUrl;
	}

	/** get isupdate url */
	private String getUpdateUrl() {
		if(!setBaseURL){
			String apiPath = preference.getReportApiPath();
			if(!TextUtils.isEmpty(apiPath)){
				preUrl = apiPath;
			}
		}		
		return preUrl + onlineConfigUrl;
	}

	/**
	 * Getting latitude of location
	 * @return
	 */
	private String getLatitude(){
		String latitude = "";
		if(autoLocation){
			latitude = deviceHelper.getLatitude();
		}
		return latitude;
	}
	
	/**
	 * Getting longtitude of location
	 * @return
	 */
	private String getLongitude(){
		String longtitude = "";
		if(autoLocation){
			longtitude = deviceHelper.getLongitude();
		}
		return longtitude;
	}
	
	/**
	 * get event data from db,and send to server
	 */
	private void uploadAllLog(){
		ArrayList<MessageModel> msgList = MessageUtils.getEventMsg(context);
		for(MessageModel model : msgList){
			uploadLog(model);
		}
	}
	
	/**upload all log to server*/
	private boolean uploadLog(MessageModel eventData) {
		String content = eventData.data;
		if (!TextUtils.isEmpty(content) && deviceHelper.isNetworkAvailable()) {
			try {
				JSONObject object = new JSONObject(content);
				object.put(MessageUtils.DEVICE_DATA, getDeviceJSONObject());	
				
				//TODO 写入文件里面
				//PreferencesHelper.getInstance(context).saveEInfoToFile(object);
				//TODO 发送地址改变
				content = object.toString();
				if(Ln.DebugMode){
					Toast.makeText(context, "Server address : " + getUploadLogUrl(), 1000).show();
				}
				Log.i("server address ==>>>", getUploadLogUrl());
				PostResult post = NetworkHelper.post(getUploadLogUrl(), content, getAppKey());
				if (post!=null && post.isSuccess()) {
					boolean success = parseResponseData(post.getResponseMsg());
					if (success) {
						if(Ln.DebugMode){
							Toast.makeText(context, "Send msg successfully!", 1000).show();
						}
						MessageUtils.deleteManyMsg(context, eventData.idList);
					} else {
						if(Ln.DebugMode){
							Toast.makeText(context, "Fail to send msg !", 1000).show();
						}
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
	 * update the config from the server
	 */
	private void updateOnlineConfig() {

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
	
	/** Dose update the application or not , the method is kept to use in the future
	 * install app need this permission - <android.permission.INSTALL_PACKAGES/>
	 * */
	private void isUpdate() {
		
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
	
	/**
	 * parse the response from server
	 * 
	 * @param jsonMsg
	 */
	private boolean parseResponseData(String jsonMsg) {
		//To solve the coding problems. eg. utf-8
		if(jsonMsg.startsWith("\ufeff")) {
			Ln.w(" parseResponseData jsonMsg.startsWith(\\ufeff) == >>", "jsonMsg error");
			jsonMsg = jsonMsg.substring(1);
		}
		
		try {
			JSONObject object = new JSONObject(jsonMsg);
			int status = Integer.parseInt(object.getString("status"));
			if (status == 200){
				if (object.isNull("res")){
					return true;
				}else{
					object = object.getJSONObject("res");
				}
				
				if (object.isNull("config")){
					return true;
				}else{
					object = object.getJSONObject("config");
				}
				
				if (object.isNull("api_path")){
					return true;
				}
				
				String apiPath = object.getString("api_path");
				if(!TextUtils.isEmpty(apiPath)){
					preUrl = apiPath; 
					preference.setReportApiPath(apiPath);
				}
				
				return true;
			}else{
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
			clientData.put("is_pirate", 0);
			clientData.put("is_jailbroken", deviceHelper.isRooted()?1:0);
			clientData.put("longitude", getLongitude());
			clientData.put("latitude", getLatitude());
			clientData.put("language", deviceHelper.getLanguage());
			clientData.put("timezone", deviceHelper.getTimeZone());
			clientData.put("cpu", deviceHelper.getCpuName());
			clientData.put("manuid", deviceHelper.getManuID());

			String manutime = deviceHelper.getTime(Long.parseLong(deviceHelper.getManuTime()));
			clientData.put("manutime", manutime);

			//Ln.i("deviceData---------->", clientData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return clientData;
	}
	
	@Deprecated
	private void downloadApk(final String url){

		String apkName = getApkName(url);
		//判断以前是否已经下载过
		if(downloadAPK.containsKey(apkName)){
			//TOTO 安装提示
			File file = new File(downloadAPK.get(apkName));
			if(file.exists()){
				Uri uri = Uri.fromFile(file);
				sendInstallApkMsg(uri);
				return;
			}
		}
				
		//TODO 如果下载任务已经在下载中，就不重复下载
		if(downloadUrl.containsKey(apkName)){
			return;
		}else{
			downloadUrl.put(apkName, url);
		}
				
		System.out.println("Get url from intent:" + url);
		Runnable start = new Runnable() {
			public void run() {
				deletState.put(++flag, false);
				startLoadFile(url, flag);
			}
		};
		new Thread(start) {}.start();
	}
	
	/**
	 * download apk
	 * @param url
	 * @param flag
	 */
	@Deprecated
	private void startLoadFile(String url, int flag) {
		Intent notificationIntent = new Intent(context, this.getClass());
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// addflag设置跳转类型
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		// 创建Notifcation对象，设置图标，提示文字
		Notification notification = new Notification();// 设定Notification出现时的声音，一般不建议自定义
		notification.icon = android.R.drawable.stat_sys_download;//R.drawable.ic_launcher;
		//notification.tickerText = "download apk";
		notification.flags |= Notification.FLAG_ONGOING_EVENT;//FLAG_ONGOING_EVENT;// 出现在 “正在运行的”栏目下面
		
		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.app_download_notification);
		contentView.setTextViewText(R.id.tv_progress_title, "下载APP");
		contentView.setTextViewText(R.id.tv_progress_text, "当前进度：" + 0 + "% ");
		contentView.setProgressBar(R.id.progress_bar, 100, 0, false);
		//设置点击事件
		Intent broadcastIntent = new Intent(RemoteService.BROADCAST_ACTION);
		broadcastIntent.setFlags(flag);
        PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);        
		contentView.setOnClickPendingIntent(R.id.notify_layout, deleteIntent);
		
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;
		notification.deleteIntent = deleteIntent;
		
		
		String filename = getApkName(url);
		
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response;
		File file = null;
		int percent = 0;
		try {
			response = client.execute(get);
			HttpEntity entity = response.getEntity();
			double length = entity.getContentLength();
			InputStream is = entity.getContent();
			// 使用InputStream对文件进行读取，就是字节流的输入
			FileOutputStream fileOutputStream = null;

			if (is != null) {
				//TODO ,把下载的app保存起来，如果重复下载的话，给安装
				file = new File(getDownloadPath(), filename);
				fileOutputStream = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				int ch = -1;
				float compeleteSize = 0;
				// ch中存放从buf字节数组中读取到的字节个数
				while ((ch = is.read(buf)) != -1) {
					fileOutputStream.write(buf, 0, ch);
					compeleteSize += ch;
					// 从字节数组读取数据read(buf)后，返回，读取的个数，count中保存，已下载的数据字节数
					double load = compeleteSize * 100 / length;
					if (load >= percent) {
						Log.i("TAG", "读取字节循环中的count" + load);
						percent ++;
						sendNotificationMsg((int)load, url, notification, flag, filename);
					}
					if(deletState.get(flag)){
						file.delete();
						return;
					}
				}
			}
			// 文件输出流为空，则表示下载完成，安装apk文件
			Uri uri = Uri.fromFile(file);
			Log.i("TAG", "下载完成，传递文件位置Url  " + uri);
			
			fileOutputStream.flush();
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}

			//TODO 更新下载列表和apk列表
			downloadUrl.remove(filename);
			downloadAPK.put(filename, url);
			sendInstallApkMsg(uri);
		} catch (Exception e) {
			e.printStackTrace();
			if(file!= null && file.exists()){
				file.delete();
			}
			//sendErrorMsg(e.getCause().getMessage());
		}
	}
	
	// 状态栏视图更新
	private Notification displayNotificationMessage(Notification notification, int count, int flag, String url, String filename) {
		RemoteViews contentView1 = notification.contentView;
		Log.i("TAG", "updata   flag==  " + flag);
		Log.i("TAG", "updata   count==  " + count);
		Log.i("TAG", "updata   filename==  " + filename);
		contentView1.setTextViewText(R.id.tv_progress_title, filename);
		contentView1.setTextViewText(R.id.tv_progress_text, "当前进度：" + count + "% ");
		contentView1.setProgressBar(R.id.progress_bar, 100, count, false);
		notification.contentView = contentView1;
		notificationMrg.notify(flag, notification);
		return notification;
	}
	
	private void openfile(Uri url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(url, "application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	private class Holder {
		Notification notify;
		String url;
		int count;
		int flag;
		String filename;
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
			Ln.i("ComponentName getPackageName ==>>", cn.getPackageName());
			boolean isEqual = packageName.equals(cn.getPackageName()); 
			return !isEqual;
		} else {
			Ln.e("lost permission", "android.permission.GET_TASKS");
			return true;
		}
	}

	@Deprecated
	public boolean isAppOnForeground() {
		// Returns a list of application processes that are running on the
		// device
		String packageName = context.getPackageName();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		for (RunningAppProcessInfo appProcess : appProcesses) {
			// importance:
			// The relative importance level that the system places
			// on this process.
			// May be one of IMPORTANCE_FOREGROUND, IMPORTANCE_VISIBLE,
			// IMPORTANCE_SERVICE, IMPORTANCE_BACKGROUND, or IMPORTANCE_EMPTY.
			// These constants are numbered so that "more important" values are
			// always smaller than "less important" values.
			// processName:
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName) && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
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
			case REFRESH_NOTIFICATION:
				final Holder data = (Holder) msg.obj;
				Log.i("TAG", "handlemessage中的 case 1: data.count     " + data.count);
				Log.i("TAG", "handlemessage中的 case 1: flag          " + data.flag);
				if (data.count >= 99) {
					notificationMrg.cancel(data.flag);
					break;
				}
				Notification notification;
				if (notificationCache.containsKey(data.url)) {
					// 每次更新时，先以key，扫描hashmap，存在则读取出来。
					notification = notificationCache.get(data.url);
					notification = displayNotificationMessage(notification, data.count, data.flag, data.url, data.filename);
					notificationCache.put(data.url, notification);
				} else {
					notification = data.notify;
					notification = displayNotificationMessage(notification, data.count, data.flag, data.url, data.filename);
					notificationCache.put(data.url, notification);
				}
				// }
				break;
			case INSTALL_APK:
				Uri uri = (Uri)msg.obj;
				Log.i("TAG", "case 2中Uri ：  " + uri);
				openfile(uri);
				break;
			case TOAST_MSG:
				String error = (String)msg.obj;
				Toast.makeText(context, "error===>>>"+error, 1).show();
				break;
			// 否则输出错误提示
			case CANCEL_NOTIFICATION:
				notificationMrg.cancel(msg.arg1);
				break;
			case UPLOAD_LOG:
				uploadAllLog();
				break;
			case DOWNLOAD_APK:
				downloadApk((String)msg.obj);
				break;
			case SAVE_SEND_LOG:
				Bundle bundle = msg.getData();
				String jsonString = bundle.getString("value");
				//Ln.i("insert msg ==>>", jsonString);
				sendIsAppExitMsg();
				MessageUtils.insertMsg(context, bundle.getString("action"), jsonString);
				uploadAllLog();
				break;
			case IS_UPDATE_APK:
				isUpdate();
				break;
			case UPDATE_CONFIG:
				updateOnlineConfig();
				break;
			case EXIT_APP:
				// TOTO exit app when the app in the background 15s	
				Ln.i("EXIT_APP ==>> appBackRunning", "EXIT_APP ==>>>" + appBackRunning);
				if(appBackRunning){
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
									Ln.i("exit app count ==>>", appExitCount + "");
									if (appExitCount == 15) {
										preference.setAppExitDate();
										Ln.i("exit app ==>>", " upload all log ");
										MessageUtils.insertMsg(context, MessageUtils.EXIT_DATA, getExitAppString());
										sendUploadLogMsg();
									}else if(appExitCount >= 30){
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
