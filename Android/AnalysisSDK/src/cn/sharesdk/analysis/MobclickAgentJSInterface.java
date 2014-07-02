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

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MobclickAgentJSInterface {
	private Context context;

	public MobclickAgentJSInterface(Context paramContext, WebView paramWebView) {
		this.context = paramContext;
		paramWebView.getSettings().setJavaScriptEnabled(true);
		paramWebView.setWebChromeClient(new StatisticsWebClient(null));
	}

	public MobclickAgentJSInterface(Context paramContext, WebView paramWebView, WebChromeClient paramWebChromeClient) {
		this.context = paramContext;
		paramWebView.getSettings().setJavaScriptEnabled(true);
		paramWebView.setWebChromeClient(new StatisticsWebClient(paramWebChromeClient));
	}

	final class StatisticsWebClient extends WebChromeClient {
		WebChromeClient webClient = null;

		public StatisticsWebClient(WebChromeClient client) {
			if (client == null)
				this.webClient = new WebChromeClient();
			else
				this.webClient = client;
		}

		@SuppressWarnings("rawtypes")
		public boolean onJsPrompt(WebView paramWebView, String paramString1, String paramString2, String paramString3, JsPromptResult paramJsPromptResult) {
			JSONObject localJSONObject = null;

			if ("ekv".equals(paramString2)) {
				try {
					localJSONObject = new JSONObject(paramString3);

					HashMap<String, String> localHashMap = new HashMap<String, String>();

					String id = (String) localJSONObject.remove("id");

					int i = localJSONObject.isNull("duration") ? 0 : ((Integer) localJSONObject.remove("duration")).intValue();

					Iterator localIterator = localJSONObject.keys();

					String str3 = null;
					while (localIterator.hasNext()) {
						localHashMap.put(str3 = (String) localIterator.next(), localJSONObject.getString(str3));
					}

					MobclickAgent.onEventDuration(context, id, i, localHashMap);
				} catch (Exception localException1) {
					localException1.printStackTrace();
				}
			} else if ("event".equals(paramString2))
				try {
					localJSONObject = new JSONObject(paramString3);
					String str1 = localJSONObject.optString("label");

					if ("".equals(str1)) {
						str1 = null;
					}

					MobclickAgent.onEventDuration(context, localJSONObject.getString("tag"), str1, localJSONObject.optInt("duration"));
				} catch (Exception localException2) {
				}
			else
				return this.webClient.onJsPrompt(paramWebView, paramString1, paramString2, paramString3, paramJsPromptResult);

			paramJsPromptResult.confirm();
			return true;
		}

		public void onCloseWindow(WebView paramWebView) {
			this.webClient.onCloseWindow(paramWebView);
		}

		public boolean onCreateWindow(WebView paramWebView, boolean paramBoolean1, boolean paramBoolean2, Message paramMessage) {
			return this.webClient.onCreateWindow(paramWebView, paramBoolean1, paramBoolean2, paramMessage);
		}

		public boolean onJsAlert(WebView paramWebView, String paramString1, String paramString2, JsResult paramJsResult) {
			return this.webClient.onJsAlert(paramWebView, paramString1, paramString2, paramJsResult);
		}

		public boolean onJsBeforeUnload(WebView paramWebView, String paramString1, String paramString2, JsResult paramJsResult) {
			return this.webClient.onJsBeforeUnload(paramWebView, paramString1, paramString2, paramJsResult);
		}

		public boolean onJsConfirm(WebView paramWebView, String paramString1, String paramString2, JsResult paramJsResult) {
			return this.webClient.onJsConfirm(paramWebView, paramString1, paramString2, paramJsResult);
		}

		public void onProgressChanged(WebView paramWebView, int paramInt) {
			this.webClient.onProgressChanged(paramWebView, paramInt);
		}

		public void onReceivedIcon(WebView paramWebView, Bitmap paramBitmap) {
			this.webClient.onReceivedIcon(paramWebView, paramBitmap);
		}

		public void onReceivedTitle(WebView paramWebView, String paramString) {
			this.webClient.onReceivedTitle(paramWebView, paramString);
		}

		public void onRequestFocus(WebView paramWebView) {
			this.webClient.onRequestFocus(paramWebView);
		}
	}
}
