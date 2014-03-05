/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware.ui;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;

import com.aware.Aware;
import com.aware.ESM;
import com.aware.providers.ESM_Provider.ESM_Data;

/**
 * Processes an  ESM queue until it's over.
 * @author denzilferreira
 */
public class ESM_Queue extends FragmentActivity {
    
    private static String TAG = "AWARE::ESM Queue";
    private final ESM_QueueManager queue_manager = new ESM_QueueManager();
    
    private PowerManager powerManager = null;
    private Vibrator vibrator = null;
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        
        TAG = Aware.getSetting(getContentResolver(),"debug_tag").length()>0?Aware.getSetting(getContentResolver(),"debug_tag"):TAG;
        
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
        IntentFilter filter = new IntentFilter();
        filter.addAction(ESM.ACTION_AWARE_ESM_ANSWERED);
        filter.addAction(ESM.ACTION_AWARE_ESM_DISMISSED);
        filter.addAction(ESM.ACTION_AWARE_ESM_EXPIRED);
        registerReceiver(queue_manager, filter);
        
        if( getQueueSize() > 0 ) {
        	Intent queue_started = new Intent(ESM.ACTION_AWARE_ESM_QUEUE_STARTED);
            sendBroadcast(queue_started);
            
			DialogFragment esm = new ESM_UI();
			esm.show(getSupportFragmentManager(), TAG);
			
			if( ! powerManager.isScreenOn() ) {
	            vibrator.vibrate(777);
	        }
		} else {
			finish();
		}
    }
    
    public class ESM_QueueManager extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getAction().equals(ESM.ACTION_AWARE_ESM_ANSWERED) 
			   || intent.getAction().equals(ESM.ACTION_AWARE_ESM_DISMISSED) 
			   || intent.getAction().equals(ESM.ACTION_AWARE_ESM_EXPIRED) ) {
    			if( getQueueSize() > 0 ) {
    				DialogFragment esm = new ESM_UI();
    				esm.show(getSupportFragmentManager(), "TAG");
    				
    				if( ! powerManager.isScreenOn() ) {
    		            vibrator.vibrate(777);
    		        }
    			} else {
    				finish();
    			}
    		}
    	}
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        unregisterReceiver(queue_manager);
        
        if(Aware.DEBUG) Log.d(TAG,"ESM Queue is done!");
        
        //The user pressed the HOME button... queue got destroyed, so lets set previous queue as dismissed
        if( getQueueSize() > 0 ) {
        	Cursor onqueue = getContentResolver().query(ESM_Data.CONTENT_URI,null, ESM_Data.STATUS + "=" + ESM.STATUS_NEW, null, null);
            if( onqueue != null & onqueue.moveToFirst() ) {
                ContentValues rowData = new ContentValues();
                rowData.put(ESM_Data.ANSWER_TIMESTAMP, System.currentTimeMillis());
                rowData.put(ESM_Data.STATUS, ESM.STATUS_DISMISSED );
                getContentResolver().update(ESM_Data.CONTENT_URI, rowData, ESM_Data.STATUS + "=" + ESM.STATUS_NEW, null);
            }
            if( onqueue != null && ! onqueue.isClosed() ) onqueue.close();
        }
        
        Intent esm_done = new Intent(ESM.ACTION_AWARE_ESM_QUEUE_COMPLETE);
        sendBroadcast(esm_done);
    }
    
    /**
     * Get amount of ESMs waiting on database
     * @return int count
     */
    private int getQueueSize() {
        int size = 0;
        Cursor onqueue = getContentResolver().query(ESM_Data.CONTENT_URI,null, ESM_Data.STATUS + "=" + ESM.STATUS_NEW, null, null);
        if( onqueue != null & onqueue.moveToFirst() ) {
            size = onqueue.getCount();
        }
        if( onqueue != null && ! onqueue.isClosed() ) onqueue.close();
        return size;
    }
}
