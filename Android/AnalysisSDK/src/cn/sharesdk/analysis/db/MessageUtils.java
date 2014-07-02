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
package cn.sharesdk.analysis.db;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import cn.sharesdk.analysis.util.Ln;

public class MessageUtils {

	public static int WIFI = 0;
	public static int MOBILE_3G = 1;
	public static int NONE = 2;
	
	public static final String DEVICE_DATA = "device_data";
	public static final String LAUNCH_DATA = "launch_data";
	public static final String EXIT_DATA = "exit_data";
	public static final String ERROR_DATA = "error_data";
	public static final String EVENT_DATA = "event_data";
	public static final String HASH_EVENT_DATA = "eventkv_data";
	public static final String PAGE_DATA = "page_data";
	
	/**
	 * 插入统计消息
	 * @param c
	 * @param msg
	 */
	public static synchronized long insertMsg(Context c, String type, String data) {
		if(TextUtils.isEmpty(data)){
			return -1;
		}
		DBProvider provider = DBProvider.getDBProvider(c);
		ContentValues value = new ContentValues();
		value.put(DBHelp.COLUMN_EVENT_TYPE, type);
		value.put(DBHelp.COLUMN_EVENT_DATA, data);
		
		long id = provider.insert(DBHelp.TABLE_STATISTICS, value);
		//Ln.e("threadID == %s, insert ID == %s ", time , id);
		return id;
		
	}

	/**
	 * delect msg by id
	 * @param c
	 * @param id
	 * @return
	 */
	public static synchronized long deleteMsgByID(Context c, String id) {
		DBProvider provider = DBProvider.getDBProvider(c);
		int deleteCount = provider.delete(DBHelp.TABLE_STATISTICS, DBHelp.COLUMN_ID+"= ?", new String[]{id});	
		//Ln.e("delete id == %s", deleteCount);	
		return deleteCount;
	}

	/**
	 * delete msg by time 
	 * @param c
	 * @param time
	 * @return
	 */
	public static long deleteMsgByType(Context c, String type) {
		DBProvider provider = DBProvider.getDBProvider(c);
		int deleteCount = provider.delete(DBHelp.TABLE_STATISTICS, DBHelp.COLUMN_EVENT_TYPE+"= ?", new String[]{type});		
		//Ln.e("delete id == %s", deleteCount);
		return deleteCount;
	}
	
	/**
	 * delect msg by id
	 * @param c
	 * @param id
	 * @return
	 */
	public static synchronized long deleteManyMsg(Context c,  ArrayList<String> idList) {
		StringBuilder buider = new StringBuilder();
		for(int i=0; i<idList.size(); i++){
			buider.append("'");
			buider.append(idList.get(i));
			buider.append("'");
			buider.append(",");
		}		
		String list = buider.toString().substring(0, buider.length()-1);
		
		DBProvider provider = DBProvider.getDBProvider(c);
		int deleteCount = provider.delete(DBHelp.TABLE_STATISTICS, DBHelp.COLUMN_ID+" in ( "+list+" )", null);	
		//Ln.e("delete COUNT == %s", deleteCount);	
		return deleteCount;
	}
	
	/**
	 * 获取wifi状态下的数据*/
	private static synchronized ArrayList<MessageModel> getEventMsg(Context c, String selection, String[] selectionArgs) {
		
		ArrayList<MessageModel> group = new ArrayList<MessageModel>();
		MessageModel model = new MessageModel();		
		JSONObject jsonObject = new JSONObject();
		
		String eventID;
		String eventType;
		DBProvider provider = DBProvider.getDBProvider(c);
		Cursor cursor = provider.query(DBHelp.TABLE_STATISTICS, new String[] { DBHelp.COLUMN_ID, DBHelp.COLUMN_EVENT_TYPE, DBHelp.COLUMN_EVENT_DATA }, selection, selectionArgs, null);
		try{
			while (cursor != null && cursor.moveToNext()) {
				
				eventID = cursor.getString(0);
				eventType = cursor.getString(1);
				JSONObject eventObject = new JSONObject(cursor.getString(2).toString());
				model.idList.add(eventID);
				if (jsonObject.has(eventType)) {
					JSONArray existArray = jsonObject.getJSONArray(eventType);
					existArray.put(eventObject);
				} else {
					JSONArray newArray = new JSONArray();
					newArray.put(0, eventObject);
					jsonObject.put(eventType, newArray);
				}
				
				if(model.idList.size() == 50){
					//第一百个数据时,不换行
					model.data = jsonObject.toString();
					group.add(model);
					model = new MessageModel();
					jsonObject = new JSONObject();
					continue;
				}
			}
			cursor.close();
			
			//如果不是10的整数倍，注意要去掉最后“\n”
			if(model.idList.size() != 0){
				model.data = jsonObject.toString();
				group.add(model);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return group;
		
	}
	
	/**
	 * get msg by network state of wifi
	 * @param context
	 * @return
	 */
	public static synchronized ArrayList<MessageModel> getEventMsg(Context c) {
		
		DBProvider provider = DBProvider.getDBProvider(c);
		int msgCount = provider.getCount(DBHelp.TABLE_STATISTICS);
		Ln.e("db get message count ==>>", msgCount+"");
		if (msgCount > 0) {
			return getEventMsg(c, null, null);
		}else{
			// 无数据
			return new ArrayList<MessageModel>();
		}
		
	}
	
}
