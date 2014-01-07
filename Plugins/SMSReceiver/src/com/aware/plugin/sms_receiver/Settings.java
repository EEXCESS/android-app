package com.aware.plugin.sms_receiver;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Settings extends Activity {
    private final static String TAG = "ClipboardCatcher Settings";
    private static TextView content = null;
    private static TextView countView = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter clipboard_filter = new IntentFilter();
        clipboard_filter.addAction(Plugin.ACTION_AWARE_SMSRECEIVER);
        
        Log.d(TAG,"Creating Receiver");
        registerReceiver(clipboard_receiver, clipboard_filter);
        
        setContentView(R.layout.debug_layout);
        
        countView = (TextView) findViewById(R.id.count);
        
        content = (TextView) findViewById(R.id.clipboardcontent);        
//        content.setText(Plugin.currentClipboardContent);
        
        updateCount();
    }
    
    protected void onResume() {
        super.onResume();
    	updateCount();
    }
    
    
    private static final ClipboardReceiver clipboard_receiver = new ClipboardReceiver();
    
    public static class ClipboardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"received");

        	if( intent.getAction().equals(Plugin.ACTION_AWARE_SMSRECEIVER)) {
        		Log.d(TAG,"Action");
//        		content.setText(intent.getStringExtra(Plugin.EXTRA_CLIPBOARDCONTENT));
            }
        }
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(TAG,"Destroying Receiver");
    	super.onDestroy();
        
        unregisterReceiver(clipboard_receiver);
    }
    
    public void updateCount() {
    	Cursor countCursor = getContentResolver().query(SMSReceiver_Provider.SMSReceiver.CONTENT_URI,
                new String[] {"count(*) AS count"},
                null,
                null,
                null);

        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        
        Log.d(TAG, "count:" + count);
        TextView myCountView = (TextView) findViewById(R.id.count);
        myCountView.setText("" + count);
    }
}
