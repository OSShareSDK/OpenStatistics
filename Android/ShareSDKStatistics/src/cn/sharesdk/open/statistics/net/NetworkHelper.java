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
package cn.sharesdk.open.statistics.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;
import android.util.Base64;
import cn.sharesdk.open.statistics.model.PostResult;
import cn.sharesdk.open.statistics.util.Ln;

public class NetworkHelper {

	public static String Base64Gzip(String str) {
		ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		String result = null;
		// gzip
		GZIPOutputStream gos;
		try {
			gos = new GZIPOutputStream(baos);
			int count;
			byte data[] = new byte[1024];
			while ((count = bais.read(data, 0, 1024)) != -1) {
				gos.write(data, 0, count);
			}
			gos.finish();
			gos.close();

			byte[] output = baos.toByteArray();
			baos.flush();
			baos.close();
			bais.close();

			result = Base64.encodeToString(output, Base64.NO_WRAP);

		} catch (IOException e) {
			e.printStackTrace();
			Ln.e("NetworkHelper", "Base64Gzip == >>", e);
		}

		Ln.i("after base64gizp", result);
		return result;
	}

	public static PostResult post(String url, String data, String appkey) {

		Ln.i(" post msg url ==>>", url);
		Ln.i(" post msg appkey ==>>", appkey);
		
		PostResult postResult = new PostResult();
		if(TextUtils.isEmpty(appkey)){
			postResult.setSuccess(false);
			postResult.setResponseMsg("appkey is null");
			return postResult;
		}
		HttpPost httppost = new HttpPost(url);
		HttpClient httpclient = new DefaultHttpClient();
		try {
			Ln.i("postdata before base64gizp", "client_data:" + data);

			ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("appkey", appkey));
			if (!TextUtils.isEmpty(data)) {
				data = Base64Gzip(data);
				pairs.add(new BasicNameValuePair("m", data));
			}
			HttpEntity entity = new UrlEncodedFormEntity(pairs, HTTP.UTF_8);
			httppost.addHeader("charset", HTTP.UTF_8);
			httppost.setHeader("Accept", "application/json,text/x-json,application/jsonrequest,text/json");
			httppost.setEntity(entity);

			HttpResponse response = httpclient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();
			
			String resString = EntityUtils.toString(response.getEntity());
			postResult = parse(status, resString);

		} catch (Exception e) {
			Ln.e("NetworkHelper", "=== post Ln ===", e);
		}
		return postResult;
	}

	private static PostResult parse(int status, String response) {

		PostResult message = new PostResult();
		Ln.w("post result status == >>", status + "");
		String returnContent = URLDecoder.decode(response);
		Ln.i("response content decode==>>", returnContent);
		switch (status) {
		case 200:
			message.setSuccess(true);
			message.setResponseMsg(returnContent);
			break;
		default:
			Ln.e("error ==>>", status + returnContent);
			message.setSuccess(false);
			message.setResponseMsg(returnContent);
			break;
		}
		return message;
	}

}
