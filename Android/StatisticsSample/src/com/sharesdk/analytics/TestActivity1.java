package com.sharesdk.analytics;

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

public class TestActivity1 extends Activity implements OnClickListener{
	
	Context context;
	Button btn_event_begin;
	Button btn_event_end;
	Button btn_event_begin_label;
	Button btn_event_end_label;
	Button btn_error;
	Button btn_to_activity;
	TextView tv_show;

	//event id
	final String event_begin_duration_id = "begin_duration_";
	final String event_begin_duration_label_id = "begin_duration_label_";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        
        initView();
        initData();
      
    }

    private void initView(){
    	context = TestActivity1.this;
    	btn_event_begin = (Button) findViewById(R.id.btn_event_begin);
    	btn_event_end = (Button) findViewById(R.id.btn_event_end);
    	btn_event_begin_label = (Button) findViewById(R.id.btn_event_begin_label);
    	btn_event_end_label = (Button) findViewById(R.id.btn_event_end_label);
    	btn_error = (Button) findViewById(R.id.btn_error);
    	btn_to_activity = (Button) findViewById(R.id.btn_to_activity);
    	tv_show = (TextView)findViewById(R.id.tv_show);
    	
    	btn_event_begin.setOnClickListener(this);
    	btn_event_end.setOnClickListener(this);
    	btn_event_begin_label.setOnClickListener(this);
    	btn_event_end_label.setOnClickListener(this);
    	btn_error.setOnClickListener(this);
    	btn_to_activity.setOnClickListener(this);
    	tv_show.setText("当前页面的名字是"+this.getClass().getName());
    }
    
    private void initData(){}
    
    @Override
    public void onResume(){
    	super.onResume();
    	MobclickAgent.onResume(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	MobclickAgent.onPause(this);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_event_begin:
				MobclickAgent.onEventBegin(context, event_begin_duration_id + Constant.getRandomEventEnd());
				break;
			case R.id.btn_event_end:
				MobclickAgent.onEventEnd(context, event_begin_duration_id + Constant.getRandomEventEnd());
				break;
			case R.id.btn_event_begin_label:
				MobclickAgent.onEventBegin(context, event_begin_duration_label_id + Constant.getRandomEventEnd());
				break;
			case R.id.btn_event_end_label:
				MobclickAgent.onEventEnd(context, event_begin_duration_label_id + Constant.getRandomEventEnd());
				break;			
			case R.id.btn_error:
				MobclickAgent.onError(context, "Test error msg <* _ *> " + Constant.getRandomEventEnd());
				break;
			case R.id.btn_to_activity:
				Intent i = new Intent(TestActivity1.this, TestActivity2.class);
				startActivity(i);
				break;
			default:
				break;
		}
		
	}
    
}
