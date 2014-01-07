package com.aware.plugin.sms_receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.sms_receiver.SMSReceiver_Provider.SMSReceiver;
import com.aware.utils.Aware_Plugin;

/**
 * Main Plugin for the ClipboardCatcher
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
 *
 */

public class Plugin extends Aware_Plugin{

	private static final String TAG = "SMSReceiver Plugin";
	public static final String ACTION_AWARE_SMSRECEIVER= "ACTION_AWARE_SMSRECEIVER";
	public static SmsReceiver smsreceiver;
	
	@Override
    public void onCreate() {
		Log.d(TAG,"Plugin Created");
        super.onCreate();
        
        // load initially
     		
        //loadCurrentClipboardContent();
        
        //Share the context back to the framework and other applications
        CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
            @Override
            public void onContext() {
            	Log.d(TAG,"Putting extra context into intent");
                Intent notification = new Intent(ACTION_AWARE_SMSRECEIVER);
                
                sendBroadcast(notification);
            }
        };
        DATABASE_TABLES = SMSReceiver_Provider.DATABASE_TABLES;
        TABLES_FIELDS = SMSReceiver_Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ SMSReceiver.CONTENT_URI };
                
        smsreceiver = new SmsReceiver();
        
        IntentFilter clipboard_filter = new IntentFilter();
        clipboard_filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        
        Log.d(TAG,"Creating Receiver");
        registerReceiver(smsreceiver, clipboard_filter);
        
        Log.d(TAG,"Plugin Started");
        
    }
	
	@Override
    public void onDestroy() {
       
		Log.d(TAG,"Plugin is destroyed");
		
		super.onDestroy();    
        
        unregisterReceiver(smsreceiver);
    }
	
	@SuppressLint("NewApi")
	public void clipboardUpdated(){
		// load
		//loadCurrentClipboardContent();
		 
		// save
//		saveData(currentClipboardContent);
     	
		// distribute
		CONTEXT_PRODUCER.onContext();
	}

	
	protected void saveData(String smsContent) {
		
		Log.d(TAG,"Saving Data");
		
		ContentValues rowData = new ContentValues();
        rowData.put(SMSReceiver.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
        rowData.put(SMSReceiver.TIMESTAMP, System.currentTimeMillis());    
        rowData.put(SMSReceiver.SMSContent, smsContent);
        
        getContentResolver().insert(SMSReceiver.CONTENT_URI, rowData);
    }
	
	
	public class SmsReceiver extends BroadcastReceiver {

		// implementation from http://stackoverflow.com/questions/11435354/receiving-sms-on-android-app
	    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	Log.d(TAG, intent.getAction());
	    	if (intent.getAction().equals(SMS_RECEIVED)) {
	            Bundle bundle = intent.getExtras();
	            if (bundle != null) {
	                // get sms objects
	                Object[] pdus = (Object[]) bundle.get("pdus");
	                if (pdus.length == 0) {
	                    return;
	                }
	                // large message might be broken into many
	                SmsMessage[] messages = new SmsMessage[pdus.length];
	                StringBuilder sb = new StringBuilder();
	                for (int i = 0; i < pdus.length; i++) {
	                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
	                    sb.append(messages[i].getMessageBody());
	                }
	                String sender = messages[0].getOriginatingAddress();
	                String message = sb.toString();
//	                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	                Log.d(TAG, message);
	                
	                saveData(message);
	                // prevent any other broadcast receivers from receiving broadcast
	                // abortBroadcast();
	            }
	        }
	    }
	}
}
