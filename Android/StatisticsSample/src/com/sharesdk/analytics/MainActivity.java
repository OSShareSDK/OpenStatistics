package com.sharesdk.analytics;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.sharesdk.open.statistics.MobclickAgent;

import com.sharesdk.analytics.data.Constant;

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
	
	Context context;
	Button btn_click;
	Button btn_click_acc;
	Button btn_click_label_acc;
	Button btn_click_hashmap;
	Button btn_update_online_config;
	Button btn_to_activity;
	TextView tv_show;
	
	//event id
	final String btn_click_id = "event_";
	final String btn_click_acc_id = "event_acc_";
	final String btn_click_label_acc_id = "event_label_acc_";
	final String btn_click_hashmap_id = "event_hashmap_";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		initData();
		
	}
	
	private void initView(){
		context = MainActivity.this;
		btn_click = (Button) findViewById(R.id.btn_click);
		btn_click_acc = (Button) findViewById(R.id.btn_click_acc);
		btn_click_label_acc = (Button) findViewById(R.id.btn_click_label_acc);
		btn_click_hashmap = (Button) findViewById(R.id.btn_click_hashmap);
		btn_update_online_config = (Button) findViewById(R.id.btn_update_online_config);
		btn_to_activity = (Button) findViewById(R.id.btn_to_activity);
		tv_show = (TextView) findViewById(R.id.tv_show);
		
		btn_click.setOnClickListener(this);
		btn_click_acc.setOnClickListener(this);
		btn_click_label_acc.setOnClickListener(this);
		btn_click_hashmap.setOnClickListener(this);
		btn_update_online_config.setOnClickListener(this);
		btn_to_activity.setOnClickListener(this);
		
		tv_show.setText("欢迎使用ShareSDK Statistics 开源统计SDK \n 当前页面是"+this.getClass().getName()+"\n");
		
	}

	private void initData(){

		MobclickAgent.setDebugMode(true);
		MobclickAgent.setUpdateOnlyWifi(context, true);

		/**
		 * Call MobclickAgent.setBaseURL(String url) before all other APIs. url:
		 * ShareSDKStatistics web server
		 * 
		 * */
		MobclickAgent.setBaseURL("http://192.168.1.195/statistics.sharesdk.cn/api/index.php");

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
		MobclickAgent.onPause(this);
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
			case R.id.btn_click:
				MobclickAgent.onEvent(MainActivity.this, btn_click_id + Constant.getRandomEventEnd());
				break;
			case R.id.btn_click_acc:
				MobclickAgent.onEvent(MainActivity.this, btn_click_acc_id + Constant.getRandomEventEnd(), Constant.getRandomNum());
				break;
			case R.id.btn_click_label_acc:
				MobclickAgent.onEvent(MainActivity.this, btn_click_label_acc_id + Constant.getRandomEventEnd(), "play_music_time", Constant.getRandomNum());				
				break;
			case R.id.btn_click_hashmap:
				HashMap<String, String> map = new HashMap<String, String>();
				for(int i=0; i< 10; i++){
					map.put("map_" + Constant.getRandomEventEnd(), Constant.getRandomNum(100));
				}
				MobclickAgent.onEvent(MainActivity.this, btn_click_hashmap_id + Constant.getRandomEventEnd(), map);				
				break;
			case R.id.btn_update_online_config:
				/**Call MobclickAgent.updateOnlineConfig(Context context)
				 * to update the local report policy when necessary.
				 * */
				MobclickAgent.updateOnlineConfig(this);
				break;
			case R.id.btn_to_activity:
				Intent i = new Intent(MainActivity.this, TestActivity1.class);
				startActivity(i);
				break;	
			default:
				break;
		}
		
	}
	
}
