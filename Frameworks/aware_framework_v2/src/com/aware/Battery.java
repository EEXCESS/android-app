/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.aware.providers.Battery_Provider;
import com.aware.providers.Battery_Provider.Battery_Charges;
import com.aware.providers.Battery_Provider.Battery_Data;
import com.aware.providers.Battery_Provider.Battery_Discharges;
import com.aware.utils.Aware_Sensor;

/**
 * Service that logs power related events (battery and shutdown/reboot)
 * - Battery changed
 * - Battery charging
 * - Battery discharging
 * - Battery full
 * - Battery low
 * - Phone shutdown
 * - Phone reboot
 * @author denzil
 *
 */
public class Battery extends Aware_Sensor {
	
    /**
	 * Logging tag (default = "AWARE::Service")
	 */
	public static String TAG = "AWARE::Battery";
	
	/**
	 * Broadcasted event: the battery values just changed
	 */
	public static final String ACTION_AWARE_BATTERY_CHANGED = "ACTION_AWARE_BATTERY_CHANGED";
	
	/**
	 * Broadcasted event: the user just started charging
	 */
	public static final String ACTION_AWARE_BATTERY_CHARGING = "ACTION_AWARE_BATTERY_CHARGING";
	
	/**
	 * Broadcasted event: battery charging over power supply (AC)
	 */
	public static final String ACTION_AWARE_BATTERY_CHARGING_AC = "ACTION_AWARE_BATTERY_CHARGING_AC";
	
	/**
	 * Broadcasted event: battery charging over USB
	 */
	public static final String ACTION_AWARE_BATTERY_CHARGING_USB = "ACTION_AWARE_BATTERY_CHARGING_USB";
	
	/**
	 * Broadcasted event: the user just stopped charging and is running on battery
	 */
	public static final String ACTION_AWARE_BATTERY_DISCHARGING = "ACTION_AWARE_BATTERY_DISCHARGING";
	
	/**
	 * Broadcasted event: the battery is fully charged
	 */
	public static final String ACTION_AWARE_BATTERY_FULL = "ACTION_AWARE_BATTERY_FULL";
	
	/**
	 * Broadcasted event: the battery is running low and should be charged ASAP
	 */
	public static final String ACTION_AWARE_BATTERY_LOW = "ACTION_AWARE_BATTERY_LOW";
	
	/**
	 * Broadcasted event: the phone is about to be shutdown.
	 */
	public static final String ACTION_AWARE_PHONE_SHUTDOWN = "ACTION_AWARE_PHONE_SHUTDOWN";
	
	/**
	 * Broadcasted event: the phone is about to be rebooted.
	 */
	public static final String ACTION_AWARE_PHONE_REBOOT = "ACTION_AWARE_PHONE_REBOOT";
	
	/**
	 * {@link Battery_Data#STATUS} Phone shutdown
	 */
	public static final int STATUS_PHONE_SHUTDOWN = -1;
	
	/**
     * {@link Battery_Data#STATUS} Phone rebooted
     */
    public static final int STATUS_PHONE_REBOOT = -2;
    
    /**
     * BroadcastReceiver for Battery module
     * - ACTION_BATTERY_CHANGED: battery values changed
     * - ACTION_BATTERY_PLUGGED_AC: user is charging via AC
     * - ACTION_BATTERY_PLUGGED_USB: user is charging via USB
     * - ACTION_BATTERY_STATUS_FULL: battery finished charging
     * - ACTION_POWER_CONNECTED: power is connected
     * - ACTION_POWER_DISCONNECTED: power is disconnected
     * - ACTION_BATTERY_LOW: battery is running low (15% by Android OS)
     * - ACTION_SHUTDOWN: phone is about to shut down
     * - ACTION_REBOOT: phone is about to reboot
     * - ACTION_AWARE_WEBSERVICE: request for webservice remote backup
     * @author df
     *
     */
	public static class Battery_Broadcaster extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                
                Bundle extras = intent.getExtras();
                if(extras == null) return;
                
                ContentValues rowData = new ContentValues();
                rowData.put(Battery_Data.TIMESTAMP, System.currentTimeMillis());
                rowData.put(Battery_Data.DEVICE_ID, Aware.getSetting(context.getContentResolver(),Aware_Preferences.DEVICE_ID));
                rowData.put(Battery_Data.STATUS, extras.getInt(BatteryManager.EXTRA_STATUS));
                rowData.put(Battery_Data.LEVEL, extras.getInt(BatteryManager.EXTRA_LEVEL));
                rowData.put(Battery_Data.SCALE, extras.getInt(BatteryManager.EXTRA_SCALE));
                rowData.put(Battery_Data.VOLTAGE, extras.getInt(BatteryManager.EXTRA_VOLTAGE));
                rowData.put(Battery_Data.TEMPERATURE, extras.getInt(BatteryManager.EXTRA_TEMPERATURE)/10);
                rowData.put(Battery_Data.PLUG_ADAPTOR, extras.getInt(BatteryManager.EXTRA_PLUGGED));
                rowData.put(Battery_Data.HEALTH, extras.getInt(BatteryManager.EXTRA_HEALTH));
                rowData.put(Battery_Data.TECHNOLOGY, extras.getString(BatteryManager.EXTRA_TECHNOLOGY));
                
                try{
                    if( Aware.DEBUG ) Log.d(TAG,"Battery:" + rowData.toString());
                    context.getContentResolver().insert(Battery_Data.CONTENT_URI, rowData);
                }catch( SQLiteException e ) {
                    if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                }catch( SQLException e ) {
                    if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                }
            
                if(extras.getInt(BatteryManager.EXTRA_PLUGGED) == BatteryManager.BATTERY_PLUGGED_AC) {
                    if(Aware.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_CHARGING_AC);
                    Intent battChargeAC = new Intent(ACTION_AWARE_BATTERY_CHARGING_AC);
                    context.sendBroadcast(battChargeAC);
                }
                
                if(extras.getInt(BatteryManager.EXTRA_PLUGGED) == BatteryManager.BATTERY_PLUGGED_USB) {
                    if(Aware.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_CHARGING_USB);
                    Intent battChargeUSB = new Intent(ACTION_AWARE_BATTERY_CHARGING_USB);
                    context.sendBroadcast(battChargeUSB);
                }
                
                if(extras.getInt(BatteryManager.EXTRA_STATUS) == BatteryManager.BATTERY_STATUS_FULL) {
                    if(Aware.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_FULL);
                    Intent battFull = new Intent(ACTION_AWARE_BATTERY_FULL);
                    context.sendBroadcast(battFull);
                }
                
                if(Aware.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_CHANGED);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_CHANGED);
                context.sendBroadcast(battChanged);
            }
            
            if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                Cursor lastBattery = context.getContentResolver().query(Battery_Data.CONTENT_URI, null, null, null, Battery_Data.TIMESTAMP + " DESC LIMIT 1");
                
                Cursor lastDischarge = context.getContentResolver().query(Battery_Discharges.CONTENT_URI, null, Battery_Discharges.END_TIMESTAMP + "=0", null, Battery_Discharges.TIMESTAMP + " DESC LIMIT 1");
                if( lastDischarge != null && lastDischarge.moveToFirst()) {
                	if( lastBattery != null && lastBattery.moveToFirst() ) {
                		ContentValues rowData = new ContentValues();
                		rowData.put(Battery_Discharges.BATTERY_END, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.LEVEL)));
                		rowData.put(Battery_Discharges.END_TIMESTAMP, System.currentTimeMillis());
                		context.getContentResolver().update(Battery_Discharges.CONTENT_URI, rowData, Battery_Discharges._ID+"="+ lastDischarge.getInt(lastDischarge.getColumnIndex(Battery_Discharges._ID)), null );
                	}
                }
                if( lastDischarge != null && ! lastDischarge.isClosed() ) lastDischarge.close();
                
                if( lastBattery != null && lastBattery.moveToFirst() ) {
	                ContentValues rowData = new ContentValues();
	                rowData.put(Battery_Charges.TIMESTAMP, System.currentTimeMillis());
	                rowData.put(Battery_Charges.DEVICE_ID, Aware.getSetting(context.getContentResolver(), Aware_Preferences.DEVICE_ID));
	                rowData.put(Battery_Charges.BATTERY_START, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.LEVEL)));
	                context.getContentResolver().insert(Battery_Charges.CONTENT_URI, rowData);
                }
                if( lastBattery != null && ! lastBattery.isClosed() ) lastBattery.close();
                
                if(Aware.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_CHARGING);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_CHARGING);
                context.sendBroadcast(battChanged);
            }
            
            if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                Cursor lastBattery = context.getContentResolver().query(Battery_Data.CONTENT_URI, null, null, null, Battery_Data.TIMESTAMP + " DESC LIMIT 1");
                
                Cursor lastCharge = context.getContentResolver().query(Battery_Charges.CONTENT_URI, null, Battery_Charges.END_TIMESTAMP + "=0", null, Battery_Charges.TIMESTAMP + " DESC LIMIT 1");
                if( lastCharge != null && lastCharge.moveToFirst()) {
                	if( lastBattery != null && lastBattery.moveToFirst() ) {
                		ContentValues rowData = new ContentValues();
                		rowData.put(Battery_Charges.BATTERY_END, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.LEVEL)));
                		rowData.put(Battery_Charges.END_TIMESTAMP, System.currentTimeMillis());
                		context.getContentResolver().update(Battery_Charges.CONTENT_URI, rowData, Battery_Charges._ID+"="+ lastCharge.getInt(lastCharge.getColumnIndex(Battery_Charges._ID)), null );
                	}
                }
                if( lastCharge != null && ! lastCharge.isClosed() ) lastCharge.close();
                
                if( lastBattery != null && lastBattery.moveToFirst() ) {
	                ContentValues rowData = new ContentValues();
	                rowData.put(Battery_Discharges.TIMESTAMP, System.currentTimeMillis());
	                rowData.put(Battery_Discharges.DEVICE_ID, Aware.getSetting(context.getContentResolver(), Aware_Preferences.DEVICE_ID));
	                rowData.put(Battery_Discharges.BATTERY_START, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.LEVEL)));
	                context.getContentResolver().insert(Battery_Discharges.CONTENT_URI, rowData);
                }
                if( lastBattery != null && ! lastBattery.isClosed() ) lastBattery.close();
                
                if(Aware.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_DISCHARGING);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_DISCHARGING);
                context.sendBroadcast(battChanged);
            }
            
            if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
                if(Aware.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_LOW);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_LOW);
                context.sendBroadcast(battChanged);
            }
            
            if(intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                Cursor lastBattery = context.getContentResolver().query(Battery_Data.CONTENT_URI, null, null, null, Battery_Data.TIMESTAMP + " DESC LIMIT 1");
                if(lastBattery != null && lastBattery.moveToFirst()) {
                    ContentValues rowData = new ContentValues();
                    rowData.put(Battery_Data.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(Battery_Data.DEVICE_ID, Aware.getSetting(context.getContentResolver(),Aware_Preferences.DEVICE_ID));
                    rowData.put(Battery_Data.STATUS, STATUS_PHONE_SHUTDOWN);
                    rowData.put(Battery_Data.LEVEL, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.LEVEL)));
                    rowData.put(Battery_Data.SCALE, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.SCALE)));
                    rowData.put(Battery_Data.VOLTAGE, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.VOLTAGE)));
                    rowData.put(Battery_Data.TEMPERATURE, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.TEMPERATURE)));
                    rowData.put(Battery_Data.PLUG_ADAPTOR, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.PLUG_ADAPTOR)));
                    rowData.put(Battery_Data.HEALTH, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.HEALTH)));
                    rowData.put(Battery_Data.TECHNOLOGY, lastBattery.getString(lastBattery.getColumnIndex(Battery_Data.TECHNOLOGY)));
                    
                    try {
                        if( Aware.DEBUG ) Log.d(TAG,"Battery:" + rowData.toString());
                        context.getContentResolver().insert(Battery_Data.CONTENT_URI, rowData);
                    }catch( SQLiteException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }
                }
                if( lastBattery != null && ! lastBattery.isClosed() ) lastBattery.close();
                
                if(Aware.DEBUG) Log.d(TAG, ACTION_AWARE_PHONE_SHUTDOWN);
                Intent battChanged = new Intent(ACTION_AWARE_PHONE_SHUTDOWN);
                context.sendBroadcast(battChanged);
            }
            
            if(intent.getAction().equals(Intent.ACTION_REBOOT)) {
                Cursor lastBattery = context.getContentResolver().query(Battery_Data.CONTENT_URI, null, null, null, Battery_Data.TIMESTAMP + " DESC LIMIT 1");
                if( lastBattery != null && lastBattery.moveToFirst() ) {
                    ContentValues rowData = new ContentValues();
                    rowData.put(Battery_Data.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(Battery_Data.DEVICE_ID, Aware.getSetting(context.getContentResolver(),Aware_Preferences.DEVICE_ID));
                    rowData.put(Battery_Data.STATUS, STATUS_PHONE_REBOOT);
                    rowData.put(Battery_Data.LEVEL, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.LEVEL)));
                    rowData.put(Battery_Data.SCALE, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.SCALE)));
                    rowData.put(Battery_Data.VOLTAGE, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.VOLTAGE)));
                    rowData.put(Battery_Data.TEMPERATURE, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.TEMPERATURE)));
                    rowData.put(Battery_Data.PLUG_ADAPTOR, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.PLUG_ADAPTOR)));
                    rowData.put(Battery_Data.HEALTH, lastBattery.getInt(lastBattery.getColumnIndex(Battery_Data.HEALTH)));
                    rowData.put(Battery_Data.TECHNOLOGY, lastBattery.getString(lastBattery.getColumnIndex(Battery_Data.TECHNOLOGY)));
                    
                    try {
                        if( Aware.DEBUG ) Log.d(TAG,"Battery:" + rowData.toString());
                        context.getContentResolver().insert(Battery_Data.CONTENT_URI, rowData);
                    }catch( SQLiteException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }
                }
                if( lastBattery != null && ! lastBattery.isClosed() ) lastBattery.close();
                
                if(Aware.DEBUG) Log.d(TAG, ACTION_AWARE_PHONE_REBOOT);
                Intent battChanged = new Intent(ACTION_AWARE_PHONE_REBOOT);
                context.sendBroadcast(battChanged);
            }
        }	    
	}
	private static final Battery_Broadcaster batteryMonitor = new Battery_Broadcaster();
	
	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();
	public class ServiceBinder extends Binder {
		Battery getService() {
			return Battery.getService();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	
    private static Battery batterySrv = Battery.getService();
    /**
     * Singleton instance to service
     * @return Battery
     */
    public static Battery getService() {
    	if( batterySrv == null ) batterySrv = new Battery();
        return batterySrv;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		
		TAG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG):TAG;
		
		DATABASE_TABLES = Battery_Provider.DATABASE_TABLES;
    	TABLES_FIELDS = Battery_Provider.TABLES_FIELDS;
    	CONTEXT_URIS = new Uri[]{ Battery_Data.CONTENT_URI, Battery_Discharges.CONTENT_URI, Battery_Charges.CONTENT_URI };
		
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_REBOOT);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(batteryMonitor, filter);
        
		if(Aware.DEBUG) Log.d(TAG, "Battery service created!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(batteryMonitor);
		
		if(Aware.DEBUG) Log.d(TAG,"Battery service terminated...");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    
	    TAG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(),Aware_Preferences.DEBUG_TAG):TAG;
	    
        if(Aware.DEBUG) Log.d(TAG, "Battery service active...");
        
	    return START_STICKY;
	}
}
