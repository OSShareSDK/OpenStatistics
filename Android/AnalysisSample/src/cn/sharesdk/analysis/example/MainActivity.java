/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sharesdk.analysis.example;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import cn.sharesdk.analysis.EventManager;
import cn.sharesdk.analysis.MobclickAgent;

/**
 * 
 * Before you run App,you need check:
 * 
 * 1.Import appkey (generated in server ) to AndroidManifest.xml such as
 *   <meta-data android:name="ShareSDK_APPKEY" android:value="bb08202a625c2b5cae5e2632f604352f "/>
 *   Import channel
 *   <meta-data android:name="SHARESDK_CHANNEL" android:value="SHARESDK_CHANNEL_A"/>
 *   
 * 2.Permissions in AndroidManifest.xml
 * 
 * <uses-permission android:name="android.permission.INTERNET"/>
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 * <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
 * <uses-permission android:name="android.permission.GET_TASKS"/>
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
	
	private Button btn_click;
	private Button btn_click_acc;
	private Button btn_click_label_acc;
	private Button btn_click_hashmap;
	private Button btn_update_online_config;
	private Button btn_app_list;
	private Button btn_to_activity;
	private Button btn_to_webpage;
	private Button btn_to_fragment_stack;
	private Button btn_to_fragment_tabs;
	private Button btn_close_debug;
	private EditText et_ip;
	private Button btn_ip;
	private EditText et_appkey;
	private Button btn_appkey;
	
	private boolean isDebug = true;
	private final String pageName = "TestMainPage";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sharesdk_example_activity_main);
		
		initView();
		initData();
		
	}
	
	private void initView(){
		btn_click = (Button) findViewById(R.id.btn_click);
		btn_click_acc = (Button) findViewById(R.id.btn_click_acc);
		btn_click_label_acc = (Button) findViewById(R.id.btn_click_label_acc);
		btn_click_hashmap = (Button) findViewById(R.id.btn_click_hashmap);
		btn_update_online_config = (Button) findViewById(R.id.btn_update_online_config);
		btn_to_activity = (Button) findViewById(R.id.btn_to_activity);
		btn_app_list = (Button) findViewById(R.id.btn_app_list);
		btn_to_webpage = (Button) findViewById(R.id.btn_to_webpage);
		btn_to_fragment_tabs = (Button) findViewById(R.id.btn_to_fragment_tabs);
		btn_to_fragment_stack = (Button) findViewById(R.id.btn_to_fragment_stack);
		et_ip = (EditText) findViewById(R.id.et_ip_input);
		btn_ip = (Button)findViewById(R.id.btn_ip_ensure); 
		et_appkey = (EditText) findViewById(R.id.et_appkey_input);
		btn_appkey = (Button)findViewById(R.id.btn_appkey_ensure); 
		btn_close_debug = (Button)findViewById(R.id.btn_close_debug);
		
		btn_click.setOnClickListener(this);
		btn_click_acc.setOnClickListener(this);
		btn_click_label_acc.setOnClickListener(this);
		btn_click_hashmap.setOnClickListener(this);
		btn_update_online_config.setOnClickListener(this);
		btn_to_activity.setOnClickListener(this);
		btn_app_list.setOnClickListener(this);
		btn_app_list.setVisibility(View.GONE);
		btn_to_webpage.setOnClickListener(this);
		btn_to_fragment_tabs.setOnClickListener(this);
		btn_to_fragment_stack.setOnClickListener(this);
		btn_ip.setOnClickListener(this);
		btn_appkey.setOnClickListener(this);
		btn_close_debug.setOnClickListener(this);
		
	}

	private void initData(){
		
		//If close debug mode ,there is not any logs.
		MobclickAgent.setDebugMode(isDebug);	

//      When use SDK in Fragment，you should close the activity-page-statistics�?//		Then you have to use the methods of onPageStart/onPageEnd instand of the onResume/onPause two method
		MobclickAgent.openActivityDurationTrack(false);
		/**
		 * Call MobclickAgent.setBaseURL(String url) before all other APIs. url:
		 * ShareSDKStatistics web server
		 * 
		 * */
		et_ip.setText("http://61.153.100.86/api");
		MobclickAgent.setBaseURL("http://61.153.100.86/api");//("http://tj.sharesdk.cn/demo/Api/index.php/Client/Send/setData");//
		et_appkey.setText("855a5f1115028cfc9649f9cb529880b5");
		
		/**
		 * SDK could help you catch exit exception during App usage and send
		 * error report to server.
		 * 
		 * Error report includes App version, OS version, device type and
		 * stacktrace of exception.
		 * 
		 * These data will help you modify App bug.
		 * 
		 * We provide two ways to report error info.
		 * 
		 * One is catched automatically by system and another is passed by
		 * developers.
		 * 
		 * For the former, you need to add android.permission.READ_LOGS
		 * permission in AndroidManifest.xml and call
		 * MobclickAgent.onError(Context) in onCreate of Main Activity
		 * 
		 * 
		 * For the latter, developers need to call
		 * MobclickAgent.onError(Context,String) and pass error info catched by
		 * their own to the second parameter. You can view error report in
		 * product page of ShareSDKStatistics system.
		 * */
		MobclickAgent.onError(this);

		/**Call MobclickAgent.updateOnlineConfig(Context context)
		 * to update the local report policy when necessary.
		 * */
		//MobclickAgent.updateOnlineConfig(this);
		
		Dialog dialog = new AlertDialog.Builder(this)
		.setTitle(R.string.dialog_title)
		.setMessage(R.string.dialog_msg)
		.setPositiveButton(R.string.dialog_btn, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		})
		.create();
		dialog.show();
	}
	
	@Override
	protected void onResume() {

		super.onResume();

		/**
		 * 
		 * Call MobclickAgent.onResume(Context) in onResume method of every
		 * Activity. The parameter is the current context. This method will read
		 * AppKey from AndroidManifest.xml automatically. Do not pass global
		 * application context.
		 * 
		 * We recommend you Calling this method in all Activities If not,some
		 * informations of corresponding Activities will be lost,eg time
		 * */
		MobclickAgent.onPageStart(pageName);
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {

		super.onPause();

		/**
		 * Call MobclickAgent.onPause(Context) in onPause method of every
		 * Activity. Parameter is current context.
		 * 
		 * We recommend you Calling this method in all Activities If not,some
		 * informations of corresponding Activities will be lost,eg time
		 * */
		MobclickAgent.onPageEnd(pageName);
		MobclickAgent.onPause(this);
		//android.os.Process.killProcess(android.os.Process.myPid());		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			/**
			 * Call following MobclickAgent.onEvent(Context context,String
			 * event_id) could send event logs to server. It will analyze
			 * times and changing trends of event, eg ad_clicks. For
			 * example, we now monitor the event of ad clicks in App and the
			 * context is MainActivity Event ID is " ad_click" defined in
			 * server . Once ad is clicked, you need call
			 * MobclickAgent.onEvent(MainActivity.this,"ad_click") in App
			 * Then we will observe that the "Number of Messages" has
			 * changed according to the Event ID of "ad_click" in server.
			 * */
			case R.id.btn_close_debug:
				if(isDebug){
					isDebug = false;
					MobclickAgent.setDebugMode(isDebug);
					btn_close_debug.setText("打开调试模式");
				}else{
					isDebug = true;
					MobclickAgent.setDebugMode(isDebug);
					btn_close_debug.setText("关闭调试模式");
				}
				break;
			case R.id.btn_click:
				MobclickAgent.onEvent(MainActivity.this, "event_play");
				break;
			case R.id.btn_click_acc:
				MobclickAgent.onEvent(MainActivity.this, "event_play_times", 33);
				break;
			case R.id.btn_click_label_acc:
				MobclickAgent.onEvent(MainActivity.this, "event_play_name_time", "Never Grow Old", 10);				
				break;
			case R.id.btn_click_hashmap:
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("map_tomato", "10");
				map.put("map_cabbage", "15");
				map.put("map_potato", "20");
				map.put("map_carrot", "25");
				map.put("map_bean", "30");
				map.put("map_rabit", "35");
				map.put("map_snake", "40");
				map.put("map_tiger", "45");
				map.put("map_goat", "50");
				map.put("map_leopard", "100");
				MobclickAgent.onEvent(MainActivity.this, "event_hash_play_games", map);
				break;
			case R.id.btn_update_online_config:
				break;
			case R.id.btn_to_activity:
				Intent i = new Intent(MainActivity.this, TestActivity1.class);
				startActivity(i);
				this.finish();
				break;	
//			case R.id.btn_app_list:
//				break;
			case R.id.btn_to_webpage:
				startActivity( new Intent(MainActivity.this, WebviewAnalytic.class));
				break;
			case R.id.btn_to_fragment_tabs:
				startActivity( new Intent(MainActivity.this, FragmentTabs.class));
				break;
			case R.id.btn_to_fragment_stack:
				startActivity( new Intent(MainActivity.this, FragmentStack.class));
				break;
			case R.id.btn_ip_ensure:
				String ipAddress = et_ip.getText().toString();
				if(!TextUtils.isEmpty(ipAddress)){
					MobclickAgent.setBaseURL(ipAddress);
				}
				break;
			case R.id.btn_appkey_ensure:
				String appKey = et_appkey.getText().toString();
				if(!TextUtils.isEmpty(appKey)){
					EventManager.setAppKey(appKey);
				}
				break;
			default:
				break;
		}
		
	}
	
}
