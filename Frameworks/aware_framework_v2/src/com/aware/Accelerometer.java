/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Accelerometer_Provider.Accelerometer_Data;
import com.aware.providers.Accelerometer_Provider.Accelerometer_Sensor;
import com.aware.utils.Aware_Sensor;

/**
 * AWARE Accelerometer module
 * - Accelerometer raw data
 * - Accelerometer sensor information
 * @author df
 *
 */
public class Accelerometer extends Aware_Sensor implements SensorEventListener {
    
    /**
     * Logging tag (default = "AWARE::Accelerometer")
     */
    private static String TAG = "AWARE::Accelerometer";
    
    /**
     * Sensor update frequency ( default = {@link SensorManager#SENSOR_DELAY_NORMAL})
     */
    private static int SENSOR_DELAY = 200000;
    
    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;
    private static HandlerThread sensorThread = null;
    private static Handler sensorHandler = null;
    private static PowerManager powerManager = null;
    private static PowerManager.WakeLock wakeLock = null;
    
    /**
     * Broadcasted event: new accelerometer values
     * ContentProvider: Accelerometer_Provider
     */
    public static final String ACTION_AWARE_ACCELEROMETER = "ACTION_AWARE_ACCELEROMETER";
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //We log current accuracy on the sensor changed event
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ContentValues rowData = new ContentValues();
        rowData.put(Accelerometer_Data.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
        rowData.put(Accelerometer_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(Accelerometer_Data.VALUES_0, event.values[0]);
        rowData.put(Accelerometer_Data.VALUES_1, event.values[1]);
        rowData.put(Accelerometer_Data.VALUES_2, event.values[2]);
        rowData.put(Accelerometer_Data.ACCURACY, event.accuracy);
        
        try {
            getContentResolver().insert(Accelerometer_Data.CONTENT_URI, rowData);
            
            Intent accelData = new Intent(ACTION_AWARE_ACCELEROMETER);
            sendBroadcast(accelData);
            
            if( Aware.DEBUG ) Log.d(TAG, "Accelerometer:"+ rowData.toString());
        }catch( SQLiteException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }
    }
    
    private void saveAccelerometerDevice(Sensor acc) {
        Cursor accelInfo = getContentResolver().query(Accelerometer_Sensor.CONTENT_URI, null, null, null, null);
        if( accelInfo == null || ! accelInfo.moveToFirst() ) {
            ContentValues rowData = new ContentValues();
            rowData.put(Accelerometer_Sensor.DEVICE_ID, Aware.getSetting(getContentResolver(),"device_id"));
            rowData.put(Accelerometer_Sensor.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Accelerometer_Sensor.MAXIMUM_RANGE, acc.getMaximumRange());
            rowData.put(Accelerometer_Sensor.MINIMUM_DELAY, acc.getMinDelay());
            rowData.put(Accelerometer_Sensor.NAME, acc.getName());
            rowData.put(Accelerometer_Sensor.POWER_MA, acc.getPower());
            rowData.put(Accelerometer_Sensor.RESOLUTION, acc.getResolution());
            rowData.put(Accelerometer_Sensor.TYPE, acc.getType());
            rowData.put(Accelerometer_Sensor.VENDOR, acc.getVendor());
            rowData.put(Accelerometer_Sensor.VERSION, acc.getVersion());
            
            try {
                getContentResolver().insert(Accelerometer_Sensor.CONTENT_URI, rowData);
                if( Aware.DEBUG ) Log.d(TAG, "Accelerometer device:"+ rowData.toString());
            }catch( SQLiteException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }
        } else accelInfo.close();
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mAccelerometer == null) {
            if(Aware.DEBUG) Log.w(TAG,"This device does not have an accelerometer!");
            stopSelf();
        }
        
        TAG = Aware.getSetting(getContentResolver(),"debug_tag").length()>0?Aware.getSetting(getContentResolver(),"debug_tag"):TAG;
        try {
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_ACCELEROMETER));
        }catch ( NumberFormatException e) {
            Aware.setSetting(getContentResolver(), Aware_Preferences.FREQUENCY_ACCELEROMETER, 200000);
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_ACCELEROMETER));
        }
        
        DATABASE_TABLES = Accelerometer_Provider.DATABASE_TABLES;
    	TABLES_FIELDS = Accelerometer_Provider.TABLES_FIELDS;
    	CONTEXT_URIS = new Uri[]{ Accelerometer_Sensor.CONTENT_URI, Accelerometer_Data.CONTENT_URI };
        
        sensorThread = new HandlerThread(TAG);
        sensorThread.start();
        
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        
        sensorHandler = new Handler(sensorThread.getLooper());
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY, sensorHandler);
        
        saveAccelerometerDevice(mAccelerometer);
        
        if(Aware.DEBUG) Log.d(TAG,"Accelerometer service created!");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        sensorHandler.removeCallbacksAndMessages(null);
        mSensorManager.unregisterListener(this, mAccelerometer);
        sensorThread.quit();
        
        wakeLock.release();
        
        if(Aware.DEBUG) Log.d(TAG,"Accelerometer service terminated...");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        TAG = Aware.getSetting(getContentResolver(),"debug_tag").length()>0?Aware.getSetting(getContentResolver(),"debug_tag"):TAG;
        try {
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_ACCELEROMETER));
        }catch ( NumberFormatException e) {
            Aware.setSetting(getContentResolver(), Aware_Preferences.FREQUENCY_ACCELEROMETER, 200000);
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_ACCELEROMETER));
        }
        
        if(intent.getBooleanExtra("refresh", false)) {
            sensorHandler.removeCallbacksAndMessages(null);
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY, sensorHandler);
        }
        
        if(Aware.DEBUG) Log.d(TAG,"Accelerometer service active...");
        
        return START_STICKY;
    }

    //Singleton instance of this service
    private static Accelerometer accelerometerSrv = Accelerometer.getService();
    
    /**
     * Get singleton instance to Accelerometer service
     * @return Accelerometer obj
     */
    public static Accelerometer getService() {
        if( accelerometerSrv == null ) accelerometerSrv = new Accelerometer();
        return accelerometerSrv;
    }
    
    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        Accelerometer getService() {
            return Accelerometer.getService();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}