/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.v4.accessibilityservice.AccessibilityServiceInfoCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.aware.providers.Applications_Provider;
import com.aware.providers.Applications_Provider.Applications_Crashes;
import com.aware.providers.Applications_Provider.Applications_Foreground;
import com.aware.providers.Applications_Provider.Applications_History;
import com.aware.providers.Applications_Provider.Applications_Notifications;
import com.aware.utils.Encrypter;
import com.aware.utils.WebserviceHelper;

/**
 * Service that logs application usage on the device. 
 * Updates every time the user changes application or accesses a sub activity on the screen.
 * - ACTION_AWARE_APPLICATIONS_FOREGROUND: new application on the screen
 * - ACTION_AWARE_APPLICATIONS_HISTORY: applications running was just updated
 * - ACTION_AWARE_APPLICATIONS_NOTIFICATIONS: new notification received
 * - ACTION_AWARE_APPLICATIONS_CRASHES: an application crashed, error and ANR conditions 
 * @author denzil
 */
public class Applications extends AccessibilityService {
    
    private static String TAG = "AWARE::Applications";
    
    private static AlarmManager alarmManager = null;
    private static Intent updateApps = null;
    private static PendingIntent repeatingIntent = null;
    private static AccessibilityEvent lastApplication = null;
    private static AccessibilityEvent lastNotification = null;
    
    /**
     * Broadcasted event: a new application is visible on the foreground
     */
    public static final String ACTION_AWARE_APPLICATIONS_FOREGROUND = "ACTION_AWARE_APPLICATIONS_FOREGROUND";
    
    /**
     * Broadcasted event: new foreground and background statistics are available
     */
    public static final String ACTION_AWARE_APPLICATIONS_HISTORY = "ACTION_AWARE_APPLICATIONS_HISTORY";
    
    /**
     * Broadcasted event: new notification is available
     */
    public static final String ACTION_AWARE_APPLICATIONS_NOTIFICATIONS = "ACTION_AWARE_APPLICATIONS_NOTIFICATIONS";
    
    /**
     * Broadcasted event: application just crashed
     */
    public static final String ACTION_AWARE_APPLICATIONS_CRASHES = "ACTION_AWARE_APPLICATIONS_CRASHES";
    
    /**
     * How often we check what is running on the background (default = 30 seconds)
     */
    public static final int BACKGROUND_APPS_UPDATE_FREQUENCY = 30;
    
    /**
     * Clones an AccessibilityEvent
     */
    private AccessibilityEvent clone(AccessibilityEvent event) {
        AccessibilityEvent clone = AccessibilityEvent.obtain();
        
        clone.setAddedCount(event.getAddedCount());
        clone.setBeforeText(event.getBeforeText());
        clone.setChecked(event.isChecked());
        clone.setClassName(event.getClassName());
        clone.setContentDescription(event.getContentDescription());
        clone.setCurrentItemIndex(event.getCurrentItemIndex());
        clone.setEventTime(event.getEventTime());
        clone.setEventType(event.getEventType());
        clone.setEnabled(event.isEnabled());
        clone.setFromIndex(event.getFromIndex());
        clone.setFullScreen(event.isFullScreen());
        clone.setItemCount(event.getItemCount());
        clone.setPackageName(event.getPackageName());
        clone.setParcelableData(event.getParcelableData());
        clone.setPassword(event.isPassword());
        clone.setRemovedCount(event.getRemovedCount());
        clone.getText().clear();
        clone.getText().addAll(event.getText());

        return clone;
    }
    
    /**
     * Given a package name, get application label in the default language of the device
     * @param Application package name
     * @return String with application name
     */
    private String getApplicationName( String package_name ) {
    	PackageManager packageManager = getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(package_name, PackageManager.GET_ACTIVITIES);
        } catch( final NameNotFoundException e ) {
            appInfo = null;
        }
        String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";
    	return appName;
    }
    
    /**
     * Monitors for events of: <br/>
     * {@link AccessibilityEvent#TYPE_WINDOW_STATE_CHANGED}
     * {@link AccessibilityEvent#TYPE_NOTIFICATION_STATE_CHANGED}
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if( Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_NOTIFICATIONS).equals("true") && event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED ) {
        	
        	//FIXED: Duplicated accessibility events. Because we have the accessibility service both in manifest as well as in service for compabitility with Honeycomb and Gingerbread...
        	if( lastNotification != null && ( lastNotification.getPackageName().toString().equalsIgnoreCase(event.getPackageName().toString()) || (lastNotification.getText().size()>0 && event.getText().size()>0 && lastNotification.getText().get(0).equals(event.getText().get(0))) )) {
        		return;
        	}
        	
        	Notification notificationDetails = (Notification) event.getParcelableData();
        	
        	if( notificationDetails != null ) {
        		ContentValues rowData = new ContentValues();
        		rowData.put(Applications_Notifications.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
            	rowData.put(Applications_Notifications.TIMESTAMP, System.currentTimeMillis());
            	rowData.put(Applications_Notifications.PACKAGE_NAME, event.getPackageName().toString());
            	rowData.put(Applications_Notifications.APPLICATION_NAME, getApplicationName(event.getPackageName().toString()));
            	rowData.put(Applications_Notifications.TEXT, Encrypter.hashSHA1(event.getText().toString()));
            	rowData.put(Applications_Notifications.SOUND, (( notificationDetails.sound != null ) ? notificationDetails.sound.toString() : "") );
            	rowData.put(Applications_Notifications.VIBRATE, (( notificationDetails.vibrate != null) ? notificationDetails.vibrate.toString() : "") );
            	rowData.put(Applications_Notifications.DEFAULTS, notificationDetails.defaults );
            	rowData.put(Applications_Notifications.FLAGS, notificationDetails.flags );
            	
            	if(Aware.DEBUG) Log.d(TAG, "New notification:" + rowData.toString() );
            
            	getContentResolver().insert(Applications_Notifications.CONTENT_URI, rowData);
            	Intent notification = new Intent(ACTION_AWARE_APPLICATIONS_NOTIFICATIONS);
            	sendBroadcast(notification);
        	}
        	
        	lastNotification = clone(event);
        }
        
    	if( Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_APPLICATIONS).equals("true") && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ) {
            if( updateApps == null ) {
                updateApps = new Intent(getApplicationContext(), BackgroundService.class);
                updateApps.setAction(ACTION_AWARE_APPLICATIONS_HISTORY);
                repeatingIntent = PendingIntent.getService(getApplicationContext(), 0, updateApps, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, BACKGROUND_APPS_UPDATE_FREQUENCY * 1000, repeatingIntent);
            }
            
            //FIXED: Duplicated accessibility events. This is caused by accessibility service definition in xml (Android >2.3) and in code (Android 2.3)
            if(lastApplication != null && (lastApplication.getPackageName().toString().equalsIgnoreCase(event.getPackageName().toString()) || (lastApplication.getText().size()>0 && event.getText().size()>0 && lastApplication.getText().get(0).equals(event.getText().get(0))) )) {
                return;
            }
            
            PackageManager packageManager = getPackageManager();
            PackageInfo pkgInfo = null;
            ApplicationInfo appInfo = null;
            try {
                appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), PackageManager.GET_ACTIVITIES);
                pkgInfo = packageManager.getPackageInfo(event.getPackageName().toString(), PackageManager.GET_META_DATA);
            } catch( final NameNotFoundException e ) {
                appInfo = null;
            }
            String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";
        
            ContentValues rowData = new ContentValues();
            rowData.put(Applications_Foreground.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Applications_Foreground.DEVICE_ID, Aware.getSetting(getContentResolver(),Aware_Preferences.DEVICE_ID));
            rowData.put(Applications_Foreground.PACKAGE_NAME, event.getPackageName().toString());
            rowData.put(Applications_Foreground.APPLICATION_NAME, appName);
            rowData.put(Applications_Foreground.IS_SYSTEM_APP, isSystemPackage(pkgInfo));
            
            if( Aware.DEBUG ) Log.d(Aware.TAG, "FOREGROUND: " + rowData.toString());

            try{
                getContentResolver().insert(Applications_Foreground.CONTENT_URI, rowData);
            }catch( SQLiteException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }
        
            lastApplication = clone(event);
            
            Intent newForeground = new Intent(ACTION_AWARE_APPLICATIONS_FOREGROUND);
            sendBroadcast(newForeground);
            
            if( Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_CRASHES).equals("true") ) {
            	
                //Check if there is a crashed application
	            ActivityManager activityMng = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	            List<ProcessErrorStateInfo> errors = activityMng.getProcessesInErrorState();
	            if(errors != null ) {
	            	for(ProcessErrorStateInfo error : errors ) {
	            		
	            		try {
							pkgInfo = packageManager.getPackageInfo(error.processName, PackageManager.GET_META_DATA);
							appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), PackageManager.GET_ACTIVITIES);
				            appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";
							
							ContentValues crashData = new ContentValues();
		            		crashData.put(Applications_Crashes.TIMESTAMP, System.currentTimeMillis());
		            		crashData.put(Applications_Crashes.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
		            		crashData.put(Applications_Crashes.PACKAGE_NAME, error.processName);
		            		crashData.put(Applications_Crashes.APPLICATION_NAME, appName);
		            		crashData.put(Applications_Crashes.APPLICATION_VERSION, pkgInfo.versionCode);
		            		crashData.put(Applications_Crashes.ERROR_SHORT, error.shortMsg);
		            		crashData.put(Applications_Crashes.ERROR_LONG, error.longMsg);
		            		crashData.put(Applications_Crashes.ERROR_CONDITION, error.condition);
		            		crashData.put(Applications_Crashes.IS_SYSTEM_APP, isSystemPackage(pkgInfo));
		            		
		            		getContentResolver().insert(Applications_Crashes.CONTENT_URI, crashData);
		            		
		            		if( Aware.DEBUG ) Log.d(Aware.TAG, "Crashed: " + crashData.toString());
		            		
		            		Intent crashed = new Intent(ACTION_AWARE_APPLICATIONS_CRASHES);
		            		sendBroadcast(crashed);
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
	            	}
	            }
            }
            
            Intent backgroundService = new Intent(this, BackgroundService.class);
            backgroundService.setAction(ACTION_AWARE_APPLICATIONS_HISTORY);
            startService(backgroundService);
        }
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        
        //Backwards compatibility with Honeycomb and Android 2.3...
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfoCompat.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 50;
        info.packageNames = null;
        setServiceInfo(info);
        
        if( Aware.DEBUG ) Log.d("AWARE","Aware service connected to accessibility services...");
        
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        
        TAG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG):TAG;
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Aware.ACTION_AWARE_WEBSERVICE);
        filter.addAction(Aware.ACTION_AWARE_CLEAN_DATABASES);
        registerReceiver(awareMonitor, filter);
        
        if( Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_APPLICATIONS).equals("true") ) {
            updateApps = new Intent(getApplicationContext(), BackgroundService.class);
            updateApps.setAction(ACTION_AWARE_APPLICATIONS_HISTORY);
            repeatingIntent = PendingIntent.getService(getApplicationContext(), 0, updateApps, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, BACKGROUND_APPS_UPDATE_FREQUENCY * 1000, repeatingIntent);
        }
    }
    
    @Override
    public void onInterrupt() {
        Log.e(TAG,"Accessibility Service has been interrupted...");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        if( ! isAccessibilityServiceActive() ) {
            Intent accessibilitySettings = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilitySettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(accessibilitySettings);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_APPLICATIONS, false);
        alarmManager.cancel(repeatingIntent);
        unregisterReceiver(awareMonitor);
    }
    
    /**
     * Check if the accessibility service for AWARE Aware is active
     * @return boolean isActive
     */
    private boolean isAccessibilityServiceActive() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if( accessibilityManager.isEnabled() ) {
            @SuppressWarnings("deprecation")
            List<ServiceInfo> accessibilityServices = accessibilityManager.getAccessibilityServiceList();
            for( ServiceInfo s : accessibilityServices ) {
                if( s.name.equalsIgnoreCase("com.aware.Applications") || s.name.equalsIgnoreCase("com.aware.ApplicationsJB") ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Received AWARE broadcasts
     * - ACTION_AWARE_WEBSERVICE
     * - ACTION_AWARE_CLEAN_DATABASES
     * @author df
     *
     */
    public class Applications_Broadcaster extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            
        	String[] DATABASE_TABLES = Applications_Provider.DATABASE_TABLES;
        	String[] TABLES_FIELDS = Applications_Provider.TABLES_FIELDS;
        	Uri[] CONTEXT_URIS = new Uri[]{ Applications_Foreground.CONTENT_URI, Applications_History.CONTENT_URI, Applications_Notifications.CONTENT_URI, Applications_Crashes.CONTENT_URI };
        	
        	if( intent.getAction().equals(Aware.ACTION_AWARE_WEBSERVICE) ) {
        		for( int i=0; i<DATABASE_TABLES.length; i++ ) {
        			Intent webserviceHelper = new Intent(WebserviceHelper.ACTION_AWARE_WEBSERVICE_SYNC_TABLE);
        			webserviceHelper.putExtra(WebserviceHelper.EXTRA_TABLE, DATABASE_TABLES[i]);
            		webserviceHelper.putExtra(WebserviceHelper.EXTRA_FIELDS, TABLES_FIELDS[i]);
            		webserviceHelper.putExtra(WebserviceHelper.EXTRA_CONTENT_URI, CONTEXT_URIS[i].toString());
            		context.startService(webserviceHelper);
        		}
            }
            
            if( intent.getAction().equals(Aware.ACTION_AWARE_CLEAN_DATABASES)) {
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
    }
    private final Applications_Broadcaster awareMonitor = new Applications_Broadcaster();
    
    /**
     * Applications background service
     * - Updates the current running applications statistics
     * - Uploads data to the webservice
     * @author df
     *
     */
    public static class BackgroundService extends IntentService {
        public BackgroundService() {
            super(TAG+" background service");
        }
        
        @Override
        protected void onHandleIntent(Intent intent) {
            
            //Updating list of running applications/services
            if( Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_APPLICATIONS ).equals("true") && intent.getAction().equals(ACTION_AWARE_APPLICATIONS_HISTORY) ) {
                
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                PackageManager packageManager = (PackageManager) getPackageManager();
                List<RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
                
                if(Aware.DEBUG) Log.d(TAG,"Running " + runningApps.size() + " applications");
                    
                for( RunningAppProcessInfo app : runningApps ) {
                    
                    Cursor appUnclosed = null;
                    PackageInfo appPkg = null;
                    ApplicationInfo appInfo = null;
                    
                    try {
                        
                        appPkg = packageManager.getPackageInfo(app.processName, PackageManager.GET_META_DATA);
                        appInfo = packageManager.getApplicationInfo(app.processName, PackageManager.GET_ACTIVITIES);
                        
                        String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";
                        
                        appUnclosed = getContentResolver().query(Applications_History.CONTENT_URI, null, Applications_History.PACKAGE_NAME + " LIKE '%"+app.processName+"%' AND "+Applications_History.PROCESS_ID + "=" +app.pid + " AND " + Applications_History.END_TIMESTAMP +"=0", null, null);
                        if( appUnclosed == null || appUnclosed.moveToFirst() == false ) {
                            ContentValues rowData = new ContentValues();
                            rowData.put(Applications_History.TIMESTAMP, System.currentTimeMillis());
                            rowData.put(Applications_History.DEVICE_ID, Aware.getSetting(getContentResolver(),"device_id"));
                            rowData.put(Applications_History.PACKAGE_NAME, app.processName);
                            rowData.put(Applications_History.APPLICATION_NAME, appName);
                            rowData.put(Applications_History.PROCESS_IMPORTANCE, app.importance);
                            rowData.put(Applications_History.PROCESS_ID, app.pid);
                            rowData.put(Applications_History.END_TIMESTAMP, 0);
                            rowData.put(Applications_History.IS_SYSTEM_APP, isSystemPackage(appPkg));
                            try {
                                getContentResolver().insert(Applications_History.CONTENT_URI, rowData);
                            }catch( SQLiteException e ) {
                                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                            }catch( SQLException e ) {
                                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                            }
                        } else if( appUnclosed.getInt(appUnclosed.getColumnIndex(Applications_History.PROCESS_IMPORTANCE)) != app.importance ) {
                            //Close last importance
                            ContentValues rowData = new ContentValues();
                            rowData.put(Applications_History.END_TIMESTAMP, System.currentTimeMillis());
                            try {
                                getContentResolver().update(Applications_History.CONTENT_URI, rowData, Applications_History._ID + "="+ appUnclosed.getInt(appUnclosed.getColumnIndex(Applications_History._ID)), null);
                            }catch( SQLiteException e ) {
                                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                            }catch( SQLException e) {
                                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                            }
                            
                            //Insert new importance
                            rowData = new ContentValues();
                            rowData.put(Applications_History.TIMESTAMP, System.currentTimeMillis());
                            rowData.put(Applications_History.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
                            rowData.put(Applications_History.PACKAGE_NAME, app.processName);
                            rowData.put(Applications_History.APPLICATION_NAME, appName);
                            rowData.put(Applications_History.PROCESS_IMPORTANCE, app.importance);
                            rowData.put(Applications_History.PROCESS_ID, app.pid);
                            rowData.put(Applications_History.END_TIMESTAMP, 0);
                            rowData.put(Applications_History.IS_SYSTEM_APP, isSystemPackage(appPkg));
                            try {
                                getContentResolver().insert(Applications_History.CONTENT_URI, rowData);
                            }catch( SQLiteException e ) {
                                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                            }catch( SQLException e ) {
                                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                            }
                        }
                    }catch(PackageManager.NameNotFoundException e) {
                    }catch( IllegalStateException e ) {
                    } finally {
                        if( appUnclosed != null && ! appUnclosed.isClosed() ) appUnclosed.close();
                    }
                }
                    
                //Close open applications that are not running anymore
                Cursor appsOpened = getContentResolver().query(Applications_History.CONTENT_URI, null, Applications_History.END_TIMESTAMP+"=0", null, null);
                try {
                    if(appsOpened != null && appsOpened.moveToFirst() ) {
                        do{
                            if( exists(runningApps, appsOpened) == false ) {
                                ContentValues rowData = new ContentValues();
                                rowData.put(Applications_History.END_TIMESTAMP, System.currentTimeMillis());
                                try {
                                    getContentResolver().update(Applications_History.CONTENT_URI, rowData, Applications_History._ID + "="+ appsOpened.getInt(appsOpened.getColumnIndex(Applications_History._ID)), null);
                                }catch( SQLiteException e ) {
                                    if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                                }catch( SQLException e) {
                                    if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                                }
                            }
                        } while(appsOpened.moveToNext());
                    }
                }catch(IllegalStateException e) {
                    if(Aware.DEBUG) Log.e(TAG,e.toString());
                }finally{
                    if(appsOpened != null && ! appsOpened.isClosed() ) appsOpened.close();
                }
                
                Intent statsUpdated = new Intent(ACTION_AWARE_APPLICATIONS_HISTORY);
                sendBroadcast(statsUpdated);
            }
        }
        
        /**
         * Check if the application on the database, exists on the running applications
         * @param {@link List}<RunningAppProcessInfo> runningApps
         * @param {@link Cursor} row
         * @return boolean
         */
        private boolean exists(List<RunningAppProcessInfo> running, Cursor dbApp) {
            for( RunningAppProcessInfo app : running ) {
                if(dbApp.getString(dbApp.getColumnIndexOrThrow(Applications_History.PACKAGE_NAME)).equals(app.processName) && 
                   dbApp.getInt(dbApp.getColumnIndexOrThrow(Applications_History.PROCESS_IMPORTANCE)) == app.importance &&
                   dbApp.getInt(dbApp.getColumnIndexOrThrow(Applications_History.PROCESS_ID)) == app.pid) {
                   return true; 
                }
            }
            return false;
        }
    }
    
    /**
     * Check if a certain application is pre-installed or part of the operating system.
     * @param {@link PackageInfo} obj
     * @return boolean
     */
    private static boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1);
    }
}
