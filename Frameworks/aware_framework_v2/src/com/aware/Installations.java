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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.aware.providers.Installations_Provider;
import com.aware.providers.Installations_Provider.Installations_Data;
import com.aware.utils.Aware_Sensor;

/**
 * Service that logs application installations on the device. 
 * - ACTION_AWARE_APPLICATION_ADDED : new application installed
 * - ACTION_AWARE_APPLICATION_REMOVED: application removed
 * - ACTION_AWARE_APPLICATION_UPDATED: application updated
 * @author denzil
 */

public class Installations extends Aware_Sensor {
    
    private static String TAG = "AWARE::Installations";
    
    private static Installations installationsSrv = Installations.getService();
    
    /**
     * Broadcasted event: new application has been installed
     */
    public static final String ACTION_AWARE_APPLICATION_ADDED = "ACTION_AWARE_APPLICATION_ADDED";
    
    /**
     * Broadcasted event: an existing application has been removed
     */
    public static final String ACTION_AWARE_APPLICATION_REMOVED = "ACTION_AWARE_APPLICATION_REMOVED";
    
    /**
     * Broadcasted event: an existing application has been updated
     */
    public static final String ACTION_AWARE_APPLICATION_UPDATED = "ACTION_AWARE_APPLICATION_UPDATED";
    
    /**
     * Status for application removed = 0
     */
    public static final int STATUS_REMOVED = 0;
    
    /**
     * Status for application added = 1
     */
    public static final int STATUS_ADDED = 1;
    
    /**
     * Status for application updated = 2
     */
    public static final int STATUS_UPDATED = 2;
    
    /**
     * Activity-Service binder
     */
    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        Installations getService() {
            return Installations.getService();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
    
    /**
     * Singleton instance to this service
     * @return {@link Installations} obj
     */
    public static Installations getService() {
        if( installationsSrv == null ) installationsSrv = new Installations();
        return installationsSrv;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        TAG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG):TAG;
        
        DATABASE_TABLES = Installations_Provider.DATABASE_TABLES;
    	TABLES_FIELDS = Installations_Provider.TABLES_FIELDS;
    	CONTEXT_URIS = new Uri[]{ Installations_Data.CONTENT_URI };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        registerReceiver(installationsMonitor, filter);
        
        if( Aware.DEBUG ) Log.d(TAG,"Installations service created!");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TAG = Aware.getSetting(getContentResolver(),Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(),Aware_Preferences.DEBUG_TAG):TAG;
        if( Aware.DEBUG ) Log.d(TAG,"Installations service active...");
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        unregisterReceiver(installationsMonitor);
        
        if( Aware.DEBUG ) Log.d(TAG,"Installations service terminated...");
    }

    /**
     * BroadcastReceiver for Installations module
     * - Monitor for changes in installations on the device: 
     * {@link Intent#ACTION_PACKAGE_ADDED} <br/>
     * {@link Intent#ACTION_PACKAGE_REPLACED} <br/>
     * {@link Intent#ACTION_PACKAGE_REMOVED} <br/>
     * @author denzil
     */
    public static class Packages_Monitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            
            if( Aware.getSetting(context.getContentResolver(), Aware_Preferences.STATUS_INSTALLATIONS).equals("true") ) {
                
                PackageManager packageManager = context.getPackageManager();
                
                Bundle extras = intent.getExtras();
                
                if( intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ) {
                    
                    if( extras.getBoolean(Intent.EXTRA_REPLACING) ) return; //this is an update!
                    
                    Uri packageUri = intent.getData();
                    if( packageUri == null ) return;
                    String packageName = packageUri.getSchemeSpecificPart();
                    if( packageName == null ) return;
                    
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
                    } catch( final NameNotFoundException e ) {
                        appInfo = null;
                    }
                    String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";
                    
                    ContentValues rowData = new ContentValues();
                    rowData.put(Installations_Data.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(Installations_Data.DEVICE_ID, Aware.getSetting(context.getContentResolver(), Aware_Preferences.DEVICE_ID));
                    rowData.put(Installations_Data.PACKAGE_NAME, packageName);
                    rowData.put(Installations_Data.APPLICATION_NAME, appName);
                    rowData.put(Installations_Data.INSTALLATION_STATUS, STATUS_ADDED);
                    
                    try {
                        context.getContentResolver().insert(Installations_Data.CONTENT_URI, rowData);
                        if( Aware.DEBUG ) {
                            Log.d(TAG,"Installed application:" + packageName);
                        }
                    }catch( SQLiteException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }
                }
                
                if( intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) ) {
                    
                    if( extras.getBoolean(Intent.EXTRA_REPLACING) ) return; //this is an update!
                    
                    Uri packageUri = intent.getData();
                    if( packageUri == null ) return;
                    String packageName = packageUri.getSchemeSpecificPart();
                    if( packageName == null ) return;
                    
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
                    } catch( final NameNotFoundException e ) {
                        appInfo = null;
                    }
                    String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";
                    
                    ContentValues rowData = new ContentValues();
                    rowData.put(Installations_Data.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(Installations_Data.DEVICE_ID, Aware.getSetting(context.getContentResolver(), Aware_Preferences.DEVICE_ID));
                    rowData.put(Installations_Data.PACKAGE_NAME, packageName);
                    rowData.put(Installations_Data.APPLICATION_NAME, appName);
                    rowData.put(Installations_Data.INSTALLATION_STATUS, STATUS_REMOVED);
                    
                    try {
                        context.getContentResolver().insert(Installations_Data.CONTENT_URI, rowData);
                        if( Aware.DEBUG ) {
                            Log.d(TAG,"Removed application:" + packageName);
                        }
                    }catch( SQLiteException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }
                }
                 
                if( intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                    Uri packageUri = intent.getData();
                    if( packageUri == null ) return;
                    String packageName = packageUri.getSchemeSpecificPart();
                    if( packageName == null ) return;
                    
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
                    } catch( final NameNotFoundException e ) {
                        appInfo = null;
                    }
                    String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";
                    
                    ContentValues rowData = new ContentValues();
                    rowData.put(Installations_Data.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(Installations_Data.DEVICE_ID, Aware.getSetting(context.getContentResolver(), Aware_Preferences.DEVICE_ID));
                    rowData.put(Installations_Data.PACKAGE_NAME, packageName);
                    rowData.put(Installations_Data.APPLICATION_NAME, appName);
                    rowData.put(Installations_Data.INSTALLATION_STATUS, STATUS_UPDATED);
                    
                    try {
                        context.getContentResolver().insert(Installations_Data.CONTENT_URI, rowData);
                        if( Aware.DEBUG) {
                            Log.d(TAG,"Updated application:" + packageName);
                        }
                    }catch( SQLiteException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }
                }
            }
        }
    }
    private static final Packages_Monitor installationsMonitor = new Packages_Monitor();
}
