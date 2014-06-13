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

import cn.sharesdk.open.statistics.util.Ln;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class RemoteService extends Service {

	public final static String DOWNLOAD_APK = "download_apk";
	
	public final static String SET_APP_KEY = "set_app_key";
	public final static String SET_PREURL = "set_pre_url";
	public final static String SET_CHANNEL = "set_channel";
	public final static String SET_LOCATION = "set_location";
	
	public final static String BROADCAST_ACTION = "broadcast_notification";
	
	ServiceHelper serviceHelper;
	OnBtnClickListener broadcastReceiver;
	public void onCreate() {
		super.onCreate();
		
		serviceHelper = ServiceHelper.getInstance(this);
		broadcastReceiver = new OnBtnClickListener();
		//registe broadcast
		IntentFilter filter = new IntentFilter();
	    filter.addAction(BROADCAST_ACTION);
	    registerReceiver(broadcastReceiver, filter);
	    
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		System.out.println("RemoteService onStartCommand ==>>Get intent");
		if(DOWNLOAD_APK.equals(intent.getAction())){
			final String url = intent.getStringExtra("url");
			serviceHelper.sendDownloadApkMsg(url);
		}		

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {		
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
		this.stopSelf();
	}

	@Override
	public void onRebind(Intent intent) {
	    Ln.e("RemoteService onRebind ==>>", "onRebind");
		super.onRebind(intent);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
	    Ln.e("RemoteService onBind ==>>", "onBind");
		return bind;
	}
	
	AIDLService.Stub bind = new AIDLService.Stub() {
		
		@Override
		public void uploadLog() throws RemoteException {
			serviceHelper.sendUploadLogMsg();
		}
		
		@Override
		public void updateConfig() throws RemoteException {
			serviceHelper.sendUpdateConfigMsg();
		}
		
		@Override
		public void updateApk() throws RemoteException {
			serviceHelper.sendUpdateApkMsg();
		}
		
		@Override
		public void setting(String action, String value) throws RemoteException {
			if(SET_APP_KEY.equals(action)){
				serviceHelper.setAppKey(value);
			}else if(SET_PREURL.equals(action)){
				serviceHelper.sendIsAppExitMsg();
				serviceHelper.setBaseURL(value);
			}else if(SET_CHANNEL.equals(action)){
				serviceHelper.setChannel(value);
			}else if(SET_LOCATION.equals(action)){
				serviceHelper.setAutoLocation(Boolean.parseBoolean(value));
			}
		}
		
		@Override
		public void saveLog(String action, String jsonString) throws RemoteException {
			Bundle bundle = new Bundle();
			bundle.putString("action", action);
			bundle.putString("value", jsonString);
			serviceHelper.saveAndSendLogkMsg(bundle);
		}
		
		@Override
		public void downloadApk(String url) throws RemoteException {
			serviceHelper.sendDownloadApkMsg(url);
		}
	};
	
	
	public class OnBtnClickListener extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("BroadcastReceiver ==>>", "onreceive");
			if(intent.getAction().equals(BROADCAST_ACTION)){
				int notifyID = intent.getFlags();
				Log.e("BroadcastReceiver ==>>", notifyID+"");
				serviceHelper.sendCancelMsg(notifyID);
				//notificationMrg.cancel(notifyID);
			}
		}
		
	}
	
}
