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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.sharesdk.analysis.MobclickAgent;

public class TestActivity2 extends Activity implements OnClickListener{

	Context context;
	TextView tv_show;
	Button btn_duration;
	Button btn_duration_label;
	Button btn_duration_hashmap;
	Button btn_page_start;
	Button btn_page_end;
	Button btn_to_activity;

	//event id
	final String event_duration_id = "duration_";
	final String event_duration_label_id = "duration_label_";
	final String event_duration_hashmap_id = "duration_hashmap_";

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sharesdk_example_activity_test2);
	    
        initView();
        initData();
    }
    
    private void initView(){
    	context = this;
    	tv_show = (TextView)findViewById(R.id.tv_show);
    	btn_duration = (Button) findViewById(R.id.btn_duration);
    	btn_duration_label = (Button) findViewById(R.id.btn_duration_label);
    	btn_duration_hashmap = (Button) findViewById(R.id.btn_duration_hashmap);
    	btn_page_start = (Button) findViewById(R.id.btn_page_start);
    	btn_page_end = (Button) findViewById(R.id.btn_page_end);
    	btn_to_activity = (Button) findViewById(R.id.btn_to_activity);
    	
    	tv_show.setText("当前页面：TestActivity2 ");
    	btn_duration.setOnClickListener(this);
    	btn_duration_label.setOnClickListener(this);
    	btn_duration_hashmap.setOnClickListener(this);
    	btn_page_start.setOnClickListener(this);
    	btn_page_end.setOnClickListener(this);
    	btn_to_activity.setOnClickListener(this);
    }

    private void initData(){
    }
    
    @Override
    public void onResume(){
    	super.onResume();
		MobclickAgent.onPageStart("pageActivity");
    	MobclickAgent.onResume(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
		MobclickAgent.onPageEnd("pageActivity");
    	MobclickAgent.onPause(this);
    }
    
    @Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_duration:
				MobclickAgent.onEventDuration(context, "event_duration_sleep", 180);
				break;
			case R.id.btn_duration_label:
				MobclickAgent.onEventDuration(context, "event_duration_reading", "I love you!", 3000);	
				break;
			case R.id.btn_duration_hashmap:
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("duration_tomato", "tomato");
				map.put("duration_cabbage", "15");
				map.put("duration_potato", "cabbage");
				map.put("duration_carrot", "potato");
				map.put("duration_bean", "bean");
				map.put("duration_rabit", "33");
				map.put("duration_snake", "44");
				map.put("duration_tiger", "55");
				map.put("duration_goat", "22");
				map.put("duration_leopard", "11");
				MobclickAgent.onEventDuration(context, "event_duration_fruit_hash", 105, map);	
				break;
			case R.id.btn_page_start:
				break;
			case R.id.btn_page_end:
				break;
			case R.id.btn_to_activity:
				Intent i = new Intent(context, MainActivity.class);
				startActivity(i);
				this.finish();
				break;
			default:
				break;
		}
		
	}
    
}
