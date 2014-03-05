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

import com.aware.providers.Linear_Accelerometer_Provider;
import com.aware.providers.Linear_Accelerometer_Provider.Linear_Accelerometer_Data;
import com.aware.providers.Linear_Accelerometer_Provider.Linear_Accelerometer_Sensor;
import com.aware.utils.Aware_Sensor;

/**
 * AWARE Linear-accelerometer module: 
 * A three dimensional vector indicating acceleration along each device axis, not including gravity. All values have units of m/s^2. The coordinate system is the same as is used by the acceleration sensor.
 * The output of the accelerometer, gravity and linear-acceleration sensors must obey the following relation: acceleration = gravity + linear-acceleration
 * - Linear Accelerometer raw data
 * - Linear Accelerometer sensor information
 * @author df
 *
 */
public class LinearAccelerometer extends Aware_Sensor implements SensorEventListener {
    
    /**
     * Logging tag (default = "AWARE::LinearAccelerometer")
     */
    private static String TAG = "AWARE::Linear Accelerometer";
    
    /**
     * Sensor update frequency ( default = {@link SensorManager#SENSOR_DELAY_NORMAL})
     */
    private static int SENSOR_DELAY = 200000;
    
    private static SensorManager mSensorManager;
    private static Sensor mLinearAccelerator;
    private static HandlerThread sensorThread = null;
    private static Handler sensorHandler = null;
    private static PowerManager powerManager = null;
    private static PowerManager.WakeLock wakeLock = null;
    
    /**
     * Broadcasted event: new sensor values
     * ContentProvider: LinearAccelerationProvider
     */
    public static final String ACTION_AWARE_LINEAR_ACCELEROMETER = "ACTION_AWARE_LINEAR_ACCELEROMETER";
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //We log current accuracy on the sensor changed event
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ContentValues rowData = new ContentValues();
        rowData.put(Linear_Accelerometer_Data.DEVICE_ID, Aware.getSetting(getContentResolver(),"device_id"));
        rowData.put(Linear_Accelerometer_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(Linear_Accelerometer_Data.VALUES_0, event.values[0]);
        rowData.put(Linear_Accelerometer_Data.VALUES_1, event.values[1]);
        rowData.put(Linear_Accelerometer_Data.VALUES_2, event.values[2]);
        rowData.put(Linear_Accelerometer_Data.ACCURACY, event.accuracy);
        
        try {
            getContentResolver().insert(Linear_Accelerometer_Data.CONTENT_URI, rowData);
            Intent accelData = new Intent(ACTION_AWARE_LINEAR_ACCELEROMETER);
            sendBroadcast(accelData);
            if( Aware.DEBUG ) Log.d(TAG, "Linear-accelerometer:"+ rowData.toString());
        }catch( SQLiteException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }
    }
    
    private void saveAccelerometerDevice(Sensor acc) {
        Cursor accelInfo = getContentResolver().query(Linear_Accelerometer_Sensor.CONTENT_URI, null, null, null, null);
        if( accelInfo == null || ! accelInfo.moveToFirst() ) {
            ContentValues rowData = new ContentValues();
            rowData.put(Linear_Accelerometer_Sensor.DEVICE_ID, Aware.getSetting(getContentResolver(),"device_id"));
            rowData.put(Linear_Accelerometer_Sensor.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Linear_Accelerometer_Sensor.MAXIMUM_RANGE, acc.getMaximumRange());
            rowData.put(Linear_Accelerometer_Sensor.MINIMUM_DELAY, acc.getMinDelay());
            rowData.put(Linear_Accelerometer_Sensor.NAME, acc.getName());
            rowData.put(Linear_Accelerometer_Sensor.POWER_MA, acc.getPower());
            rowData.put(Linear_Accelerometer_Sensor.RESOLUTION, acc.getResolution());
            rowData.put(Linear_Accelerometer_Sensor.TYPE, acc.getType());
            rowData.put(Linear_Accelerometer_Sensor.VENDOR, acc.getVendor());
            rowData.put(Linear_Accelerometer_Sensor.VERSION, acc.getVersion());
            
            try {
                getContentResolver().insert(Linear_Accelerometer_Sensor.CONTENT_URI, rowData);
                if( Aware.DEBUG ) Log.d(TAG, "Linear-accelerometer sensor: "+ rowData.toString());
            }catch( SQLiteException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }
        }else accelInfo.close();
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        
        mLinearAccelerator = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        
        if(mLinearAccelerator == null) {
            if(Aware.DEBUG) Log.w(TAG,"This device does not have a linear-accelerometer!");
            stopSelf();
        }
        
        TAG = Aware.getSetting(getContentResolver(),"debug_tag").length()>0?Aware.getSetting(getContentResolver(),"debug_tag"):TAG;
        try {
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER));
        } catch( NumberFormatException e ) {
            Aware.setSetting(getContentResolver(), Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 200000);
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER));
        }
        
        sensorThread = new HandlerThread(TAG);
        sensorThread.start();
        
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        
        sensorHandler = new Handler(sensorThread.getLooper());
        mSensorManager.registerListener(this, mLinearAccelerator, SENSOR_DELAY, sensorHandler);
        
        saveAccelerometerDevice(mLinearAccelerator);
        
        DATABASE_TABLES = Linear_Accelerometer_Provider.DATABASE_TABLES;
    	TABLES_FIELDS = Linear_Accelerometer_Provider.TABLES_FIELDS;
    	CONTEXT_URIS = new Uri[]{ Linear_Accelerometer_Sensor.CONTENT_URI, Linear_Accelerometer_Data.CONTENT_URI };
    	
        if(Aware.DEBUG) Log.d(TAG,"Linear-accelerometer service created!");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        sensorHandler.removeCallbacksAndMessages(null);
        mSensorManager.unregisterListener(this, mLinearAccelerator);
        sensorThread.quit();
        
        wakeLock.release();
        
        if(Aware.DEBUG) Log.d(TAG,"Linear-accelerometer service terminated...");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        TAG = Aware.getSetting(getContentResolver(),"debug_tag").length()>0?Aware.getSetting(getContentResolver(),"debug_tag"):TAG;
        try {
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER));
        } catch( NumberFormatException e ) {
            Aware.setSetting(getContentResolver(), Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 200000);
            SENSOR_DELAY = Integer.parseInt(Aware.getSetting(getContentResolver(),Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER));
        }
        
        if(intent.getBooleanExtra("refresh", false)) {
            sensorHandler.removeCallbacksAndMessages(null);
            mSensorManager.unregisterListener(this, mLinearAccelerator);
            mSensorManager.registerListener(this, mLinearAccelerator, SENSOR_DELAY, sensorHandler);
        }
        
        if(Aware.DEBUG) Log.d(TAG,"Linear-accelerometer service active...");
        
        return START_STICKY;
    }

    //Singleton instance of this service
    private static LinearAccelerometer linearaccelerometerSrv = LinearAccelerometer.getService();
    
    /**
     * Get singleton instance to service
     * @return Linear_Accelerometer obj
     */
    public static LinearAccelerometer getService() {
        if( linearaccelerometerSrv == null ) linearaccelerometerSrv = new LinearAccelerometer();
        return linearaccelerometerSrv;
    }
    
    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        LinearAccelerometer getService() {
            return LinearAccelerometer.getService();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}