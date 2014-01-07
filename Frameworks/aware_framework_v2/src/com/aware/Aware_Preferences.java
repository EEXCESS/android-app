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
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.aware.providers.Accelerometer_Provider.Accelerometer_Sensor;
import com.aware.providers.Barometer_Provider.Barometer_Sensor;
import com.aware.providers.Gravity_Provider.Gravity_Sensor;
import com.aware.providers.Gyroscope_Provider.Gyroscope_Sensor;
import com.aware.providers.Light_Provider.Light_Sensor;
import com.aware.providers.Linear_Accelerometer_Provider.Linear_Accelerometer_Sensor;
import com.aware.providers.Magnetometer_Provider.Magnetometer_Sensor;
import com.aware.providers.Proximity_Provider.Proximity_Sensor;
import com.aware.providers.Rotation_Provider.Rotation_Sensor;
import com.aware.providers.Temperature_Provider.Temperature_Sensor;
import com.aware.utils.Encrypter;
import com.aware.utils.Http;

/**
 * Preferences dashboard for the AWARE Aware
 * Allows the researcher to configure all the modules, start and stop modules and where logging happens.
 * @author df
 *
 */
@SuppressWarnings("deprecation")
public class Aware_Preferences extends PreferenceActivity {

    private static final Aware framework = Aware.getService();
    private static Context mContext = null;
    private static SensorManager mSensorMgr = null;
    private static SharedPreferences prefs = null;
    
    private static final int DIALOG_ERROR_ACCESSIBILITY = 1;
    private static final int DIALOG_ERROR_MISSING_PARAMETERS = 2;
    private static final int DIALOG_ERROR_MISSING_SENSOR = 3;
    
    /**
     * Broadcast extra for ACTION_AWARE_CONFIGURATION<br/>
     * Value: setting (String)
     */
    public static final String EXTRA_SET_SETTING = "setting";
    
    /**
     * Broadcast extra for ACTION_AWARE_CONFIGURATION<br/>
     * Value: value (String)
     */
    public static final String EXTRA_SET_SETTING_VALUE = "value";
    
    /**
     * AWARE preferences parameters
     */
    public static final String DEBUG_FLAG = "debug_flag";
    public static final String DEBUG_TAG = "debug_tag";
    public static final String DEVICE_ID = "device_id";
    public static final String AWARE_AUTO_UPDATE = "aware_auto_update";
    public static final String STATUS_ACCELEROMETER = "status_accelerometer";
    public static final String FREQUENCY_ACCELEROMETER = "frequency_accelerometer";
    public static final String STATUS_APPLICATIONS = "status_applications";
    public static final String STATUS_INSTALLATIONS = "status_installations";
    public static final String STATUS_NOTIFICATIONS = "status_notifications";
    public static final String STATUS_CRASHES = "status_crashes";
    public static final String STATUS_BATTERY = "status_battery";
    public static final String STATUS_BLUETOOTH = "status_bluetooth";
    public static final String FREQUENCY_BLUETOOTH = "frequency_bluetooth";
    public static final String STATUS_COMMUNICATION = "status_communication";
    public static final String STATUS_CALLS = "status_calls";
    public static final String STATUS_MESSAGES = "status_messages";
    public static final String STATUS_GRAVITY = "status_gravity";
    public static final String FREQUENCY_GRAVITY = "frequency_gravity";
    public static final String STATUS_GYROSCOPE = "status_gyroscope";
    public static final String FREQUENCY_GYROSCOPE = "frequency_gyroscope";
    public static final String STATUS_LOCATION_GPS = "status_location_gps";
    public static final String FREQUENCY_GPS = "frequency_gps";
    public static final String MIN_GPS_ACCURACY = "min_gps_accuracy";
    public static final String STATUS_LOCATION_NETWORK = "status_location_network";
    public static final String FREQUENCY_NETWORK = "frequency_network";
    public static final String MIN_NETWORK_ACCURACY = "min_network_accuracy";
    public static final String EXPIRATION_TIME = "expiration_time";
    public static final String STATUS_LIGHT = "status_light";
    public static final String FREQUENCY_LIGHT = "frequency_light";
    public static final String STATUS_LINEAR_ACCELEROMETER = "status_linear_accelerometer";
    public static final String FREQUENCY_LINEAR_ACCELEROMETER = "frequency_linear_accelerometer";
    public static final String STATUS_NETWORK = "status_network";
    public static final String STATUS_NETWORK_TRAFFIC = "status_network_traffic";
    public static final String FREQUENCY_TRAFFIC = "frequency_traffic";
    public static final String STATUS_MAGNETOMETER = "status_magnetometer";
    public static final String FREQUENCY_MAGNETOMETER = "frequency_magnetometer";
    public static final String STATUS_BAROMETER = "status_barometer";
    public static final String FREQUENCY_BAROMETER = "frequency_barometer";
    public static final String STATUS_PROCESSOR = "status_processor";
    public static final String FREQUENCY_PROCESSOR = "frequency_processor";
    public static final String STATUS_TIMEZONE = "status_timezone";
    public static final String FREQUENCY_TIMEZONE = "frequency_timezone";
    public static final String STATUS_PROXIMITY = "status_proximity";
    public static final String FREQUENCY_PROXIMITY = "frequency_proximity";
    public static final String STATUS_ROTATION = "status_rotation";
    public static final String FREQUENCY_ROTATION = "frequency_rotation";
    public static final String STATUS_SCREEN = "status_screen";
    public static final String STATUS_TEMPERATURE = "status_temperature";
    public static final String FREQUENCY_TEMPERATURE = "frequency_temperature";
    public static final String STATUS_TELEPHONY = "status_telephony";
    public static final String STATUS_WIFI = "status_wifi";
    public static final String FREQUENCY_WIFI = "frequency_wifi";
    public static final String STATUS_ESM = "status_esm";
    public static final String STATUS_MQTT = "status_mqtt";
    public static final String MQTT_SERVER = "mqtt_server";
    public static final String MQTT_PORT = "mqtt_port";
    public static final String MQTT_USERNAME = "mqtt_username";
    public static final String MQTT_PASSWORD = "mqtt_password";
    public static final String MQTT_KEEP_ALIVE = "mqtt_keep_alive";
    public static final String MQTT_QOS = "mqtt_qos";
    public static final String STATUS_WEBSERVICE = "status_webservice";
    public static final String WEBSERVICE_SERVER = "webservice_server";
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        switch(id) {
            case DIALOG_ERROR_ACCESSIBILITY:
                builder.setMessage("Please activate AWARE on the Accessibility Services!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent accessibilitySettings = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        accessibilitySettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                        startActivity(accessibilitySettings);
                    }
                });
                dialog = builder.create();
            break;
            case DIALOG_ERROR_MISSING_PARAMETERS:
                builder.setMessage("Some parameters are missing...");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
            break;
            case DIALOG_ERROR_MISSING_SENSOR:
                builder.setMessage("This device is missing this sensor.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
            break;
        }
        return dialog;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContext = getApplicationContext();
        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        //Start the Aware
        Intent startAware = new Intent(this, Aware.class);
        startService(startAware);
        
        loadPrefs();
    }
    
    private void loadPrefs() {
        addPreferencesFromResource(R.xml.aware_preferences);
        PreferenceManager.setDefaultValues(this, R.xml.aware_preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Aware.load_preferences(getContentResolver(), prefs);
        
        if( Aware.getSetting(getContentResolver(),"device_id").length() == 0 ) {
            UUID uuid = UUID.randomUUID();
            Aware.setSetting(getContentResolver(),"device_id", uuid.toString());
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        setUIElements();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUIElements();        
    }
    
    private void setUIElements() {
        developerOptions();
        servicesOptions();
    }
    
    /**
     * Check if the accessibility service for AWARE Aware is active
     * @return boolean isActive
     */
    private boolean isAccessibilityServiceActive() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if( accessibilityManager.isEnabled() ) {
            List<ServiceInfo> accessibilityServices = accessibilityManager.getAccessibilityServiceList();
            for( ServiceInfo s : accessibilityServices ) {
                if( s.name.equalsIgnoreCase("com.aware.Applications") || s.name.equalsIgnoreCase("com.aware.ApplicationsJB") ) {
                    return true;
                }
            }
        }
        return false;
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
			
    		HttpResponse response = new Http().dataGET("http://www.awareframework.com/index.php/awaredev/latest");
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
     * Developer UI options
     * - Debug flag
     * - Debug tag
     * - AWARE updates
     * - Device ID
     */
    private void developerOptions() {
        final CheckBoxPreference debug_flag = (CheckBoxPreference) findPreference("debug_flag");
        debug_flag.setChecked(Aware.getSetting(getContentResolver(),"debug_flag").equals("true")?true:false);
        debug_flag.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                Aware.DEBUG = debug_flag.isChecked();
                Aware.setSetting(getContentResolver(),"debug_flag", debug_flag.isChecked()?"true":"false");
                
                return true;
            }
        });
        
        final EditTextPreference debug_tag = (EditTextPreference) findPreference("debug_tag");
        debug_tag.setText(Aware.getSetting(getContentResolver(),"debug_tag"));
        debug_tag.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.TAG = (String) newValue;
                Aware.setSetting(getContentResolver(),"debug_tag", (String) newValue);
                return true;
            }
        });
        
        final CheckBoxPreference auto_update = (CheckBoxPreference) findPreference(AWARE_AUTO_UPDATE);
        auto_update.setChecked(Aware.getSetting(getContentResolver(),AWARE_AUTO_UPDATE).equals("true")?true:false);
        
        PackageInfo awareInfo = null;
		try {
			awareInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		auto_update.setSummary("Current version is " + ((awareInfo != null)?awareInfo.versionCode:"???"));
        auto_update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),AWARE_AUTO_UPDATE, auto_update.isChecked());
                if( auto_update.isChecked() ) {
                	new Update_Check().execute();
                }
                return true;
            }
        });
        
        final EditTextPreference device_id = (EditTextPreference) findPreference("device_id");
        device_id.setSummary("UUID: " + Aware.getSetting(getContentResolver(), DEVICE_ID));
        device_id.setText(Aware.getSetting(getContentResolver(),"device_id"));
        device_id.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"device_id", (String) newValue);
                device_id.setSummary("UUID: " + Aware.getSetting(getContentResolver(), DEVICE_ID));
                return true;
            }
        });
    }
    
    /**
     * AWARE services UI components
     */
    private void servicesOptions() {
        esm();
        accelerometer();
        applications();
        barometer();
        battery();
        bluetooth();
        communication();
        gyroscope();
        light();
        linear_accelerometer();
        locations();
        magnetometer();
        network();
        screen();
        wifi();
        processor();
        timeZone();
        proximity();
        rotation();
        telephony();
        logging();
        gravity();
        temperature();
    }
    
    /**
     * ESM module settings UI
     */
    private void esm() {
        final CheckBoxPreference esm = (CheckBoxPreference) findPreference("status_esm");
        esm.setChecked(Aware.getSetting(getContentResolver(),"status_esm").equals("true")?true:false);
        esm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_esm",esm.isChecked()?"true":"false");
                
                if(esm.isChecked()) {
                    framework.startESM();
                }else {
                    framework.stopESM();
                }
                return true;
            }
        });
    }
    
    /**
     * Temperature module settings UI
     */
    private void temperature() {
    	final PreferenceScreen temp_pref = (PreferenceScreen) findPreference("temperature");
    	Cursor sensorProfile = getContentResolver().query(Temperature_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Temperature_Sensor.POWER_MA));
    		temp_pref.setSummary(temp_pref.getSummary().toString().replace("*", "- Drain: " + power_ma +" mA"));
    	} else {
    		temp_pref.setSummary(temp_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference temperature = (CheckBoxPreference) findPreference("status_temperature");
        temperature.setChecked(Aware.getSetting(getContentResolver(),"status_temperature").equals("true")?true:false);
        temperature.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_TEMPERATURE) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    temperature.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_temperature","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_temperature",temperature.isChecked()?"true":"false");
                
                if(temperature.isChecked()) {
                    framework.startTemperature();
                }else {
                    framework.stopTemperature();
                }
                return true;
            }
        });
        
        final EditTextPreference frequency_temperature = (EditTextPreference) findPreference("frequency_temperature");
        frequency_temperature.setText(Aware.getSetting(getContentResolver(),"frequency_temperature"));
        frequency_temperature.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_temperature", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Temperature.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
    }
    
    /**
     * Accelerometer module settings UI
     */
    private void accelerometer() {
        
    	final PreferenceScreen accel_pref = (PreferenceScreen) findPreference("accelerometer");
    	Cursor sensorProfile = getContentResolver().query(Accelerometer_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Accelerometer_Sensor.POWER_MA));
    		accel_pref.setSummary(accel_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		accel_pref.setSummary(accel_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference accelerometer = (CheckBoxPreference) findPreference("status_accelerometer");
        accelerometer.setChecked(Aware.getSetting(getContentResolver(),"status_accelerometer").equals("true")?true:false);
        accelerometer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    accelerometer.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_accelerometer","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_accelerometer",accelerometer.isChecked()?"true":"false");
                
                if(accelerometer.isChecked()) {
                    framework.startAccelerometer();
                }else {
                    framework.stopAccelerometer();
                }
                return true;
            }
        });
        
        final EditTextPreference frequency_accelerometer = (EditTextPreference) findPreference("frequency_accelerometer");
        frequency_accelerometer.setText(Aware.getSetting(getContentResolver(),"frequency_accelerometer"));
        frequency_accelerometer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_accelerometer", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Accelerometer.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
        
    }
    
    /**
     * Linear Accelerometer module settings UI
     */
    private void linear_accelerometer() {
        
    	final PreferenceScreen linear_pref = (PreferenceScreen) findPreference("linear_accelerometer");
    	Cursor sensorProfile = getContentResolver().query(Linear_Accelerometer_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Linear_Accelerometer_Sensor.POWER_MA));
    		linear_pref.setSummary(linear_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		linear_pref.setSummary(linear_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference linear_accelerometer = (CheckBoxPreference) findPreference("status_linear_accelerometer");
        linear_accelerometer.setChecked(Aware.getSetting(getContentResolver(),"status_linear_accelerometer").equals("true")?true:false);
        linear_accelerometer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    linear_accelerometer.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_linear_accelerometer","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_linear_accelerometer",linear_accelerometer.isChecked()?"true":"false");
                
                if(linear_accelerometer.isChecked()) {
                    framework.startLinearAccelerometer();
                }else {
                    framework.stopLinearAccelerometer();
                }
                return true;
            }
        });
        
        final EditTextPreference frequency_linear_accelerometer = (EditTextPreference) findPreference("frequency_linear_accelerometer");
        frequency_linear_accelerometer.setText(Aware.getSetting(getContentResolver(),"frequency_linear_accelerometer"));
        frequency_linear_accelerometer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_linear_accelerometer", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, LinearAccelerometer.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
        
    }
    
    /**
     * Applications module settings UI
     */
    private void applications() {
    	final CheckBoxPreference notifications = (CheckBoxPreference) findPreference(Aware_Preferences.STATUS_NOTIFICATIONS);
        notifications.setChecked(Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_NOTIFICATIONS).equals("true")?true:false);
        notifications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if( isAccessibilityServiceActive() && notifications.isChecked() ) {
					Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_NOTIFICATIONS, notifications.isChecked());
					notifications.setChecked(true);
					framework.startApplications();
					return true;
				}
				if (! isAccessibilityServiceActive() ) {
					showDialog(Aware_Preferences.DIALOG_ERROR_ACCESSIBILITY);
				}
				Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_NOTIFICATIONS, false);
				notifications.setChecked(false);
				return false;
			}
		});
        final CheckBoxPreference crashes = (CheckBoxPreference) findPreference(Aware_Preferences.STATUS_CRASHES);
        crashes.setChecked(Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_CRASHES).equals("true")?true:false);
        crashes.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if( isAccessibilityServiceActive() && crashes.isChecked() ) {
					Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_CRASHES, crashes.isChecked());
					crashes.setChecked(true);
					framework.startApplications();
					return true;
				}
				if (! isAccessibilityServiceActive() ) {
					showDialog(Aware_Preferences.DIALOG_ERROR_ACCESSIBILITY);
				}
				Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_CRASHES, false);
				crashes.setChecked(false);
				return false;
			}
		});
    	final CheckBoxPreference applications = (CheckBoxPreference) findPreference("status_applications");
        if( Aware.getSetting(getContentResolver(), "status_applications").equals("true") && isAccessibilityServiceActive()==false ) {
            showDialog(Aware_Preferences.DIALOG_ERROR_ACCESSIBILITY);
            Aware.setSetting(getContentResolver(),"status_applications","false");
            framework.stopApplications();
        }
        applications.setChecked(Aware.getSetting(getContentResolver(), Aware_Preferences.STATUS_APPLICATIONS).equals("true")?true:false);
        applications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if( isAccessibilityServiceActive() && applications.isChecked() ) {
                    Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_APPLICATIONS, true);
                    applications.setChecked(true);
                    framework.startApplications();
                    return true;
                }else {
                    if( ! isAccessibilityServiceActive() ) {
                        showDialog(Aware_Preferences.DIALOG_ERROR_ACCESSIBILITY);
                    }  
                    
                    Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_APPLICATIONS, false);
                    applications.setChecked(false);
                    
                    Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_NOTIFICATIONS, false);
                    notifications.setChecked(false);
                    
                    Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_CRASHES, false);
                    crashes.setChecked(false);
                    
                    framework.stopApplications();
                    return false;
                }
            }
        });
        
        final CheckBoxPreference installations = (CheckBoxPreference) findPreference("status_installations");
        installations.setChecked(Aware.getSetting(getContentResolver(),"status_installations").equals("true")?true:false);
        installations.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_installations",installations.isChecked()?"true":"false");
                
                if(installations.isChecked()) {
                    framework.startInstallations();
                }else {
                    framework.stopInstallations();
                }
                return true;
            }
        });
    }
    
    /**
     * Battery module settings UI
     */
    private void battery() {
        final CheckBoxPreference battery = (CheckBoxPreference) findPreference("status_battery");
        battery.setChecked(Aware.getSetting(getContentResolver(),"status_battery").equals("true")?true:false);
        battery.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                Aware.setSetting(getContentResolver(),"status_battery",battery.isChecked()?"true":"false");
                
                if(battery.isChecked()) {
                    framework.startBattery();
                }else {
                    framework.stopBattery();
                }
                
                return true;
            }
        });
    }
    
    /**
     * Bluetooth module settings UI
     */
    private void bluetooth() {
        final CheckBoxPreference bluetooth = (CheckBoxPreference) findPreference("status_bluetooth");
        bluetooth.setChecked(Aware.getSetting(getContentResolver(),"status_bluetooth").equals("true")?true:false);
        bluetooth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                if( btAdapter == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    bluetooth.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_bluetooth","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_bluetooth",bluetooth.isChecked()?"true":"false");
                
                if(bluetooth.isChecked()) {
                    framework.startBluetooth();
                }else {
                    framework.stopBluetooth();
                }
                
                return true;
            }
        });
        
        final EditTextPreference bluetoothInterval = (EditTextPreference) findPreference("frequency_bluetooth");
        bluetoothInterval.setText(Aware.getSetting(getContentResolver(),"frequency_bluetooth"));
        bluetoothInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                
                Aware.setSetting(getContentResolver(),"frequency_bluetooth", (String) newValue);
                framework.startBluetooth();
                
                return true;
            }
        });
    }
    
    /**
     * Communication module settings UI
     */
    private void communication() {
        final CheckBoxPreference calls = (CheckBoxPreference) findPreference("status_calls");
        calls.setChecked(Aware.getSetting(getContentResolver(),"status_calls").equals("true")?true:false);
        calls.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                Aware.setSetting(getContentResolver(),"status_calls",calls.isChecked()?"true":"false");
                
                if(calls.isChecked()) {
                    framework.startCommunication();
                } else {
                    framework.stopCommunication();
                }
                return true;
            }
        });
        
        final CheckBoxPreference messages = (CheckBoxPreference) findPreference("status_messages");
        messages.setChecked(Aware.getSetting(getContentResolver(),"status_messages").equals("true")?true:false);
        messages.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_messages",messages.isChecked()?"true":"false");
                
                if(messages.isChecked()) {
                    framework.startCommunication();
                } else {
                    framework.stopCommunication();
                }
                return true;
            }
        });
        
        final CheckBoxPreference communication = (CheckBoxPreference) findPreference("status_communication");
        communication.setChecked(Aware.getSetting(getContentResolver(),"status_communication").equals("true")?true:false);
        communication.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_communication",communication.isChecked()?"true":"false");
                
                if(communication.isChecked()) {
                    framework.startCommunication();
                } else {
                    framework.stopCommunication();
                }
                
                return true;
            }
        });
    }
    
    /**
     * Gravity module settings UI
     */
    private void gravity() {
        
    	final PreferenceScreen grav_pref = (PreferenceScreen) findPreference("gravity");
    	Cursor sensorProfile = getContentResolver().query(Gravity_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Gravity_Sensor.POWER_MA));
    		grav_pref.setSummary(grav_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		grav_pref.setSummary(grav_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference gravity = (CheckBoxPreference) findPreference("status_gravity");
        gravity.setChecked(Aware.getSetting(getContentResolver(),"status_gravity").equals("true")?true:false);
        gravity.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_GRAVITY) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    gravity.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_gravity","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_gravity",gravity.isChecked()?"true":"false");
                
                if(gravity.isChecked()) {
                    framework.startGravity();
                }else {
                    framework.stopGravity();
                }
                return true;
            }
        });
        
        final EditTextPreference frequency_gravity = (EditTextPreference) findPreference("frequency_gravity");
        frequency_gravity.setText(Aware.getSetting(getContentResolver(),"frequency_gravity"));
        frequency_gravity.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_gravity", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Gravity.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
        
    }
    
    /**
     * Gyroscope module settings UI
     */
    private void gyroscope() {
        
    	final PreferenceScreen gyro_pref = (PreferenceScreen) findPreference("gyroscope");
    	Cursor sensorProfile = getContentResolver().query(Gyroscope_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Gyroscope_Sensor.POWER_MA));
    		gyro_pref.setSummary(gyro_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		gyro_pref.setSummary(gyro_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference gyroscope = (CheckBoxPreference) findPreference("status_gyroscope");
        gyroscope.setChecked(Aware.getSetting(getContentResolver(),"status_gyroscope").equals("true")?true:false);
        gyroscope.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    gyroscope.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_gyroscope","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_gyroscope",gyroscope.isChecked()?"true":"false");
                
                if(gyroscope.isChecked()) {
                    framework.startGyroscope();
                }else {
                    framework.stopGyroscope();
                }
                return true;
            }
        });
        
        final EditTextPreference frequency_gyroscope = (EditTextPreference) findPreference("frequency_gyroscope");
        frequency_gyroscope.setText(Aware.getSetting(getContentResolver(),"frequency_gyroscope"));
        frequency_gyroscope.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_gyroscope", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Gyroscope.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
        
    }
    
    /**
     * Location module settings UI
     */
    private void locations() {
        
        final CheckBoxPreference location_gps = (CheckBoxPreference) findPreference("status_location_gps");
        location_gps.setChecked(Aware.getSetting(getContentResolver(),"status_location_gps").equals("true")?true:false);
        location_gps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                LocationManager localMng = (LocationManager) getSystemService(LOCATION_SERVICE);
                List<String> providers = localMng.getAllProviders();
                
                if( ! providers.contains(LocationManager.GPS_PROVIDER) ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    location_gps.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_location_gps","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_location_gps",location_gps.isChecked()?"true":"false");
                
                if(location_gps.isChecked()) {
                    framework.startLocations();
                }else {
                    framework.stopLocations();
                }
                return true;
            }
        });
        
        final CheckBoxPreference location_network = (CheckBoxPreference) findPreference("status_location_network");
        location_network.setChecked(Aware.getSetting(getContentResolver(),"status_location_network").equals("true")?true:false);
        location_network.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                LocationManager localMng = (LocationManager) getSystemService(LOCATION_SERVICE);
                List<String> providers = localMng.getAllProviders();
                
                if( ! providers.contains(LocationManager.NETWORK_PROVIDER) ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    location_gps.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_location_network","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_location_network",location_network.isChecked()?"true":"false");
                
                if(location_network.isChecked()) {
                    framework.startLocations();
                }else {
                    framework.stopLocations();
                }
                
                return true;
            }
        });
        
        final EditTextPreference gpsInterval = (EditTextPreference) findPreference("frequency_gps");
        gpsInterval.setText(Aware.getSetting(getContentResolver(),"frequency_gps"));
        gpsInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                
                Aware.setSetting(getContentResolver(),"frequency_gps", (String) newValue);
                
                framework.startLocations();
                
                return true;
            }
        });
        
        final EditTextPreference networkInterval = (EditTextPreference) findPreference("frequency_network");
        networkInterval.setText(Aware.getSetting(getContentResolver(),"frequency_network"));
        networkInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_network", (String) newValue);
                
                framework.startLocations();
                
                return true;
            }
        });
        
        final EditTextPreference gpsAccuracy = (EditTextPreference) findPreference("min_gps_accuracy");
        gpsAccuracy.setText(Aware.getSetting(getContentResolver(),"min_gps_accuracy"));
        gpsAccuracy.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"min_gps_accuracy", (String) newValue);
                
                framework.startLocations();
                
                return true;
            }
        });
        
        final EditTextPreference networkAccuracy = (EditTextPreference) findPreference("min_network_accuracy");
        networkAccuracy.setText(Aware.getSetting(getContentResolver(),"min_network_accuracy"));
        networkAccuracy.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"min_network_accuracy", (String) newValue);
                
                framework.startLocations();
                
                return true;
            }
        });
        
        final EditTextPreference expirateTime = (EditTextPreference) findPreference("expiration_time");
        expirateTime.setText(Aware.getSetting(getContentResolver(),"expiration_time"));
        expirateTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"expiration_time", (String) newValue);
                
                framework.startLocations();
                
                return true;
            }
        });
    }

    /**
     * Network module settings UI
     */
    private void network() {
        final CheckBoxPreference network_traffic = (CheckBoxPreference) findPreference("status_network_traffic");
        network_traffic.setChecked(Aware.getSetting(getContentResolver(),"status_network_traffic").equals("true")?true:false);
        network_traffic.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_network_traffic",network_traffic.isChecked()?"true":"false");
                
                if(network_traffic.isChecked()) {
                    framework.startTraffic();
                }else {
                    framework.stopTraffic();
                }
                
                return true;
            }
        });
        
        final EditTextPreference frequencyTraffic = (EditTextPreference) findPreference("frequency_traffic");
        frequencyTraffic.setText(Aware.getSetting(getContentResolver(),"frequency_traffic"));
        frequencyTraffic.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_traffic", (String) newValue);
                
                if( network_traffic.isChecked() ) {
                    framework.startTraffic();
                }
                
                return true;
            }
        });
        
        final CheckBoxPreference network = (CheckBoxPreference) findPreference("status_network");
        network.setChecked(Aware.getSetting(getContentResolver(),"status_network").equals("true")?true:false);
        network.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_network",network.isChecked()?"true":"false");
                
                if(network.isChecked()) {
                    framework.startNetwork();
                }else {
                    framework.stopNetwork();
                }
                
                return true;
            }
        });
    }
    
    /**
     * Screen module settings UI
     */
    private void screen () {
        final CheckBoxPreference screen = (CheckBoxPreference) findPreference("status_screen");
        screen.setChecked(Aware.getSetting(getContentResolver(),"status_screen").equals("true")?true:false);
        screen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_screen",screen.isChecked()?"true":"false");
                
                if(screen.isChecked()) {
                    framework.startScreen();
                }else {
                    framework.stopScreen();
                }
                
                return true;
            }
        });
    }
    
    /**
     * WiFi module settings UI
     */
    private void wifi() {
        final CheckBoxPreference wifi = (CheckBoxPreference) findPreference("status_wifi");
        wifi.setChecked(Aware.getSetting(getContentResolver(),"status_wifi").equals("true")?true:false);
        wifi.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_wifi",wifi.isChecked()?"true":"false");
                
                if(wifi.isChecked()) {
                    framework.startWiFi();
                }else {
                    framework.stopWiFi();
                }
                
                return true;
            }
        });
        
        final EditTextPreference wifiInterval = (EditTextPreference) findPreference("frequency_wifi");
        wifiInterval.setText(Aware.getSetting(getContentResolver(),"frequency_wifi"));
        wifiInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_wifi", (String) newValue);
                
                framework.startWiFi();
                
                return true;
            }
        });
    }
    
    /**
     * Processor module settings UI
     */
    private void processor() {
        final CheckBoxPreference processor = (CheckBoxPreference) findPreference("status_processor");
        processor.setChecked(Aware.getSetting(getContentResolver(),"status_processor").equals("true")?true:false);
        processor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_processor",processor.isChecked()?"true":"false");
                
                if(processor.isChecked()) {
                    framework.startProcessor();
                }else {
                    framework.stopProcessor();
                }
                
                return true;
            }
        });
        
        final EditTextPreference frequencyProcessor = (EditTextPreference) findPreference("frequency_processor");
        frequencyProcessor.setText(Aware.getSetting(getContentResolver(),"frequency_processor"));
        frequencyProcessor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_processor", (String) newValue);
                
                framework.startProcessor();
                
                return true;
            }
        });
    }
    
    /**
     * TimeZone module settings UI
     */
    private void timeZone() {
        final CheckBoxPreference timeZone = (CheckBoxPreference) findPreference(STATUS_TIMEZONE);
        timeZone.setChecked(Aware.getSetting(getContentResolver(),STATUS_TIMEZONE).equals("true")?true:false);
        timeZone.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),STATUS_TIMEZONE,timeZone.isChecked()?"true":"false");
                
                if(timeZone.isChecked()) {
                    framework.startTimeZone();
                }else {
                    framework.stopTimeZone();
                }
                
                return true;
            }
        });
        
        final EditTextPreference frequencyTimeZone = (EditTextPreference) findPreference(FREQUENCY_TIMEZONE);
        frequencyTimeZone.setText(Aware.getSetting(getContentResolver(),FREQUENCY_TIMEZONE));
        frequencyTimeZone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),FREQUENCY_TIMEZONE, (String) newValue);
                
                framework.startTimeZone();
                
                return true;
            }
        });
    }
    
    /**
     * Light module settings UI
     */
    private void light() {
        
    	final PreferenceScreen light_pref = (PreferenceScreen) findPreference("light");
    	Cursor sensorProfile = getContentResolver().query(Light_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Light_Sensor.POWER_MA));
    		light_pref.setSummary(light_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		light_pref.setSummary(light_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference light = (CheckBoxPreference) findPreference("status_light");
        light.setChecked(Aware.getSetting(getContentResolver(),"status_light").equals("true")?true:false);
        light.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    light.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_light","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_light",light.isChecked()?"true":"false");
                
                if(light.isChecked()) {
                    framework.startLight();
                }else {
                    framework.stopLight();
                }
                
                return true;
            }
        });
        
        final EditTextPreference frequency_light = (EditTextPreference) findPreference("frequency_light");
        frequency_light.setText(Aware.getSetting(getContentResolver(),"frequency_light"));
        frequency_light.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_light", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Light.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
        
    }
    
    /**
     * Magnetometer module settings UI
     */
    private void magnetometer() {
        
    	final PreferenceScreen magno_pref = (PreferenceScreen) findPreference("magnetometer");
    	Cursor sensorProfile = getContentResolver().query(Magnetometer_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Magnetometer_Sensor.POWER_MA));
    		magno_pref.setSummary(magno_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		magno_pref.setSummary(magno_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference magnetometer = (CheckBoxPreference) findPreference("status_magnetometer");
        magnetometer.setChecked(Aware.getSetting(getContentResolver(),"status_magnetometer").equals("true")?true:false);
        magnetometer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    magnetometer.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_magnetometer","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_magnetometer",magnetometer.isChecked()?"true":"false");
                
                if(magnetometer.isChecked()) {
                    framework.startMagnetometer();
                }else {
                    framework.stopMagnetometer();
                }
                
                return true;
            }
        });
        
        final EditTextPreference frequency_magnetometer = (EditTextPreference) findPreference("frequency_magnetometer");
        frequency_magnetometer.setText(Aware.getSetting(getContentResolver(),"frequency_magnetometer"));
        frequency_magnetometer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_magnetometer", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Magnetometer.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
        
    }
    
    /**
     * Atmospheric Pressure module settings UI
     */
    private void barometer() {
        
    	final PreferenceScreen baro_pref = (PreferenceScreen) findPreference("barometer");
    	Cursor sensorProfile = getContentResolver().query(Barometer_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Barometer_Sensor.POWER_MA));
    		baro_pref.setSummary(baro_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		baro_pref.setSummary(baro_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference pressure = (CheckBoxPreference) findPreference("status_barometer");
        pressure.setChecked(Aware.getSetting(getContentResolver(),"status_barometer").equals("true")?true:false);
        pressure.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    pressure.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_barometer","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_barometer",pressure.isChecked()?"true":"false");
                
                if(pressure.isChecked()) {
                    framework.startBarometer();
                }else {
                    framework.stopBarometer();
                }
                
                return true;
            }
        });
        
        final EditTextPreference frequency_pressure = (EditTextPreference) findPreference("frequency_barometer");
        frequency_pressure.setText(Aware.getSetting(getContentResolver(),"frequency_barometer"));
        frequency_pressure.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_barometer", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Barometer.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
        
    }
    
    /**
     * Proximity module settings UI
     */
    private void proximity() {
        
    	final PreferenceScreen proxi_pref = (PreferenceScreen) findPreference("proximity");
    	Cursor sensorProfile = getContentResolver().query(Proximity_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Proximity_Sensor.POWER_MA));
    		proxi_pref.setSummary(proxi_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		proxi_pref.setSummary(proxi_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference proximity = (CheckBoxPreference) findPreference("status_proximity");
        proximity.setChecked(Aware.getSetting(getContentResolver(),"status_proximity").equals("true")?true:false);
        proximity.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    proximity.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_proximity","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_proximity",proximity.isChecked()?"true":"false");
                
                if(proximity.isChecked()) {
                    framework.startProximity();
                }else {
                    framework.stopProximity();
                }
                
                return true;
            }
        });
        
        final EditTextPreference frequency_proximity = (EditTextPreference) findPreference("frequency_proximity");
        frequency_proximity.setText(Aware.getSetting(getContentResolver(),"frequency_proximity"));
        frequency_proximity.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_proximity", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Proximity.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
       
    }
    
    /**
     * Rotation module settings UI
     */
    private void rotation() {
        
    	final PreferenceScreen rotation_pref = (PreferenceScreen) findPreference("rotation");
    	Cursor sensorProfile = getContentResolver().query(Rotation_Sensor.CONTENT_URI, null, null, null, null);
    	if( sensorProfile != null && sensorProfile.moveToFirst() ) {
    		float power_ma = sensorProfile.getFloat(sensorProfile.getColumnIndex(Rotation_Sensor.POWER_MA));
    		rotation_pref.setSummary(rotation_pref.getSummary().toString().replace("*", " - Power: " + power_ma +" mA"));
    	} else {
    		rotation_pref.setSummary(rotation_pref.getSummary().toString().replace("*", ""));
    	}
    	if( sensorProfile != null && ! sensorProfile.isClosed() ) sensorProfile.close();
    	
    	final CheckBoxPreference rotation = (CheckBoxPreference) findPreference("status_rotation");
        rotation.setChecked(Aware.getSetting(getContentResolver(),"status_rotation").equals("true")?true:false);
        rotation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if( mSensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null ) {
                    showDialog(DIALOG_ERROR_MISSING_SENSOR);
                    rotation.setChecked(false);
                    Aware.setSetting(getContentResolver(),"status_rotation","false");
                    return false;
                }
                
                Aware.setSetting(getContentResolver(),"status_rotation",rotation.isChecked()?"true":"false");
                
                if(rotation.isChecked()) {
                    framework.startRotation();
                }else {
                    framework.stopRotation();
                }
                
                return true;
            }
        });
        
        final EditTextPreference frequency_rotation = (EditTextPreference) findPreference("frequency_rotation");
        frequency_rotation.setText(Aware.getSetting(getContentResolver(),"frequency_rotation"));
        frequency_rotation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"frequency_rotation", (String) newValue);
                
                Intent sensorRefresh = new Intent(mContext, Rotation.class);
                sensorRefresh.putExtra("refresh", true);
                startService(sensorRefresh);
                
                return true;
            }
        });
        
    }
    
    /**
     * Telephony module settings UI
     */
    private void telephony() {
        final CheckBoxPreference telephony = (CheckBoxPreference) findPreference("status_telephony");
        telephony.setChecked(Aware.getSetting(getContentResolver(),"status_telephony").equals("true")?true:false);
        telephony.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Aware.setSetting(getContentResolver(),"status_telephony",telephony.isChecked()?"true":"false");
                
                if(telephony.isChecked()) {
                    framework.startTelephony();
                }else {
                    framework.stopTelephony();
                }
                
                return true;
            }
        });
    }
    
    /**
     * Logging module settings UI components
     */
    private void logging() {
        webservices();
        mqtt();
    }
    
    /**
     * Webservices module settings UI
     */
    private void webservices() {
    	PreferenceScreen webScreen = (PreferenceScreen) findPreference("webservice");
        webScreen.setSummary( Aware.getSetting(getContentResolver(), WEBSERVICE_SERVER) );
        
    	final CheckBoxPreference webservice = (CheckBoxPreference) findPreference("status_webservice");
        webservice.setChecked(Aware.getSetting(getContentResolver(),"status_webservice").equals("true")?true:false);
        webservice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                if( Aware.getSetting(getContentResolver(), "webservice_server").length() == 0 ) {
                    showDialog(DIALOG_ERROR_MISSING_PARAMETERS);
                    webservice.setChecked(false);
                    return false;
                } else {
                    Aware.setSetting(getContentResolver(),"status_webservice",webservice.isChecked()?"true":"false");
                    if( webservice.isChecked() && Aware.getSetting(getContentResolver(), WEBSERVICE_SERVER).length() > 0 ) {
                    	//Webservice server URL is complete, send broadcast
                        Intent webservice = new Intent(Aware.ACTION_AWARE_WEBSERVICE);
                        sendBroadcast(webservice);
                    }
                    return true;
                }
            }
        });
        
        final EditTextPreference webservice_server = (EditTextPreference) findPreference("webservice_server");
        webservice_server.setText(Aware.getSetting(getContentResolver(),"webservice_server"));
        webservice_server.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"webservice_server", (String) newValue);
                return true;
            }
        });
    }
    
    /**
     * MQTT module settings UI
     */
    private void mqtt() {
        PreferenceScreen mqttScreen = (PreferenceScreen) findPreference("mqtt");
        if( Aware.getSetting(getContentResolver(),"status_mqtt").equals("true") ) {
        	mqttScreen.setSummary("MQTT Device ID: " + Aware.getSetting(getContentResolver(), "device_id").hashCode() );
        }
    	
    	final CheckBoxPreference mqtt = (CheckBoxPreference) findPreference("status_mqtt");
        mqtt.setChecked(Aware.getSetting(getContentResolver(),"status_mqtt").equals("true")?true:false);
        mqtt.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if( Aware.getSetting(getContentResolver(), "mqtt_server").length() == 0 ) {
                    showDialog(DIALOG_ERROR_MISSING_PARAMETERS);
                    mqtt.setChecked(false);
                    return false;
                } else {
                    Aware.setSetting(getContentResolver(),"status_mqtt",mqtt.isChecked()?"true":"false");
                    if(mqtt.isChecked()) {
                        framework.startMQTT();
                    }else {
                        framework.stopMQTT();
                    }
                    return true;
                }
            }
        });
        
        final EditTextPreference mqttServer = (EditTextPreference) findPreference("mqtt_server");
        mqttServer.setText(Aware.getSetting(getContentResolver(),"mqtt_server"));
        mqttServer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"mqtt_server", (String) newValue);
                return true;
            }
        });
        
        final EditTextPreference mqttPort = (EditTextPreference) findPreference("mqtt_port");
        mqttPort.setText(Aware.getSetting(getContentResolver(),"mqtt_port"));
        mqttPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"mqtt_port", (String) newValue);
                return true;
            }
        });
        
        final EditTextPreference mqttUsername = (EditTextPreference) findPreference("mqtt_username");
        mqttUsername.setText(Aware.getSetting(getContentResolver(),"mqtt_username"));
        mqttUsername.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"mqtt_username", (String) newValue);
                return true;
            }
        });
        
        final EditTextPreference mqttPassword = (EditTextPreference) findPreference("mqtt_password");
        mqttPassword.setText(Aware.getSetting(getContentResolver(),"mqtt_password"));
        mqttPassword.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"mqtt_password", Encrypter.hashMD5((String) newValue));
                return true;
            }
        });
        
        final EditTextPreference mqttKeepAlive = (EditTextPreference) findPreference("mqtt_keep_alive");
        mqttKeepAlive.setText(Aware.getSetting(getContentResolver(),"mqtt_keep_alive"));
        mqttKeepAlive.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"mqtt_keep_alive", (String) newValue);
                return true;
            }
        });
        
        final EditTextPreference mqttQoS = (EditTextPreference) findPreference("mqtt_qos");
        mqttQoS.setText(Aware.getSetting(getContentResolver(),"mqtt_qos"));
        mqttQoS.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Aware.setSetting(getContentResolver(),"mqtt_qos", (String) newValue);
                return true;
            }
        });
    }
}
