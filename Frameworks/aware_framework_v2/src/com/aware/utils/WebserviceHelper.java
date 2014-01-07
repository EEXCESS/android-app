/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;

public class WebserviceHelper extends IntentService {
	
	public static final String ACTION_AWARE_WEBSERVICE_SYNC_TABLE = "ACTION_AWARE_WEBSERVICE_SYNC_TABLE";
	public static final String ACTION_AWARE_WEBSERVICE_CLEAR_TABLE = "ACTION_AWARE_WEBSERVICE_CLEAR_TABLE";
	
	public static final String EXTRA_TABLE = "table";
	public static final String EXTRA_FIELDS = "fields";
	public static final String EXTRA_CONTENT_URI = "uri";

	private String WEBSERVER = "";
	private String DEVICE_ID = "";
	private boolean DEBUG = false;
	
	public WebserviceHelper() {
		super(Aware.TAG + " Webservice Sync");
	}

	private boolean exists( String[] array, String find ) {
		for( String a : array ) {
			if( a.equals(find) ) return true;
		}
		return false;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		WEBSERVER = Aware.getSetting(getContentResolver(), Aware_Preferences.WEBSERVICE_SERVER);
		DEVICE_ID = Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID);
		DEBUG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_FLAG).equals("true");
		
		if( intent.getAction().equals(ACTION_AWARE_WEBSERVICE_SYNC_TABLE) ) {
			
			String DATABASE_TABLE = intent.getStringExtra(EXTRA_TABLE);
			String TABLES_FIELDS = intent.getStringExtra(EXTRA_FIELDS);
			Uri CONTENT_URI = Uri.parse(intent.getStringExtra(EXTRA_CONTENT_URI));
			
			//Check first if we have database table remotely, otherwise create it!
			ArrayList<NameValuePair> fields = new ArrayList<NameValuePair>();
    		fields.add(new BasicNameValuePair(Aware_Preferences.DEVICE_ID, DEVICE_ID));
    		fields.add(new BasicNameValuePair(EXTRA_FIELDS, TABLES_FIELDS));
    		
    		//Create table if doesn't exist on the remote webservice server
    		HttpResponse response = new Http().dataPOST(WEBSERVER + "/" + DATABASE_TABLE + "/create_table", fields);
    		if( response != null && response.getStatusLine().getStatusCode() == 200 ) {
    		    
    		    if( DEBUG ) {
                    HttpResponse copy = response;
                    try {
                        Log.d(Aware.TAG, EntityUtils.toString(copy.getEntity()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
    			
    			String[] columnsStr = new String[]{};
    			Cursor columnsDB = getContentResolver().query(CONTENT_URI, null, null, null, null);
    			if(columnsDB != null && columnsDB.moveToFirst()) {
    				columnsStr = columnsDB.getColumnNames();
    			}
    			if( columnsDB != null && ! columnsDB.isClosed() ) columnsDB.close();
    			
				try {
					ArrayList<NameValuePair> request = new ArrayList<NameValuePair>();
    				request.add(new BasicNameValuePair(Aware_Preferences.DEVICE_ID, DEVICE_ID));
    				
    				//check the latest entry in remote database
    				HttpResponse latest = new Http().dataPOST(WEBSERVER + "/" + DATABASE_TABLE + "/latest", request);
    				if( latest == null ) return;
    				
    				String data = EntityUtils.toString(latest.getEntity());
    				if( DEBUG ) { 
    					Log.d(Aware.TAG,"Webservice response: " + data );
    				}
    				
    				Cursor context_data = null;
    				
					JSONArray remoteData = new JSONArray(data);
					if( remoteData.length() == 0 ) {
						if( exists(columnsStr, "double_end_timestamp") ) {
							context_data = getContentResolver().query(CONTENT_URI, null, "double_end_timestamp != 0", null, "timestamp ASC");
						} else if (exists(columnsStr, "double_esm_user_answer_timestamp")) {
							context_data = getContentResolver().query(CONTENT_URI, null, "double_esm_user_answer_timestamp != 0", null, "timestamp ASC");
						} else {
							context_data = getContentResolver().query(CONTENT_URI, null, null, null, "timestamp ASC");
						}
					} else {
						long last = 0;
						
						if ( exists(columnsStr, "double_end_timestamp") ) {
							last = remoteData.getJSONObject(0).getLong("double_end_timestamp");
							context_data = getContentResolver().query(CONTENT_URI, null, "timestamp > " + last + " AND double_end_timestamp != 0", null, "timestamp ASC");
						} else if( exists(columnsStr, "double_esm_user_answer_timestamp") ) {
							last = remoteData.getJSONObject(0).getLong("double_esm_user_answer_timestamp");
							context_data = getContentResolver().query(CONTENT_URI, null, "timestamp > " + last + " AND double_esm_user_answer_timestamp != 0", null, "timestamp ASC");
						} else {
							last = remoteData.getJSONObject(0).getLong("timestamp");
							context_data = getContentResolver().query(CONTENT_URI, null, "timestamp > " + last, null, "timestamp ASC");
						}
					}
					
					JSONArray context_data_entries = new JSONArray();
					
					if( context_data != null && context_data.moveToFirst() ) {
						if( DEBUG ) Log.d(Aware.TAG, "Uploading " + context_data.getCount() + " from " + DATABASE_TABLE);
						
						do {
							
							JSONObject entry = new JSONObject();
							
							String[] columns = context_data.getColumnNames();
							for(String c_name : columns) {
								
								//Skip local database ID
								if( c_name.equals("_id") ) continue;
								
								if( c_name.equals("timestamp") || c_name.contains("double") ) {
									entry.put(c_name, context_data.getDouble(context_data.getColumnIndex(c_name)));
								} else if (c_name.contains("float")) {
									entry.put(c_name, context_data.getFloat(context_data.getColumnIndex(c_name)));
								} else if (c_name.contains("long")) {
									entry.put(c_name, context_data.getLong(context_data.getColumnIndex(c_name)));
								} else if (c_name.contains("blob")) {
									entry.put(c_name, context_data.getBlob(context_data.getColumnIndex(c_name)));
								} else if (c_name.contains("integer")) {
									entry.put(c_name, context_data.getInt(context_data.getColumnIndex(c_name)));
								} else {
									entry.put(c_name, context_data.getString(context_data.getColumnIndex(c_name)));
								}
							}
							context_data_entries.put(entry);
							
							if( context_data_entries.length() == 1000 ) {
								request = new ArrayList<NameValuePair>();
								request.add(new BasicNameValuePair(Aware_Preferences.DEVICE_ID, DEVICE_ID));
								request.add(new BasicNameValuePair("data", context_data_entries.toString()));
								new Http().dataPOST( WEBSERVER + "/" + DATABASE_TABLE + "/insert", request);
								
								context_data_entries = new JSONArray();
							}
						} while ( context_data.moveToNext() );
						
						if( context_data_entries.length() > 0 ) {
							request = new ArrayList<NameValuePair>();
							request.add(new BasicNameValuePair(Aware_Preferences.DEVICE_ID, DEVICE_ID));
							request.add(new BasicNameValuePair("data", context_data_entries.toString()));
							new Http().dataPOST( WEBSERVER + "/" + DATABASE_TABLE+"/insert", request);
						}
					} else {
						if( DEBUG ) Log.d(Aware.TAG, "Nothing new in " + DATABASE_TABLE +"!" );
					}
					
					if( context_data != null && ! context_data.isClosed() ) context_data.close();
					
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
		}
		
		if( intent.getAction().equals(ACTION_AWARE_WEBSERVICE_CLEAR_TABLE) ) {
			String DATABASE_TABLE = intent.getStringExtra(EXTRA_TABLE);
			
			//Clear database table remotely
			ArrayList<NameValuePair> request = new ArrayList<NameValuePair>();
    		request.add(new BasicNameValuePair(Aware_Preferences.DEVICE_ID, DEVICE_ID));
    		
    		//Create table if doesn't exist on the remote webservice server
    		new Http().dataPOST(WEBSERVER + "/" + DATABASE_TABLE + "/clear_table", request);
		}
	}
}
