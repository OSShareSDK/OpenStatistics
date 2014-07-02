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

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import cn.sharesdk.analysis.MobclickAgent;
import cn.sharesdk.analysis.MobclickAgentJSInterface;

public class WebviewAnalytic extends Activity {
	private final String mPageName = "WebViewPage";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sharesdk_example_webview);

		WebView webview = (WebView) findViewById(R.id.webview);
		// important , so that you can use js to call Uemng APIs
		new MobclickAgentJSInterface(this, webview, new WebChromeClient());
		webview.loadUrl("file:///android_asset/demo.html");
	}

	@Override
	protected void onPause() {
		super.onPause();
		// SDK已经禁用了基于Activity 的页面统计，�?���?��再次重新统计页面
		MobclickAgent.onPageEnd(mPageName);
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// SDK已经禁用了基于Activity 的页面统计，�?���?��再次重新统计页面
		MobclickAgent.onPageStart(mPageName);
		MobclickAgent.onResume(this);
	}

}
