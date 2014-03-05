/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;

/**
 * Aware_Sensor: Extend to integrate with the framework (extension of Android Service class).
 * @author dferreira
 *
 */
public class Aware_Sensor extends Service {
	
	/**
	 * Debug tag for this sensor
	 */
	public static String TAG = "AWARE Sensor";
	
	/**
	 * Debug flag for this sensor
	 */
	public static boolean DEBUG = false;
	
	public ContextProducer CONTEXT_PRODUCER = null;
	
	/**
	 * Sensor database tables
	 */
	public String[] DATABASE_TABLES = null;
	
	/**
	 * Sensor table fields
	 */
	public String[] TABLES_FIELDS = null;
	
	/**
	 * Context Providers URIs
	 */
	public Uri[] CONTEXT_URIS = null;
	
	/**
	 * Sensor is inactive
	 */
	public static final int STATUS_SENSOR_OFF = 0;
	
	/**
	 * Sensor is active
	 */
	public static final int STATUS_SENSOR_ON = 1;
	
	/**
     * Interface to share context with other applications/addons<br/>
     * You MUST broadcast your contexts here!
     * @author denzil
     */
    public interface ContextProducer {
    	public void onContext();
    }
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	
    	TAG = Aware.getSetting(getContentResolver(),Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG):TAG;
        DEBUG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_FLAG).equals("true")?true:DEBUG;
        
        if( DEBUG ) Log.d(TAG, TAG + " sensor created!");
        
        //Register Context Broadcaster
        IntentFilter filter = new IntentFilter();
        filter.addAction(Aware.ACTION_AWARE_CURRENT_CONTEXT);
        filter.addAction(Aware.ACTION_AWARE_WEBSERVICE);
        filter.addAction(Aware.ACTION_AWARE_CLEAN_DATABASES);
        filter.addAction(Aware.ACTION_AWARE_STOP_SENSORS);
        registerReceiver(contextBroadcaster, filter);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	//Unregister Context Broadcaster
        unregisterReceiver(contextBroadcaster);
        
        if(DEBUG) Log.d(TAG, TAG + " sensor terminated...");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	TAG = Aware.getSetting(getContentResolver(),Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(),Aware_Preferences.DEBUG_TAG):TAG;
        DEBUG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_FLAG).equals("true")?true:false;
        if(DEBUG) Log.d(TAG, TAG + " sensor active...");
        return START_STICKY;
    }
    
	/**
     * AWARE Context Broadcaster<br/>
     * - ACTION_AWARE_CURRENT_CONTEXT: returns current plugin's context
     * - ACTION_AWARE_WEBSERVICE: push content provider data remotely
     * - ACTION_AWARE_CLEAN_DATABASES: clears local and remote database
     * - ACTION_AWARE_STOP_SENSORS: stops this sensor
     * @author denzil
     */
    public class ContextBroadcaster extends BroadcastReceiver {     
    	@Override
        public void onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(Aware.ACTION_AWARE_CURRENT_CONTEXT) ) {
                if( CONTEXT_PRODUCER != null ) {
                    CONTEXT_PRODUCER.onContext();
                }
            }
            if( intent.getAction().equals(Aware.ACTION_AWARE_WEBSERVICE) 
            		&& Aware.getSetting(context.getContentResolver(), Aware_Preferences.STATUS_WEBSERVICE).equals("true") ) {
            	if( DATABASE_TABLES != null && TABLES_FIELDS != null && CONTEXT_URIS != null) {
            		for( int i=0; i<DATABASE_TABLES.length; i++ ) {
            			Intent webserviceHelper = new Intent(WebserviceHelper.ACTION_AWARE_WEBSERVICE_SYNC_TABLE);
            			webserviceHelper.putExtra(WebserviceHelper.EXTRA_TABLE, DATABASE_TABLES[i]);
                		webserviceHelper.putExtra(WebserviceHelper.EXTRA_FIELDS, TABLES_FIELDS[i]);
                		webserviceHelper.putExtra(WebserviceHelper.EXTRA_CONTENT_URI, CONTEXT_URIS[i].toString());
                		context.startService(webserviceHelper);
            		}
            	} else {
            		if( Aware.DEBUG ) Log.d(TAG,"No database to backup!");
            	}
            }
            if( intent.getAction().equals(Aware.ACTION_AWARE_CLEAN_DATABASES)) {
            	if( DATABASE_TABLES != null && CONTEXT_URIS != null ) {
            		for( int i=0; i<DATABASE_TABLES.length; i++) {
	            		//Clear locally
	            		context.getContentResolver().delete(CONTEXT_URIS[i], null, null);
	            		if( Aware.DEBUG ) Log.d(TAG,"Cleared " + CONTEXT_URIS[i].toString());
	            		
	            		//Clear remotely
	            		if( Aware.getSetting(context.getContentResolver(), Aware_Preferences.STATUS_WEBSERVICE).equals("true") ) {
		            		Intent webserviceHelper = new Intent(WebserviceHelper.ACTION_AWARE_WEBSERVICE_CLEAR_TABLE);
		            		webserviceHelper.putExtra(WebserviceHelper.EXTRA_TABLE, DATABASE_TABLES[i]);
		            		context.startService(webserviceHelper);
	            		}
            		}
            	}
            }
            if(intent.getAction().equals(Aware.ACTION_AWARE_STOP_SENSORS)) {
                if( Aware.DEBUG ) Log.d(TAG, TAG + " stopped");
                stopSelf();
            }
        }
    }
    private ContextBroadcaster contextBroadcaster = new ContextBroadcaster();
    
    @Override
    public IBinder onBind(Intent intent) {
    	return null;
    }
}
