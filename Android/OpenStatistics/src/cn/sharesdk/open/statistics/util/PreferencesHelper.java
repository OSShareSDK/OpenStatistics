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
package cn.sharesdk.open.statistics.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

public class PreferencesHelper {


	/** wifi send 0 */
	public final int WIFI_SEND_POLICY = 0;
	/** launch send 1 */
	public final int LAUNCH_SEND_POLICY = 1;
	/** delay send 2 */
	public final int DELAY_SEND_POLICY = 2;

	private static Context context;
	private static String packageName;
	private static PreferencesHelper dbHelper;

	private PreferencesHelper(Context mContext) {
		context = mContext.getApplicationContext();
		packageName = context.getPackageName();
	}

	public static synchronized PreferencesHelper getInstance(Context mContext) {
		if (dbHelper == null && mContext != null) {
			dbHelper = new PreferencesHelper(mContext);
		}
		return dbHelper;
	}

	/** get sdcard path */
	private static String getSdcardPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	/**
	 * get event info from cached file
	 */
	@Deprecated
	public String GetInfoFromFile() {
		FileInputStream in;
		try {
			File cacheRoot = new File(getSdcardPath(), packageName);
			if (!cacheRoot.exists()) {
				return null;
			}

			File cacheFile = new File(cacheRoot, "mobclick_agent_cached_" + packageName);
			if (!cacheFile.exists()) {
				return null;
			}

			in = new FileInputStream(cacheFile);
			StringBuffer sb = new StringBuffer();

			int i = 0;
			byte[] s = new byte[1024 * 4];

			while ((i = in.read(s)) != -1) {
				sb.append(new String(s, 0, i));
			}

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * save events' info to cached file
	 */
	@Deprecated
	public void saveInfoToFile(String key, JSONObject object) {
		JSONObject existJSON = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && DeviceHelper.getInstance(context).checkPermissions("android.permission.WRITE_EXTERNAL_STORAGE")) {
				// Does a cached file exist
				File cacheRoot = new File(getSdcardPath(), packageName);
				if (!cacheRoot.exists()) {
					cacheRoot.mkdirs();
					Ln.i("cacheRoot path", "no path");
				}

				File cacheFile = new File(cacheRoot, "mobclick_agent_cached_" + packageName);
				if (!cacheFile.exists()) {
					cacheFile.createNewFile();
					Ln.i("cacheFile path", "no path createNewFile");
				}
				
				// Does any data in the cached file
				FileInputStream in = new FileInputStream(cacheFile);
				StringBuffer sb = new StringBuffer();

				int i = 0;
				byte[] s = new byte[1024 * 4];

				while ((i = in.read(s)) != -1) {
					sb.append(new String(s, 0, i));
				}
				in.close();

				if (sb.length() != 0) {
					existJSON = new JSONObject(sb.toString());
				} else {
					existJSON = new JSONObject();
				}

				if (existJSON.has(key)) {
					JSONArray newDataArray = existJSON.getJSONArray(key);
					Ln.i("SaveInfo", object + "");
					newDataArray.put(object);
				} else {
					JSONArray newArray = new JSONArray();
					newArray.put(0, object);
					existJSON.put(key, newArray);
					Ln.i("SaveInfo", "jsonobject" + existJSON);
				}
				
				Ln.i("SaveInfo", "save json data to the cached file!");
				// save json data to the cached file
				FileOutputStream fileOutputStream = new FileOutputStream(cacheFile, false);
				fileOutputStream.write(existJSON.toString().getBytes());
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/**
	 * delete cached file
	 */
	@Deprecated
	public boolean deleteCacheFile() {
		File cacheRoot = new File(getSdcardPath(), packageName);
		if (!cacheRoot.exists()) {
			return false;
		}

		File cacheFile = new File(cacheRoot, "mobclick_agent_cached_" + packageName);
		if (!cacheFile.exists()) {
			return false;
		} else {
			cacheFile.delete();
			return true;
		}

	}

	/**
	 * Setting data transmission mode
	 * 
	 * @param reportModel
	 * 
	 *            value: 0 post log anytime, 1 post log at app launch; 2 post at
	 *            delay time
	 */
	public void setReportPolicy(int reportModel, int delay) {
		Ln.i("setReportPolicy === delay time >>", reportModel + "==" + delay);
		SharedPreferences localSharedPreferences = context.getSharedPreferences("mobclick_agent_online_setting_" + packageName, 0);
		Editor editor = localSharedPreferences.edit();
		editor.putInt("policy", reportModel);
		editor.putInt("delay", delay);
		editor.commit();
	}

	/**
	 * Get the current send model
	 */
	public int getReportPolicy() {
		SharedPreferences localSharedPreferences = context.getSharedPreferences("mobclick_agent_online_setting_" + packageName, 0);
		int policy = localSharedPreferences.getInt("policy", WIFI_SEND_POLICY);
		return policy;
	}

	/**
	 * Get the time of report delay millseconds
	 */
	public int getReportDelay() {
		SharedPreferences localSharedPreferences = context.getSharedPreferences("mobclick_agent_online_setting_" + packageName, 0);
		int duration = localSharedPreferences.getInt("delay", 10 * 1000);
		return duration;
	}

	/** save post log url */
	public void setReportApiPath(String apiPath) {
		Ln.i("setReportApiPath ==>> ", apiPath);
		SharedPreferences preferences = context.getSharedPreferences("mobclick_agent_online_setting_" + packageName, 0);
		Editor editor = preferences.edit();
		editor.putString("apiPath", apiPath);
		editor.commit();
	}

	/**
	 * Get post-log-url
	 */
	public String getReportApiPath() {
		SharedPreferences localSharedPreferences = context.getSharedPreferences("mobclick_agent_online_setting_" + packageName, 0);
		String apiPath = localSharedPreferences.getString("apiPath", null);
		return apiPath;
	}

	/** save session time */
	public void setSessionTime() {
		SharedPreferences preferences2sessiontime = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		Editor editor = preferences2sessiontime.edit();
		long currenttime = System.currentTimeMillis();
		editor.putLong("session_save_time", currenttime);
		editor.commit();
	}

	/** get session time */
	public long getSessionTime() {
		SharedPreferences preferences2sessiontime = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		return preferences2sessiontime.getLong("session_save_time", 0);
	}

	/** save session id */
	public void setSessionID(String sessionId) {
		SharedPreferences preferences = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putString("session_id", sessionId);
		edit.commit();
	}

	/** get session id */
	public String getSessionID() {
		SharedPreferences preferences = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		return preferences.getString("session_id", null);
	}

	/** save the time of post log */
	public void setLastReportTime(long time) {
		SharedPreferences preferences = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putLong("last_report_time", time);
		edit.commit();
	}

	/** get the time of last post log */
	public long getLastReportTime() {
		SharedPreferences preferences = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		return preferences.getLong("last_report_time", 0);
	}
	
	/** save session time */
	public void setAppExitDate() {
		SharedPreferences preferences2sessiontime = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		Editor editor = preferences2sessiontime.edit();
		Date date = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = localSimpleDateFormat.format(date);
		editor.putString("app_exit_date", dateStr);
		editor.commit();
	}

	/** get session time */
	public String getAppExitDate() {
		SharedPreferences preferences2sessiontime = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		return preferences2sessiontime.getString("app_exit_date", "0");
	}
	
	/** save session time */
	public void setAppStartDate() {
		SharedPreferences preferences2sessiontime = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		Editor editor = preferences2sessiontime.edit();
		Date date = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = localSimpleDateFormat.format(date);
		editor.putString("app_start_date", dateStr);
		editor.commit();
	}

	/** get session time */
	public String getAppStartDate() {
		SharedPreferences preferences2sessiontime = context.getSharedPreferences("mobclick_agent_state_" + packageName, Context.MODE_PRIVATE);
		return preferences2sessiontime.getString("app_start_date", "0");
	}
	

	public void saveEInfoToFile(JSONObject existJSON) {
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && DeviceHelper.getInstance(context).checkPermissions("android.permission.WRITE_EXTERNAL_STORAGE")) {
				// Does a cached file exist
				File cacheRoot = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), packageName);
				if (!cacheRoot.exists()) {
					cacheRoot.mkdirs();
					Ln.i("cacheRoot path", "no path");
				}

				File cacheFile = new File(cacheRoot, "mobclick_agent_json_cached_" + packageName);
				if (!cacheFile.exists()) {
					cacheFile.createNewFile();
					Ln.i("cacheFile path", "no path createNewFile");
				}
				
				// save json data to the cached file
				FileOutputStream fileOutputStream = new FileOutputStream(cacheFile, false);
				fileOutputStream.write(existJSON.toString().getBytes());
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}
}
