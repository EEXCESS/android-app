/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.aware.providers.Aware_Provider.Aware_Settings;
import com.aware.providers.Mqtt_Provider;
import com.aware.providers.Mqtt_Provider.Mqtt_Messages;
import com.aware.providers.Mqtt_Provider.Mqtt_Subscriptions;
import com.aware.utils.Aware_Sensor;

/**
 * Service that connects to the MQTT P2P network for AWARE
 * @author denzil
 *
 */
public class Mqtt extends Aware_Sensor {
	
    /**
	 * Logging tag (default = "AWARE::MQTT")
	 */
	public static String TAG = "AWARE::MQTT";
	
	/**
	 * MQTT persistence messages
	 */
	private static MqttDefaultFilePersistence MQTT_MESSAGES_PERSISTENCE = null;
	
	/**
	 * The MQTT server
	 */
	private static String MQTT_SERVER = "";
	
	/**
	 * The MQTT server connection port
	 */
	private static String MQTT_PORT = "";
	
	/**
	 * The user that is allowed to connect to the MQTT server
	 */
	private static String MQTT_USERNAME = "";
	
	/**
	 * The password of the user that is allowed to connect to the MQTT server
	 */
	private static String MQTT_PASSWORD = "";
	
	/**
	 * How frequently the device will ping the server, in seconds (default = 600)
	 */
	private static String MQTT_KEEPALIVE = "600";
	
	/**
	 * MQTT message QoS (default = 2)
	 * 0 - no guarantees
	 * 1 - At least once
	 * 2 - Exacly once
	 */
	private static String MQTT_QoS = "2";
	
	/**
	 * MQTT message published ID
	 */
	public static final int MQTT_MSG_PUBLISHED = 1;
	
	/**
	 * MQTT message received ID
	 */
	public static final int MQTT_MSG_RECEIVED = 2;
	
	/**
	 * Broadcast event when a new MQTT message is received from any of the topics subscribed
	 */
	public static final String ACTION_AWARE_MQTT_MSG_RECEIVED = "ACTION_AWARE_MQTT_MSG_RECEIVED";
	
	/**
	 * Receive broadcast event: request to publish message to a topic <br/>
	 * Extras: <br/>
	 * {@link Mqtt#EXTRA_TOPIC} <br/>
	 * {@link Mqtt#EXTRA_MESSAGE}
	 */
	public static final String ACTION_AWARE_MQTT_MSG_PUBLISH = "ACTION_AWARE_MQTT_MSG_PUBLISH";
	
	/**
	 * Receive broadcast event: subscribe to a topic.
	 * Extras: <br/>
	 * {@link Mqtt#EXTRA_TOPIC}
	 */
	public static final String ACTION_AWARE_MQTT_TOPIC_SUBSCRIBE = "ACTION_AWARE_MQTT_TOPIC_SUBSCRIBE";
	
	/**
	 * Receive broadcast event: unsubscribe from a topic.
	 * Extras: <br/>
	 * {@link Mqtt#EXTRA_TOPIC}
	 */
	public static final String ACTION_AWARE_MQTT_TOPIC_UNSUBSCRIBE = "ACTION_AWARE_MQTT_TOPIC_UNSUBSCRIBE";
	
	/**
	 * Extra for Mqtt broadcast as "topic"
	 */
	public static final String EXTRA_TOPIC = "topic";
	
	/**
	 * Extra for Mqtt broadcast as "message"
	 */
	public static final String EXTRA_MESSAGE = "message";
	
	private static MqttClient MQTT_CLIENT;
	private static MqttConnectOptions MQTT_OPTIONS;
	
	private final MqttCallback MQTT_CALLBACK = new MqttCallback() {
        @Override
        public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
            ContentValues rowData = new ContentValues();
            rowData.put(Mqtt_Messages.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Mqtt_Messages.DEVICE_ID, Aware.getSetting(getContentResolver(),Aware_Preferences.DEVICE_ID));
            rowData.put(Mqtt_Messages.TOPIC, topic.getName());
            rowData.put(Mqtt_Messages.MESSAGE, message.toString());
            rowData.put(Mqtt_Messages.STATUS, MQTT_MSG_RECEIVED);
            
            try {
                getContentResolver().insert(Mqtt_Messages.CONTENT_URI, rowData);
            }catch( SQLiteException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
            }
        
            Intent mqttMsg = new Intent(ACTION_AWARE_MQTT_MSG_RECEIVED);
            mqttMsg.putExtra(EXTRA_TOPIC, topic.getName());
            mqttMsg.putExtra(EXTRA_MESSAGE, message.toString());
            sendBroadcast(mqttMsg);
            
            if( topic.getName().equals(Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID).hashCode()+"/broadcasts") ) {
                Intent broadcast = new Intent(message.toString());
                sendBroadcast(broadcast);
            }
            
            if( topic.getName().equals(Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID).hashCode()+"/esm") ) {
                Intent queueESM = new Intent(ESM.ACTION_AWARE_QUEUE_ESM);
                queueESM.putExtra(ESM.EXTRA_ESM, message.toString());
                sendBroadcast(queueESM);
            }
            
            if( topic.getName().equals(Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID).hashCode()+"/configuration") ) {
                String[] settingsStr = message.toString().split(";");
                for(String setStr : settingsStr) {
                    
                    String setting = setStr.substring(0,setStr.indexOf("="));
                    String value = setStr.toString().substring(setStr.indexOf("=")+1, setStr.length());
                    
                    Intent settings = new Intent(Aware.ACTION_AWARE_CONFIGURATION);
                    settings.putExtra(Aware_Settings.SETTING_KEY, setting);
                    settings.putExtra(Aware_Settings.SETTING_VALUE, value);
                    sendBroadcast(settings);
                }
            }
            
            if( Aware.DEBUG ) Log.d(TAG,"MQTT: Topic = "+topic.getName()+ " Message = "+message.toString());
        }
        
        @Override
        public void deliveryComplete(MqttDeliveryToken arg0) {
            //TODO: feedback on message delivered to server
        }
        
        @Override
        public void connectionLost(Throwable arg0) {
            new MQTTAsync().execute( MQTT_OPTIONS );
        }
    };
	
	private static Context mContext = null;
	
	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();
	public class ServiceBinder extends Binder {
		Mqtt getService() {
			return Mqtt.getService();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	
    private static Mqtt mqttSrv = Mqtt.getService();
    /**
     * Singleton instance to service
     * @return Mqtt mqttSrv
     */
    public static Mqtt getService() {
    	if ( mqttSrv == null ) mqttSrv = new Mqtt();
        return mqttSrv;
    }    

    /**
     * MQTT broadcast receiver. Allows other services and applications to publish and subscribe to content on MQTT broker:
     * - ACTION_AWARE_MQTT_MSG_PUBLISH - publish a new message to a specified topic - extras: (String) topic; message
     * - ACTION_AWARE_MQTT_TOPIC_SUBSCRIBE - subscribe to a topic - extras: (String) topic
     * - ACTION_AWARE_MQTT_TOPIC_UNSUBSCRIBE - unsubscribe from a topic - extras: (String) topic
     * @author df
     *
     */
    public static class MQTTReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_AWARE_MQTT_MSG_PUBLISH)) {
                String topic = intent.getStringExtra(EXTRA_TOPIC);
                String message = intent.getStringExtra(EXTRA_MESSAGE);
                if(topic != null && message!= null && topic.length() > 0 && message.length() >0 ) {
                    if ( publish(topic, message.getBytes()) ) {
                        ContentValues rowData = new ContentValues();
                        rowData.put(Mqtt_Messages.TIMESTAMP, System.currentTimeMillis());
                        rowData.put(Mqtt_Messages.DEVICE_ID, Aware.getSetting(context.getContentResolver(),Aware_Preferences.DEVICE_ID));
                        rowData.put(Mqtt_Messages.TOPIC, topic);
                        rowData.put(Mqtt_Messages.MESSAGE, message);
                        rowData.put(Mqtt_Messages.STATUS, MQTT_MSG_PUBLISHED);
                        
                        try {
                            context.getContentResolver().insert(Mqtt_Messages.CONTENT_URI, rowData);
                        }catch( SQLiteException e ) {
                            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                        }catch( SQLException e ) {
                            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                        }
                    }
                }
            }
            if(intent.getAction().equals(ACTION_AWARE_MQTT_TOPIC_SUBSCRIBE)) {
                String topic = intent.getStringExtra(EXTRA_TOPIC);
                if(topic != null && topic.length() > 0) {
                    if ( subscribe( Aware.getSetting( context.getContentResolver(), Aware_Preferences.DEVICE_ID).hashCode()+"/"+ topic ) ) {
                        
                        Cursor subscriptions = context.getContentResolver().query(Mqtt_Subscriptions.CONTENT_URI, null, Mqtt_Subscriptions.TOPIC + " LIKE '%" + topic + "%'", null, null);
                        if( subscriptions == null || ! subscriptions.moveToFirst() ) {
                            
                            ContentValues rowData = new ContentValues();
                            rowData.put(Mqtt_Subscriptions.TIMESTAMP, System.currentTimeMillis());
                            rowData.put(Mqtt_Subscriptions.DEVICE_ID, Aware.getSetting(context.getContentResolver(), Aware_Preferences.DEVICE_ID));
                            rowData.put(Mqtt_Subscriptions.TOPIC, topic);
                            
                            try {
                                context.getContentResolver().insert(Mqtt_Subscriptions.CONTENT_URI, rowData);
                                if( Aware.DEBUG ) Log.w( TAG, "MQTT new topic subscribed: " + topic );
                                
                            }catch( SQLiteException e ) {
                                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                            }catch( SQLException e ) {
                                if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                            }
                        }
                        subscriptions.close();
                    }
                }
            }
            if(intent.getAction().equals(ACTION_AWARE_MQTT_TOPIC_UNSUBSCRIBE)) {
                String topic = intent.getStringExtra(EXTRA_TOPIC);
                if( topic != null && topic.length() > 0 ) {
                    if( unsubscribe( Aware.getSetting( context.getContentResolver(), Aware_Preferences.DEVICE_ID).hashCode() + "/" + topic ) ) {
                        try {
                            context.getContentResolver().delete(Mqtt_Subscriptions.CONTENT_URI, Mqtt_Subscriptions.TOPIC+" LIKE '%"+topic+"%'", null);
                        }catch( SQLiteException e) {
                            if( Aware.DEBUG ) Log.w(TAG, e.getMessage());
                        }catch( SQLException e) {
                            if( Aware.DEBUG ) Log.w(TAG, e.getMessage());
                        }
                    }
                }
            }
        };
    }
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		TAG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG):TAG;
        mContext = getApplicationContext();
        
		DATABASE_TABLES = Mqtt_Provider.DATABASE_TABLES;
    	TABLES_FIELDS = Mqtt_Provider.TABLES_FIELDS;
    	CONTEXT_URIS = new Uri[]{ Mqtt_Messages.CONTENT_URI, Mqtt_Subscriptions.CONTENT_URI };
				
        MQTT_SERVER = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_SERVER );
        MQTT_PORT = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_PORT );
        MQTT_USERNAME = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_USERNAME );
        MQTT_PASSWORD = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_PASSWORD );
        MQTT_KEEPALIVE = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_KEEP_ALIVE );
        MQTT_QoS = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_QOS);
        
        try {
            MQTT_MESSAGES_PERSISTENCE = new MqttDefaultFilePersistence(Environment.getExternalStorageDirectory()+"/AWARE/");
        } catch (MqttPersistenceException e) {
            if(Aware.DEBUG) Log.w(TAG,"MQTT will run without persistence on this client, failed to use the external storage for persistence...");
            MQTT_MESSAGES_PERSISTENCE = null;
        }
        
        MQTT_OPTIONS = new MqttConnectOptions();
        MQTT_OPTIONS.setCleanSession(false);
        MQTT_OPTIONS.setKeepAliveInterval(Integer.parseInt(MQTT_KEEPALIVE));
        
        if( MQTT_USERNAME.length() > 0 ) MQTT_OPTIONS.setUserName(MQTT_USERNAME);
        if( MQTT_PASSWORD.length() > 0 ) MQTT_OPTIONS.setPassword(MQTT_PASSWORD.toCharArray());
        
        String MQTT_URL = "tcp://"+MQTT_SERVER+":"+MQTT_PORT;
        
        try {
            MQTT_CLIENT = new MqttClient(MQTT_URL, String.valueOf(Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID).hashCode()), MQTT_MESSAGES_PERSISTENCE);
            MQTT_CLIENT.setCallback(MQTT_CALLBACK);
            
            new MQTTAsync().execute(MQTT_OPTIONS);
            
            if(Aware.DEBUG) Log.d(TAG, "MQTT service created!");
            
        } catch (MqttException e) {
            if( Aware.DEBUG ) Log.e(TAG,"Failed to create the MQTT client: "+e.getMessage());
            stopSelf();
        }
	}

	/**
	 * UI Thread safe background MQTT connection attempt!
	 * @author denzil
	 */
	private class MQTTAsync extends AsyncTask<MqttConnectOptions, Void, Void> {
        @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	    }
	    
	    @Override
        protected Void doInBackground(MqttConnectOptions... params) {
            try {
                if( ! MQTT_CLIENT.isConnected() ) MQTT_CLIENT.connect(params[0]);
            } catch (MqttSecurityException e) {
                if( Aware.DEBUG ) Log.e(TAG,"Failed to create the MQTT client: "+e.getMessage());
            } catch (MqttException e) {
                if( Aware.DEBUG ) Log.e(TAG,"Failed to create the MQTT client: "+e.getMessage());
            }
            return null;
        }
	    @Override
	    protected void onPostExecute(Void result) {
	        super.onPostExecute(result);
	        
	        Intent selfSubscribe = new Intent(ACTION_AWARE_MQTT_TOPIC_SUBSCRIBE);
	        selfSubscribe.putExtra(EXTRA_TOPIC, "broadcasts");
	        mContext.sendBroadcast(selfSubscribe);
	        
	        selfSubscribe = new Intent(ACTION_AWARE_MQTT_TOPIC_SUBSCRIBE);
	        selfSubscribe.putExtra(EXTRA_TOPIC, "esm");
	        mContext.sendBroadcast(selfSubscribe);
	        
	        selfSubscribe = new Intent(ACTION_AWARE_MQTT_TOPIC_SUBSCRIBE);
	        selfSubscribe.putExtra(EXTRA_TOPIC, "configuration");
	        mContext.sendBroadcast(selfSubscribe);
	    }
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if( MQTT_CLIENT != null && MQTT_CLIENT.isConnected() ) {
            try {
                MQTT_CLIENT.disconnect();
            } catch (MqttException e) {
                if( Aware.DEBUG ) Log.e(TAG,"Failed to disconnect from the server...");
            } 
	    }
		
		if(Aware.DEBUG) Log.d(TAG,"MQTT service terminated...");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    
	    TAG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_TAG):TAG;
	    
	    MQTT_SERVER = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_SERVER );
        MQTT_PORT = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_PORT );
        MQTT_USERNAME = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_USERNAME );
        MQTT_PASSWORD = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_PASSWORD );
        MQTT_KEEPALIVE = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_KEEP_ALIVE );
        MQTT_QoS = Aware.getSetting(getContentResolver(), Aware_Preferences.MQTT_QOS);
        
        if(Aware.DEBUG) Log.d(TAG, "MQTT service active...");
        
        try {
            MQTT_MESSAGES_PERSISTENCE = new MqttDefaultFilePersistence(Environment.getExternalStorageDirectory()+"/AWARE/");
        } catch (MqttPersistenceException e) {
            if(Aware.DEBUG) Log.w(TAG,"MQTT will run without persistence on this client, failed to use the external storage for persistence...");
            MQTT_MESSAGES_PERSISTENCE = null;
        }
        
        MQTT_OPTIONS = new MqttConnectOptions();
        MQTT_OPTIONS.setCleanSession(false);
        MQTT_OPTIONS.setKeepAliveInterval(Integer.parseInt(MQTT_KEEPALIVE));
        
        if( MQTT_USERNAME.length() > 0 ) MQTT_OPTIONS.setUserName(MQTT_USERNAME);
        if( MQTT_PASSWORD.length() > 0 ) MQTT_OPTIONS.setPassword(MQTT_PASSWORD.toCharArray());
        
    	try {
    		//MQTT known bug, server disconnects client every 10 minutes without calling the connectionLost callback... reconnect anyway.
			if( MQTT_CLIENT.isConnected() ) MQTT_CLIENT.disconnect();
			new MQTTAsync().execute( MQTT_OPTIONS );
		} catch (MqttException e) {
			if( Aware.DEBUG) Log.e(TAG, e.getMessage());
		}
        
        return START_STICKY;
	}

	/**
	 * Publish message to topic
	 * @param topicName
	 * @param payload
	 */
	private static boolean publish(String topicName, byte[] payload) {
	    if( MQTT_CLIENT != null && MQTT_CLIENT.isConnected() ) {
            MqttTopic topic = MQTT_CLIENT.getTopic(topicName);
            MqttMessage message = new MqttMessage(payload);
            message.setQos(Integer.parseInt(MQTT_QoS));
            try {
                topic.publish(message);
                return true;
            } catch (MqttPersistenceException e) {
                if(Aware.DEBUG) Log.e(TAG, e.getMessage());
            } catch (MqttException e) {
                if(Aware.DEBUG) Log.e(TAG, e.getMessage());
            }
	    }
	    return false;
	}
	
	/**
	 * Subscribe to a topic
	 * @param topicName
	 */
	private static boolean subscribe(String topicName) {
	    if( MQTT_CLIENT != null && MQTT_CLIENT.isConnected() ) {
            try {
                MQTT_CLIENT.subscribe( topicName );
                return true;
            } catch (MqttSecurityException e) {
                if(Aware.DEBUG) Log.e(TAG, e.getMessage());
            } catch (MqttException e) {
                if(Aware.DEBUG) Log.e(TAG, e.getMessage());
            }
	    }
	    return false;
	}
	
	/**
	 * Unsubscribe a topic
	 * @param topicName
	 */
	private static boolean unsubscribe(String topicName) {
	    if( MQTT_CLIENT != null && MQTT_CLIENT.isConnected() ) {
            try {
                MQTT_CLIENT.unsubscribe(topicName);
                return true;
            } catch (MqttException e) {
                if( Aware.DEBUG ) Log.e(TAG, e.getMessage() );
            }
	    }
	    return false;
	}
}
