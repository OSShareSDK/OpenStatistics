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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import cn.sharesdk.open.statistics.model.PostEvent;
import cn.sharesdk.open.statistics.util.Ln;

public class LoggerThread extends HandlerThread implements Callback {

	private final static int LOG_EVENT = 0;
	private final static int LOG_ERROR = 1;
	private final static int LOG_ERROR_INFO = 2;
	private final static int LOG_DURATION = 3;
	private final static int LOG_PAUSE = 4;
	private final static int LOG_RESUME = 5;
	private final static int LOG_ISUPDATE = 6;
	private final static int LOG_UPDATE_ONLINE_CONFIG = 7;
	private final static int LOG_UPLOAD_LOG = 8;
	
	private final static int SET_WIFI_REPORT_POLICY = 9;

	private Context context;
	private Handler handler;
	private static LoggerThread loggerThread;

	private LoggerThread() {
		super("ShareSDK Statistics");
		start();
		handler = new Handler(this.getLooper(), this);
	}

	public static LoggerThread getInstance() {
		if (loggerThread == null) {
			loggerThread = new LoggerThread();
		}
		return loggerThread;
	}

	public void onError(Context context) {
		sendMessage(context, LOG_ERROR);
	}

	public void onEventDuration(Context context, PostEvent event) {
		sendMessage(context, LOG_DURATION, event);
	}

	public void onPause(Context context) {
		sendMessage(context, LOG_PAUSE);
	}
	
	public void onResume(Context context) {
		sendMessage(context, LOG_RESUME);
	}

	public void onResume(Context context,String appkey, String channel) {
		EventManager.setAppKey(appkey);
		EventManager.setChannel(channel);
		sendMessage(context, LOG_RESUME);
	}

	public void isUpdate(Context context) {
		sendMessage(context, LOG_ISUPDATE);
	}

	public void updateOnlineConfig(Context context) {
		sendMessage(context, LOG_UPDATE_ONLINE_CONFIG);
	}
	
	public void updateOnlineConfig(Context context, String appkey, String channelID) {
		EventManager.setAppKey(appkey);
		EventManager.setChannel(channelID);
		sendMessage(context, LOG_UPDATE_ONLINE_CONFIG);
	}

	public void onError(Context context, String error) {
		sendMessage(context, LOG_ERROR_INFO, error);
	}

	public void onEvent(Context context, PostEvent event) {
		sendMessage(context, LOG_EVENT, event);
	}

	private void sendMessage(Context context, int what) {
		sendMessage(context, what, null);
	}

	private void sendMessage(Context context, int what, Object obj) {
		setContext(context);
		
		Message msg = new Message();
		msg.what = what;
		msg.obj = obj;
		handler.sendMessage(msg);
	}

	@Override
	public boolean handleMessage(Message msg) {
		if(context == null && this.context == null){
			Ln.e("context is null ==>>>", "please set the context!");
			return true;
		}
		EventManager.init(context);
		
		switch (msg.what) {
		case LOG_EVENT:
			EventManager.onEvent((PostEvent) msg.obj);
			break;
		case LOG_ERROR:
			EventManager.onError();
			break;
		case LOG_ERROR_INFO:
			EventManager.onError(String.valueOf(msg.obj));
			break;
		case LOG_DURATION:
			EventManager.onEventDuration((PostEvent) msg.obj);
			break;
		case LOG_PAUSE:
			EventManager.onPause();
			break;
		case LOG_RESUME:
			EventManager.onResume();
			break;
		case LOG_ISUPDATE:
			EventManager.isUpdate();
			break;
		case LOG_UPDATE_ONLINE_CONFIG:
			EventManager.updateOnlineConfig();
			break;
		case LOG_UPLOAD_LOG:
			EventManager.uploadLog();
			break;
		case SET_WIFI_REPORT_POLICY:
			EventManager.setWifiReportPolicy((Boolean)msg.obj);
			break;
		default:
			break;
		}
		return true;
	};

	public void setContext(Context context){
		if(context != null && this.context == null){
			this.context = context;
		}
	}
	
	// set post log in wifi
	public void setWifiReportPolicy(Context context, boolean isUpdateonlyWifi) {
		sendMessage(context, SET_WIFI_REPORT_POLICY, isUpdateonlyWifi);
	}

	public void onPageStart(String activityName) {
		EventManager.onPageStart(activityName);
	}

	public void onPageEnd(String activityName) {
		EventManager.onPageEnd(activityName);

	}
	
    public void setSessionContinueMillis(long interval){
    	EventManager.setSessionContinueMillis(interval);
    }

	public void onEventBegin(Context context, String event_id) {
		setContext(context);
		EventManager.onEventBegin(context, event_id);

	}
	
	public void onEventBegin(Context context, String event_id, String label) {
		setContext(context);
		EventManager.onEventBegin(context, event_id, label);

	}

	public long onEventEnd(Context context, String event_id) {
		setContext(context);
		return EventManager.onEventEnd(context, event_id);

	}
	
	public long onEventEnd(Context context, String event_id, String label) {
		setContext(context);
		return EventManager.onEventEnd(context, event_id, label);

	}

	/**post log delay millis*/
	public void uploadLogDelay(Context context, int delayMillis) {
		setContext(context);
		Message msg = new Message();
		msg.what = LOG_UPLOAD_LOG;
		handler.sendMessageDelayed(msg, delayMillis);
	}

	public void reportError(Context context, Throwable e) {
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		e.printStackTrace(pw);
		pw.close();
		String error = writer.toString();
		onError(context, error);
	}
	
	public void setBaseURL(String url) {
    	EventManager.setBaseURL(url);
    }

}