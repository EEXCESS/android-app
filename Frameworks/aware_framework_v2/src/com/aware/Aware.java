/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aware.providers.Aware_Provider;
import com.aware.providers.Aware_Provider.Aware_Device;
import com.aware.providers.Aware_Provider.Aware_Plugins;
import com.aware.providers.Aware_Provider.Aware_Settings;
import com.aware.utils.Aware_Plugin;
import com.aware.utils.Http;
import com.aware.utils.WebserviceHelper;

/**
 * Main AWARE framework service. awareContext will start and manage all the services and settings.
 * @author denzil
 *
 */
public class Aware extends Service {

    /**
     * Debug flag (default = false).
     */
    public static boolean DEBUG = false;
    
    /**
     * Debug tag (default = "AWARE").
     */
    public static String TAG = "AWARE";
    
    /**
     * Broadcasted event: awareContext device information is available
     */
    public static final String ACTION_AWARE_DEVICE_INFORMATION = "ACTION_AWARE_DEVICE_INFORMATION";
    
    /**
     * Received broadcast on all modules
     * - Sends the data to the defined webserver
     */
    public static final String ACTION_AWARE_WEBSERVICE = "ACTION_AWARE_WEBSERVICE";
    
    /**
     * Received broadcast on all modules<br/>
     * - Cleans the data collected on the device
     */
    public static final String ACTION_AWARE_CLEAN_DATABASES = "ACTION_AWARE_CLEAN_DATABASES";
    
    /**
     * Received broadcast: change or add a new component configuration.<br/>
     * Extras:  {@link Aware_Preferences#EXTRA_SET_SETTING}<br/>
     *          {@link Aware_Preferences#EXTRA_SET_SETTING_VALUE}
     */
    public static final String ACTION_AWARE_CONFIGURATION = "ACTION_AWARE_CONFIGURATION";
    
    /**
     * Received broadcast: refresh the framework active sensors.<br/>
     */
    public static final String ACTION_AWARE_REFRESH = "ACTION_AWARE_REFRESH";
    
    /**
     * Received broadcast: plugins must implement awareContext broadcast receiver to share their current status.
     */
    public static final String ACTION_AWARE_CURRENT_CONTEXT = "ACTION_AWARE_CURRENT_CONTEXT";
    
    /**
     * Used by BackgroundService to start plugins
     */
    private static final String ACTION_AWARE_START_PLUGINS = "ACTION_AWARE_START_PLUGINS";
    
    /**
     * Used by BackgroundService to stop plugins
     */
    private static final String ACTION_AWARE_STOP_PLUGINS = "ACTION_AWARE_STOP_PLUGINS";
    
    /**
     * Used by plugin to stop all sensors
     */
    public static final String ACTION_AWARE_STOP_SENSORS = "ACTION_AWARE_STOP_SENSORS";
    
    /**
     * Notification ID that should be used by all core sensors in AWARE
     */
    public static final int NOTIFY_ID_AWARE = 777;
    
    /**
     * The framework's status check interval.
     * By default is 5 minutes/300 seconds
     */
    private static final int STATUS_MONITOR_INTERVAL = 300;
    
    /**
     * DownloadManager AWARE update ID, used to prompt user to install the update once finished downloading.
     */
    public static long AWARE_FRAMEWORK_DOWNLOAD_ID = 0;
    
    private static AlarmManager alarmManager = null;
    private static PendingIntent repeatingIntent = null;
    private static Context awareContext = null;
    
    private static Intent awareStatusMonitor = null;
    private static Intent applicationsSrv = null;
    private static Intent accelerometerSrv = null;
    private static Intent locationsSrv = null;
    private static Intent bluetoothSrv = null;
    private static Intent screenSrv = null;
    private static Intent batterySrv = null;
    private static Intent networkSrv = null;
    private static Intent trafficSrv = null;
    private static Intent communicationSrv = null;
    private static Intent processorSrv = null;
    private static Intent mqttSrv = null;
    private static Intent gyroSrv = null;
    private static Intent wifiSrv = null;
    private static Intent telephonySrv = null;
    private static Intent timeZoneSrv = null;
    private static Intent rotationSrv = null;
    private static Intent lightSrv = null;
    private static Intent proximitySrv = null;
    private static Intent magnetoSrv = null;
    private static Intent barometerSrv = null;
    private static Intent gravitySrv = null;
    private static Intent linear_accelSrv = null;
    private static Intent temperatureSrv = null;
    private static Intent esmSrv = null;
    private static Intent installationsSrv = null;
    
    /**
     * Singleton instance of the framework
     */
    private static Aware awareSrv = Aware.getService();
    
    /**
     * Get the singleton instance to the AWARE framework
     * @return {@link Aware} obj
     */
    public static Aware getService() {
        if( awareSrv == null ) awareSrv = new Aware();
        return awareSrv;
    }

    /**
     * Activity-Service binder
     */
    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        Aware getService() {
            return Aware.getService();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        awareContext = getApplicationContext();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        
        PreferenceManager.setDefaultValues(this, R.xml.aware_preferences, false);
        
        DEBUG = Aware.getSetting(awareContext.getContentResolver(),Aware_Preferences.DEBUG_FLAG).equals("true");
        TAG = Aware.getSetting(awareContext.getContentResolver(),Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(awareContext.getContentResolver(),Aware_Preferences.DEBUG_TAG):TAG;
        
        awareStatusMonitor = new Intent(getApplicationContext(), Aware.class);
        repeatingIntent = PendingIntent.getService(getApplicationContext(), 0,  awareStatusMonitor, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, STATUS_MONITOR_INTERVAL * 1000, repeatingIntent);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        awareContext.registerReceiver(storage_BR, filter);
        
        filter = new IntentFilter();
        filter.addAction(Aware.ACTION_AWARE_CLEAN_DATABASES);
        filter.addAction(Aware.ACTION_AWARE_CONFIGURATION);
        filter.addAction(Aware.ACTION_AWARE_REFRESH);
        filter.addAction(Aware.ACTION_AWARE_WEBSERVICE);
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        awareContext.registerReceiver(aware_BR, filter);
        
        if( Aware.getSetting(awareContext.getContentResolver(),Aware_Preferences.DEVICE_ID).length() == 0 ) {
            UUID uuid = UUID.randomUUID();
            Aware.setSetting(awareContext.getContentResolver(),Aware_Preferences.DEVICE_ID, uuid.toString());
        }
        
        get_device_info();
        new AsyncPing().execute();
        
        if( Aware.DEBUG ) Log.d(TAG,"AWARE framework is created!");
    }
    
    public static void load_preferences( ContentResolver cr, SharedPreferences prefs ) {
    	Map<String,?> defaults = prefs.getAll();
        for(Map.Entry<String, ?> entry : defaults.entrySet()) {
            if( Aware.getSetting(cr, entry.getKey()).length() == 0 ) {
                Aware.setSetting(cr, entry.getKey(), entry.getValue());
            }
        }
    }
    
    private class AsyncPing extends AsyncTask<Void, Void, Void> {
		private String DEVICE_ID = "";
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		DEVICE_ID = Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.DEVICE_ID);
    	}
    	
    	@Override
		protected Void doInBackground(Void... params) {
			//Ping AWARE's server with awareContext device's information for framework's statistics log
	        ArrayList<NameValuePair> device_ping = new ArrayList<NameValuePair>();
	        device_ping.add(new BasicNameValuePair("device_id", DEVICE_ID));
	        device_ping.add(new BasicNameValuePair("ping", ""+System.currentTimeMillis()));
	        new Http().dataPOST("http://www.awareframework.com/index.php/awaredev/alive", device_ping);
	        return null;
		}
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		if( Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_WEBSERVICE).equals("true")) {
    			sendBroadcast(new Intent(Aware.ACTION_AWARE_WEBSERVICE));
    		}
    	}
    }
    
    private void get_device_info() {
        Cursor awareContextDevice = awareContext.getContentResolver().query(Aware_Device.CONTENT_URI, null, null, null, null);
        if( awareContextDevice == null || ! awareContextDevice.moveToFirst() ) {
            ContentValues rowData = new ContentValues();
            rowData.put("timestamp", System.currentTimeMillis());
            rowData.put("device_id", Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.DEVICE_ID));
            rowData.put("board", Build.BOARD);
            rowData.put("brand", Build.BRAND);
            rowData.put("device",Build.DEVICE);
            rowData.put("build_id", Build.DISPLAY);
            rowData.put("hardware", Build.HARDWARE);
            rowData.put("manufacturer", Build.MANUFACTURER);
            rowData.put("model", Build.MODEL);
            rowData.put("product", Build.PRODUCT);
            rowData.put("serial", Build.SERIAL);
            rowData.put("release", Build.VERSION.RELEASE);
            rowData.put("release_type", Build.TYPE);
            rowData.put("sdk", Build.VERSION.SDK_INT);
            
            try {
                awareContext.getContentResolver().insert(Aware_Device.CONTENT_URI, rowData);
                
                Intent deviceData = new Intent(ACTION_AWARE_DEVICE_INFORMATION);
                sendBroadcast(deviceData);
                
                if( Aware.DEBUG ) Log.d(TAG, "Device information:"+ rowData.toString());
                
            }catch( SQLiteException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }
        }
        if( awareContextDevice != null && ! awareContextDevice.isClosed()) awareContextDevice.close();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        DEBUG = Aware.getSetting(awareContext.getContentResolver(),Aware_Preferences.DEBUG_FLAG).equals("true")?true:false;
        TAG = Aware.getSetting(awareContext.getContentResolver(),Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(awareContext.getContentResolver(),Aware_Preferences.DEBUG_TAG):TAG;
        
        if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
            if( Aware.DEBUG ) Log.d(TAG,"AWARE framework is active...");
            startAllServices();
            
            Intent startFramework = new Intent(getApplicationContext(), BackgroundService.class);
            startFramework.setAction(ACTION_AWARE_START_PLUGINS);
            awareContext.startService(startFramework);
            
            if( Aware.getSetting(getContentResolver(), Aware_Preferences.AWARE_AUTO_UPDATE).equals("true") ) {
            	new Update_Check().execute();
            }
            
        } else {
            Intent startFramework = new Intent(getApplicationContext(), BackgroundService.class);
            startFramework.setAction(ACTION_AWARE_STOP_PLUGINS);
            awareContext.startService(startFramework);
            
            stopAllServices();
            
            if( Aware.DEBUG ) Log.w(TAG,"AWARE framework is on hold...");
        }
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        alarmManager.cancel(repeatingIntent);
        awareContext.unregisterReceiver(aware_BR);
        awareContext.unregisterReceiver(storage_BR);
    }
    
    private class Update_Check extends AsyncTask<Void, Void, Void> {
    	@Override
    	protected Void doInBackground(Void... params) {
    		PackageInfo awarePkg = null;
			try {
				awarePkg = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
				return null;
			}
			
    		HttpResponse response = new Http().dataGET("http://www.awareframework.com/index.php/awaredev/framework_latest");
	        if( response != null && response.getStatusLine().getStatusCode() == 200 ) {
	        	try {
					JSONArray data = new JSONArray(EntityUtils.toString(response.getEntity()));
					JSONObject latest_framework = data.getJSONObject(0);
					
					if( Aware.DEBUG ) Log.d(Aware.TAG, "Latest:" + latest_framework.toString());
					
					String filename = latest_framework.getString("filename");
					int version = latest_framework.getInt("version");
					String whats_new = latest_framework.getString("whats_new");
					
					if( version > awarePkg.versionCode ) {
						update_framework( filename, whats_new );
					}
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
	        } else {
	        	if( Aware.DEBUG ) Log.d(Aware.TAG, "Unable to fetch latest framework from AWARE repository...");
	        }
    		
    		return null;
    	}
    	
    	private void update_framework( String filename, String whats_new ) {
    		//Make sure we have the releases folder
    		File releases = new File(Environment.getExternalStorageDirectory()+"/AWARE/releases/");
    		releases.mkdirs();
    		
    		String url = "http://www.awareframework.com/releases/" + filename;
    		
    		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
    		request.setDescription(whats_new);
    		request.setTitle("AWARE");
    		request.setDestinationInExternalPublicDir("/", "AWARE/releases/"+filename);
    		DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    		Aware.AWARE_FRAMEWORK_DOWNLOAD_ID = manager.enqueue(request);
    	}
    }
    
    /**
     * BroadcastReceiver that monitors for AWARE framework actions:
     * - ACTION_AWARE_WEBSERVICE = upload data to remote webservice server.
     * - ACTION_AWARE_CLEAN_DATABASES = clears local device's AWARE modules databases.
     * - ACTION_AWARE_CONFIGURATION = change settings from the framework.
     * - ACTION_AWARE_REFRESH - apply changes to the configuration.
     * - {@link DownloadManager#ACTION_DOWNLOAD_COMPLETE}
     * @author denzil
     *
     */
    public static class Aware_Broadcaster extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            
        	String[] DATABASE_TABLES = Aware_Provider.DATABASE_TABLES;
        	String[] TABLES_FIELDS = Aware_Provider.TABLES_FIELDS;
        	Uri[] CONTEXT_URIS = new Uri[]{ Aware_Device.CONTENT_URI };
        	
        	if( intent.getAction().equals(Aware.ACTION_AWARE_WEBSERVICE) && Aware.getSetting(context.getContentResolver(), Aware_Preferences.STATUS_WEBSERVICE).equals("true") ) {
            	Intent webserviceHelper = new Intent(WebserviceHelper.ACTION_AWARE_WEBSERVICE_SYNC_TABLE);
    			webserviceHelper.putExtra(WebserviceHelper.EXTRA_TABLE, DATABASE_TABLES[0]);
        		webserviceHelper.putExtra(WebserviceHelper.EXTRA_FIELDS, TABLES_FIELDS[0]);
        		webserviceHelper.putExtra(WebserviceHelper.EXTRA_CONTENT_URI, CONTEXT_URIS[0].toString());
        		context.startService(webserviceHelper);
            }
        	
            if( intent.getAction().equals(Aware.ACTION_AWARE_CLEAN_DATABASES) ) {
                context.getContentResolver().delete(Aware_Provider.Aware_Device.CONTENT_URI, null, null);
                if( Aware.DEBUG ) Log.d(TAG,"Cleared " + CONTEXT_URIS[0]);
                
                //Clear remotely if webservices are active
                if( Aware.getSetting(context.getContentResolver(), Aware_Preferences.STATUS_WEBSERVICE).equals("true") ) {
	        		Intent webserviceHelper = new Intent(WebserviceHelper.ACTION_AWARE_WEBSERVICE_CLEAR_TABLE);
	        		webserviceHelper.putExtra(WebserviceHelper.EXTRA_TABLE, DATABASE_TABLES[0]);
	        		context.startService(webserviceHelper);
                }
            }
            
            if( intent.getAction().equals(Aware.ACTION_AWARE_CONFIGURATION) ) {
                setSetting(context.getContentResolver(), intent.getStringExtra(Aware_Settings.SETTING_KEY), intent.getStringExtra(Aware_Settings.SETTING_VALUE));
                Intent restart = new Intent(context, Aware.class);
                context.startService(restart);
            }
            
            if( intent.getAction().equals(Aware.ACTION_AWARE_REFRESH)) {
                Intent refresh = new Intent(context, Aware.class);
                context.startService(refresh);
            }
            
            if( intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) ) {
            	DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            	long downloaded_id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            	
            	if( downloaded_id == AWARE_FRAMEWORK_DOWNLOAD_ID ) {
            		
            		if( Aware.DEBUG ) Log.d(Aware.TAG, "AWARE update received...");
            		
            		Query qry = new Query();
            		qry.setFilterById(AWARE_FRAMEWORK_DOWNLOAD_ID);
            		Cursor data = manager.query(qry);
            		if( data != null && data.moveToFirst() ) {
            			if( data.getInt(data.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL ) {
            				String filePath = data.getString(data.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            				File mFile = new File( Uri.parse(filePath).getPath() );
            				Intent promptUpdate = new Intent(Intent.ACTION_VIEW);
            				promptUpdate.setDataAndType(Uri.fromFile(mFile), "application/vnd.android.package-archive");
            				promptUpdate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            				context.startActivity(promptUpdate);
            			}
            		}
            		if( data != null && ! data.isClosed() ) data.close();
            	}
            }
        }
    }
    private static final Aware_Broadcaster aware_BR = new Aware_Broadcaster();
    
    public static class Storage_Broadcaster extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED) ) {
                if( Aware.DEBUG ) Log.d(TAG,"Resuming AWARE data logging...");
            }
            if ( intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED) ) {
                if( Aware.DEBUG ) Log.w(TAG,"Stopping AWARE data logging until the SDCard is available again...");
            }
            Intent aware = new Intent(context, Aware.class);
            context.startService(aware);
        }
    }
    private static final Storage_Broadcaster storage_BR = new Storage_Broadcaster();
    
    /**
     * Aware framework background service
     * - Backup the device information
     * @author df
     *
     */
    public static class BackgroundService extends IntentService {
        
        public BackgroundService() {
            super(TAG + " background service");
        }
        
        @Override
        protected void onHandleIntent(Intent intent) {
            if( intent.getAction().equals(Aware.ACTION_AWARE_START_PLUGINS)) {
                Cursor plugins = awareContext.getContentResolver().query(Aware_Plugins.CONTENT_URI, null, Aware_Plugins.PLUGIN_STATUS + "=" + Aware_Plugin.STATUS_PLUGIN_ON, null, null);
                if( plugins != null && plugins.moveToFirst() ) {
                    do {
                        Intent launch = new Intent();
                        launch.setClassName(plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME)), plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME))+".Plugin");
                        awareContext.startService(launch);
                    	
                        if( Aware.DEBUG ) Log.d(TAG,"Plugin "+ plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME)) + " is active...");
                    }while(plugins.moveToNext());
                }
                if( plugins!= null && ! plugins.isClosed() ) plugins.close();
            }
            
            if( intent.getAction().equals(Aware.ACTION_AWARE_STOP_PLUGINS) ) {
                Cursor plugins = awareContext.getContentResolver().query(Aware_Plugins.CONTENT_URI, null, null, null, null);
                if( plugins != null && plugins.moveToFirst() ) {
                    do {
                    	Intent terminate = new Intent();
                        terminate.setClassName(plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME)), plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME))+".Plugin");
                        awareContext.stopService(terminate);
                    	
                        if( Aware.DEBUG ) Log.d(TAG,"Plugin "+ plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME)) + " is terminated...");
                    }while(plugins.moveToNext());
                }
                if( plugins != null && ! plugins.isClosed() ) plugins.close();                
            }
        }
    }
    
    /**
     * Start active services
     */
    protected void startAllServices() {
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_APPLICATIONS).equals("true")) {
            startApplications();
        }else stopApplications();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_ACCELEROMETER).equals("true") ) {
            startAccelerometer();
        }else stopAccelerometer();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_INSTALLATIONS).equals("true")) {
            startInstallations();
        }else stopInstallations();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_LOCATION_GPS).equals("true") 
         || Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_LOCATION_NETWORK).equals("true") ) {
            startLocations();
        }else stopLocations();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_BLUETOOTH).equals("true") ) {
            startBluetooth();
        }else stopBluetooth();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_SCREEN).equals("true") ) {
            startScreen();
        }else stopScreen();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_BATTERY).equals("true") ) {
            startBattery();
        }else stopBattery();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_NETWORK).equals("true") ) {
            startNetwork();
        }else stopNetwork();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_NETWORK_TRAFFIC).equals("true") ) {
            startTraffic();
        }else stopTraffic();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_COMMUNICATION).equals("true") 
    	 || Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_CALLS).equals("true") 
    	 || Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_MESSAGES).equals("true") ) {
            startCommunication();
        }else stopCommunication();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_PROCESSOR).equals("true") ) {
            startProcessor();
        }else stopProcessor();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_TIMEZONE).equals("true") ) {
            startTimeZone();
        }else stopTimeZone();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_MQTT).equals("true") ) {
            startMQTT();
        }else stopMQTT();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_GYROSCOPE).equals("true") ) {
            startGyroscope();
        }else stopGyroscope();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_WIFI).equals("true") ) {
            startWiFi();
        }else stopWiFi();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_TELEPHONY).equals("true") ) {
            startTelephony();
        }else stopTelephony();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_ROTATION).equals("true") ) {
            startRotation();
        }else stopRotation();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_LIGHT).equals("true") ) {
            startLight();
        }else stopLight();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_PROXIMITY).equals("true") ) {
            startProximity();
        }else stopProximity();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_MAGNETOMETER).equals("true") ) {
            startMagnetometer();
        }else stopMagnetometer();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_BAROMETER).equals("true") ) {
            startBarometer();
        }else stopBarometer();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_GRAVITY).equals("true") ) {
            startGravity();
        }else stopGravity();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_LINEAR_ACCELEROMETER).equals("true") ) {
            startLinearAccelerometer();
        }else stopLinearAccelerometer();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_TEMPERATURE).equals("true") ) {
            startTemperature();
        }else stopTemperature();
        
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_ESM).equals("true") ) {
            startESM();
        }else stopESM();
    }
    
    /**
     * Stop all services
     */
    protected void stopAllServices() {
        stopApplications();
        stopAccelerometer();
        stopBattery();
        stopBluetooth();
        stopCommunication();
        stopLocations();
        stopNetwork();
        stopTraffic();
        stopScreen();
        stopProcessor();
        stopMQTT();
        stopGyroscope();
        stopWiFi();
        stopTelephony();
        stopTimeZone();
        stopRotation();
        stopLight();
        stopProximity();
        stopMagnetometer();
        stopBarometer();
        stopGravity();
        stopLinearAccelerometer();
        stopTemperature();
        stopESM();
        stopInstallations();
    }
    
    /**
     * Start Applications module
     */
    protected void startApplications() {
        if( applicationsSrv == null) applicationsSrv = new Intent(awareContext, Applications.class);
        awareContext.startService(applicationsSrv);
    }
    
    /**
     * Stop Applications module
     */
    protected void stopApplications() {
        if( applicationsSrv != null) awareContext.stopService(applicationsSrv);
    }
    
    /**
     * Start Installations module
     */
    protected void startInstallations() {
        if(installationsSrv == null) installationsSrv = new Intent(awareContext, Installations.class);
        awareContext.startService(installationsSrv);
    }
    
    /**
     * Stop Installations module
     */
    protected void stopInstallations() {
        if(installationsSrv != null) awareContext.stopService(installationsSrv);
    }
    
    /**
     * Start ESM module
     */
    protected void startESM() {
        if( esmSrv == null ) esmSrv = new Intent(awareContext, ESM.class);
        awareContext.startService(esmSrv);
    }
    
    /**
     * Stop ESM module
     */
    protected void stopESM() {
        if( esmSrv != null ) awareContext.stopService(esmSrv);
    }
    
    /**
     * Start Temperature module
     */
    protected void startTemperature() {
        if( temperatureSrv == null ) temperatureSrv = new Intent(awareContext, Temperature.class);
        awareContext.startService(temperatureSrv);
    }
    
    /**
     * Stop Temperature module
     */
    protected void stopTemperature() {
        if( temperatureSrv != null ) awareContext.stopService(temperatureSrv);
    }
    
    /**
     * Start Linear Accelerometer module
     */
    protected void startLinearAccelerometer() {
        if( linear_accelSrv == null ) linear_accelSrv = new Intent(awareContext, LinearAccelerometer.class);
        awareContext.startService(linear_accelSrv);
    }
    
    /**
     * Stop Linear Accelerometer module
     */
    protected void stopLinearAccelerometer() {
        if( linear_accelSrv != null ) awareContext.stopService(linear_accelSrv);
    }
    
    /**
     * Start Gravity module
     */
    protected void startGravity() {
        if( gravitySrv == null ) gravitySrv = new Intent(awareContext, Gravity.class);
        awareContext.startService(gravitySrv);
    }
    
    /**
     * Stop Gravity module
     */
    protected void stopGravity() {
        if( gravitySrv != null ) awareContext.stopService(gravitySrv);
    }
    
    /**
     * Start Barometer module
     */
    protected void startBarometer() {
        if( barometerSrv == null ) barometerSrv = new Intent(awareContext, Barometer.class);
        awareContext.startService(barometerSrv);
    }
    
    /**
     * Stop Barometer module
     */
    protected void stopBarometer() {
        if( barometerSrv != null ) awareContext.stopService(barometerSrv);
    }
    
    /**
     * Start Magnetometer module
     */
    protected void startMagnetometer() {
        if( magnetoSrv == null ) magnetoSrv = new Intent(awareContext, Magnetometer.class);
        awareContext.startService(magnetoSrv);
    }
    
    /**
     * Stop Magnetometer module
     */
    protected void stopMagnetometer() {
        if( magnetoSrv != null ) awareContext.stopService(magnetoSrv);
    }
    
    /**
     * Start Proximity module
     */
    protected void startProximity() {
        if( proximitySrv == null ) proximitySrv = new Intent(awareContext, Proximity.class);
        awareContext.startService(proximitySrv);
    }
    
    /**
     * Stop Proximity module
     */
    protected void stopProximity() {
        if( proximitySrv != null ) awareContext.stopService(proximitySrv);
    }
    
    /**
     * Start Light module
     */
    protected void startLight() {
        if( lightSrv == null ) lightSrv = new Intent(awareContext, Light.class);
        awareContext.startService(lightSrv);
    }
    
    /**
     * Stop Light module
     */
    protected void stopLight() {
        if( lightSrv != null ) awareContext.stopService(lightSrv);
    }
    
    /**
     * Start Rotation module
     */
    protected void startRotation() {
        if( rotationSrv == null ) rotationSrv = new Intent(awareContext, Rotation.class);
        awareContext.startService(rotationSrv);
    }
    
    /**
     * Stop Rotation module
     */
    protected void stopRotation() {
        if( rotationSrv != null ) awareContext.stopService(rotationSrv);
    }
    
    /**
     * Start the Telephony module
     */
    protected void startTelephony() {
        if( telephonySrv == null) telephonySrv = new Intent(awareContext, Telephony.class);
        awareContext.startService(telephonySrv);
    }
    
    /**
     * Stop the Telephony module
     */
    protected void stopTelephony() {
        if( telephonySrv != null ) awareContext.stopService(telephonySrv);
    }
    
    /**
     * Start the WiFi module
     */
    protected void startWiFi() {
        if( wifiSrv == null ) wifiSrv = new Intent(awareContext, WiFi.class);
        awareContext.startService(wifiSrv);
    }
    
    protected void stopWiFi() {
        if( wifiSrv != null ) awareContext.stopService(wifiSrv);
    }
    
    /**
     * Start the gyroscope module
     */
    protected void startGyroscope() {
        if( gyroSrv == null ) gyroSrv = new Intent(awareContext, Gyroscope.class);
        awareContext.startService(gyroSrv);
    }
    
    /**
     * Stop the gyroscope module
     */
    protected void stopGyroscope() {
        if( gyroSrv != null ) awareContext.stopService(gyroSrv);
    }
    
    /**
     * Start the accelerometer module
     */
    protected void startAccelerometer() {
        if( accelerometerSrv == null ) accelerometerSrv = new Intent(awareContext, Accelerometer.class);
        awareContext.startService(accelerometerSrv);
    }
    
    /**
     * Stop the accelerometer module
     */
    protected void stopAccelerometer() {
        if( accelerometerSrv != null) awareContext.stopService(accelerometerSrv);
    }
    
    /**
     * Start the Processor module
     */
    protected void startProcessor() {
        if( processorSrv == null) processorSrv = new Intent(awareContext, Processor.class);
        awareContext.startService(processorSrv);
    }
    
    /**
     * Stop the Processor module
     */
    protected void stopProcessor() {
        if( processorSrv != null ) awareContext.stopService(processorSrv);
    }
    
    /**
     * Start the locations module
     */
    protected void startLocations() {
        if( locationsSrv == null) locationsSrv = new Intent(awareContext, Locations.class);
        awareContext.startService(locationsSrv);
    }
    
    /**
     * Stop the locations module
     */
    protected void stopLocations() {
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_LOCATION_GPS).equals("false") 
         && Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_LOCATION_NETWORK).equals("false") ) {
            if(locationsSrv != null) awareContext.stopService(locationsSrv);
        }
    }
    
    /**
     * Start the bluetooth module
     */
    protected void startBluetooth() {
        if( bluetoothSrv == null) bluetoothSrv = new Intent(awareContext, Bluetooth.class);
        awareContext.startService(bluetoothSrv);
    }
    
    /**
     * Stop the bluetooth module
     */
    protected void stopBluetooth() {
        if(bluetoothSrv != null) awareContext.stopService(bluetoothSrv);
    }
    
    /**
     * Start the screen module
     */
    protected void startScreen() {
        if( screenSrv == null) screenSrv = new Intent(awareContext, Screen.class);
        awareContext.startService(screenSrv);
    }
    
    /**
     * Stop the screen module
     */
    protected void stopScreen() {
        if(screenSrv != null) awareContext.stopService(screenSrv);
    }
    
    /**
     * Start battery module
     */
    protected void startBattery() {
        if( batterySrv == null) batterySrv = new Intent(awareContext, Battery.class);
        awareContext.startService(batterySrv);
    }
    
    /**
     * Stop battery module
     */
    protected void stopBattery() {
        if(batterySrv != null) awareContext.stopService(batterySrv);
    }
    
    /**
     * Start network module
     */
    protected void startNetwork() {
        if( networkSrv == null ) networkSrv = new Intent(awareContext, Network.class);
        awareContext.startService(networkSrv);
    }
    
    /**
     * Stop network module
     */
    protected void stopNetwork() {
        if(networkSrv != null) awareContext.stopService(networkSrv);
    }
    
    /**
     * Start traffic module
     */
    protected void startTraffic() {
        if(trafficSrv == null) trafficSrv = new Intent(awareContext, Traffic.class);
        awareContext.startService(trafficSrv);
    }
    
    /**
     * Stop traffic module
     */
    protected void stopTraffic() {
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_NETWORK_TRAFFIC).equals("false") ) {
            if( trafficSrv != null ) awareContext.stopService(trafficSrv);
        }
    }
    
    /**
     * Start the TimeZone module
     */
    protected void startTimeZone() {
        if(timeZoneSrv == null) timeZoneSrv = new Intent(awareContext, TimeZone.class);
        awareContext.startService(timeZoneSrv);
    }
    
    /**
     * Stop the TimeZone module
     */
    protected void stopTimeZone() {
        if( timeZoneSrv != null ) awareContext.stopService(timeZoneSrv);
    }
    
    /**
     * Start communication module
     */
    protected void startCommunication() {
        if( communicationSrv == null ) communicationSrv = new Intent(awareContext, Communication.class);
        awareContext.startService(communicationSrv);
    }
    
    /**
     * Stop communication module
     */
    protected void stopCommunication() {
        if( Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_COMMUNICATION).equals("false") 
         && Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_CALLS).equals("false") 
         && Aware.getSetting(awareContext.getContentResolver(), Aware_Preferences.STATUS_MESSAGES).equals("false") ) {
            if(communicationSrv != null) awareContext.stopService(communicationSrv);
        }
    }
    
    /**
     * Start MQTT module
     */
    protected void startMQTT() {
        if( mqttSrv == null ) mqttSrv = new Intent(awareContext, Mqtt.class);
        awareContext.startService(mqttSrv);
    }
    
    /**
     * Stop MQTT module
     */
    protected void stopMQTT() {
        if( mqttSrv != null ) awareContext.stopService(mqttSrv);
    }
    
    /**
     * Retrieve setting value given key.
     * @param String key
     * @return String value
     */
    public static String getSetting( ContentResolver resolver, String key ) {
        String value = "";
        
        Cursor qry = resolver.query(Aware_Settings.CONTENT_URI, null, Aware_Settings.SETTING_KEY + " LIKE '" + key + "'", null, null);
        if( qry != null && qry.moveToFirst() ) {
            value = qry.getString(qry.getColumnIndex(Aware_Settings.SETTING_VALUE));
        }
        if( qry != null && ! qry.isClosed() ) qry.close();
        return value;
    }
    
    /**
     * Insert / Update settings of the framework
     * @param String key
     * @param String value
     * @return boolean if successful
     */
    public static void setSetting( ContentResolver resolver, String key, Object value ) {
        ContentValues setting = new ContentValues();
        setting.put(Aware_Settings.SETTING_KEY, key);
        setting.put(Aware_Settings.SETTING_VALUE, value.toString());
        
        Cursor qry = resolver.query(Aware_Settings.CONTENT_URI, null, Aware_Settings.SETTING_KEY + " LIKE '" + key + "'", null, null);
        //update
        if( qry != null && qry.moveToFirst() ) {
            try {
                if( ! qry.getString(qry.getColumnIndex(Aware_Settings.SETTING_VALUE)).equals(value.toString()) ) {
                    resolver.update(Aware_Settings.CONTENT_URI, setting, Aware_Settings.SETTING_ID + "=" + qry.getInt(qry.getColumnIndex(Aware_Settings.SETTING_ID)), null);
                    if( Aware.DEBUG) Log.d(Aware.TAG,"Updated: "+key+"="+value);
                }
            }catch( SQLiteException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }
        //insert
        } else {
            try {
                resolver.insert(Aware_Settings.CONTENT_URI, setting);
                if( Aware.DEBUG) Log.d(Aware.TAG,"Added: " + key + "=" + value);
                qry.close();
            }catch( SQLiteException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }
        }
        if( qry != null && ! qry.isClosed() ) qry.close();
    }
}
