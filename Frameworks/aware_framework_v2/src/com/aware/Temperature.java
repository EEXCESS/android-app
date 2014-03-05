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

import com.aware.providers.Temperature_Provider;
import com.aware.providers.Temperature_Provider.Temperature_Data;
import com.aware.providers.Temperature_Provider.Temperature_Sensor;
import com.aware.utils.Aware_Sensor;

/**
 * AWARE Temperature module
 * - Temperature raw data in ambient room temperature in degree Celsius
 * - Temperature sensor information
 * @author df
 */
public class Temperature extends Aware_Sensor implements SensorEventListener {
    
    /**
     * Logging tag (default = "AWARE::Temperature")
     */
    private static String TAG = "AWARE::Temperature";
    
    /**
     * Sensor update frequency ( default = {@link SensorManager#SENSOR_DELAY_NORMAL})
     */
    private static int SENSOR_DELAY = 200000;
    
    private static SensorManager mSensorManager;
    private static Sensor mTemperature;
    private static HandlerThread sensorThread = null;
    private static Handler sensorHandler = null;
    private static PowerManager powerManager = null;
    private static PowerManager.WakeLock wakeLock = null;
    
    /**
     * Broadcasted event: new sensor values
     * ContentProvider: Temperature_Provider
     */
    public static final String ACTION_AWARE_TEMPERATURE = "ACTION_AWARE_TEMPERATURE";
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //We log current accuracy on the sensor changed event
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ContentValues rowData = new ContentValues();
        rowData.put(Temperature_Data.DEVICE_ID, Aware.getSetting(getContentResolver(),"device_id"));
        rowData.put(Temperature_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(Temperature_Data.TEMPERATURE_CELSIUS, event.values[0]);
        rowData.put(Temperature_Data.ACCURACY, event.accuracy);
        
        try {
            getContentResolver().insert(Temperature_Data.CONTENT_URI, rowData);
            
            Intent temperatureData = new Intent(ACTION_AWARE_TEMPERATURE);
            sendBroadcast(temperatureData);
            
            if( Aware.DEBUG ) Log.d(TAG, "Temperature:"+ rowData.toString());
        }catch( SQLiteException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }
    }
    
    private void saveSensorDevice(Sensor sensor) {
        Cursor sensorInfo = getContentResolver().query(Temperature_Sensor.CONTENT_URI, null, null, null, null);
        if( sensorInfo == null || ! sensorInfo.moveToFirst() ) {
            ContentValues rowData = new ContentValues();
            rowData.put(Temperature_Sensor.DEVICE_ID, Aware.getSetting(getContentResolver(),"device_id"));
            rowData.put(Temperature_Sensor.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Temperature_Sensor.MAXIMUM_RANGE, sensor.getMaximumRange());
            rowData.put(Temperature_Sensor.MINIMUM_DELAY, sensor.getMinDelay());
            rowData.put(Temperature_Sensor.NAME, sensor.getName());
            rowData.put(Temperature_Sensor.POWER_MA, sensor.getPower());
            rowData.put(Temperature_Sensor.RESOLUTION, sensor.getResolution());
            rowData.put(Temperature_Sensor.TYPE, sensor.getType());
            rowData.put(Temperature_Sensor.VENDOR, sensor.getVendor());
            rowData.put(Temperature_Sensor.VERSION, sensor.getVersion());
            
            try {
                getContentResolver().insert(Temperature_Sensor.CONTENT_URI, rowData);
                if( Aware.DEBUG ) Log.d(TAG, "Temperature sensor info: "+ rowData.toString());
            }catch( SQLiteException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }
        }else sensorInfo.close();
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        
        if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB ) {
            mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        } else {
            mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        }
    
        TAG = Aware.getSetting(getContentResolver(),"debug_tag").length()>0?Aware.getSetting(getContentResolver(),"debug_tag"):TAG;
        try {
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_TEMPERATURE));
        } catch( NumberFormatException e ) {
            Aware.setSetting(getContentResolver(), Aware_Preferences.FREQUENCY_TEMPERATURE, 200000);
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_TEMPERATURE));
        }
        
        sensorThread = new HandlerThread(TAG);
        sensorThread.start();
        
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        
        sensorHandler = new Handler(sensorThread.getLooper());
        mSensorManager.registerListener(this, mTemperature, SENSOR_DELAY, sensorHandler);
        
        saveSensorDevice(mTemperature);
        
        DATABASE_TABLES = Temperature_Provider.DATABASE_TABLES;
        TABLES_FIELDS = Temperature_Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[] { Temperature_Sensor.CONTENT_URI, Temperature_Data.CONTENT_URI };
        
        if(Aware.DEBUG) Log.d(TAG,"Temperature service created!");
    
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        sensorHandler.removeCallbacksAndMessages(null);
        mSensorManager.unregisterListener(this, mTemperature);
        sensorThread.quit();
        
        wakeLock.release();
        
        if(Aware.DEBUG) Log.d(TAG,"Temperature service terminated...");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        TAG = Aware.getSetting(getContentResolver(),"debug_tag").length()>0?Aware.getSetting(getContentResolver(),"debug_tag"):TAG;
        try {
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_TEMPERATURE));
        } catch( NumberFormatException e ) {
            Aware.setSetting(getContentResolver(), Aware_Preferences.FREQUENCY_TEMPERATURE, 200000);
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_TEMPERATURE));
        }
        
        if(intent.getBooleanExtra("refresh", false)) {
            sensorHandler.removeCallbacksAndMessages(null);
            mSensorManager.unregisterListener(this, mTemperature);
            mSensorManager.registerListener(this, mTemperature, SENSOR_DELAY, sensorHandler);
        }
        
        if(Aware.DEBUG) Log.d(TAG,"Temperature service active...");
        
        return START_STICKY;
    }

    //Singleton instance of this service
    private static Temperature temperatureSrv = Temperature.getService();
    
    /**
     * Get singleton instance to service
     * @return Temperature obj
     */
    public static Temperature getService() {
        if( temperatureSrv == null ) temperatureSrv = new Temperature();
        return temperatureSrv;
    }
    
    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        Temperature getService() {
            return Temperature.getService();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}