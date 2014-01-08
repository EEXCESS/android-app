package com.aware.plugin.clipboard_catcher;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.clipboard_catcher.ClipboardCatcher_Provider.ClipboardCatcher;
import com.aware.utils.Aware_Plugin;

/**
 * Main Plugin for the ClipboardCatcher
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
 *
 */

public class Plugin extends Aware_Plugin{

	private static final String TAG = "ClipboardCatcher Plugin";
	public static final String ACTION_AWARE_CLIPBOARDCATCHER = "ACTION_AWARE_CLIPBOARDCATCHER";
	private ClipboardManager.OnPrimaryClipChangedListener clipboardListener;
	
	public static final String EXTRA_CLIPBOARDCONTENT = "clipboardcontent";
	public static String currentClipboardContent = "";
    public static String previousClipboardContent = "";
    private static long previousTimestamp = 0L;

    @Override
    public void onCreate() {
		Log.d(TAG,"Plugin Created");
        super.onCreate();
        
        // load initially
     		
        loadCurrentClipboardContent();
        
        //Share the context back to the framework and other applications
        CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
            @Override
            public void onContext() {
            	Log.d(TAG,"Putting extra context into intent");
                Intent notification = new Intent(ACTION_AWARE_CLIPBOARDCATCHER);
                notification.putExtra(Plugin.EXTRA_CLIPBOARDCONTENT, currentClipboardContent);
                sendBroadcast(notification);
            }
        };
        DATABASE_TABLES = ClipboardCatcher_Provider.DATABASE_TABLES;
        TABLES_FIELDS = ClipboardCatcher_Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ ClipboardCatcher.CONTENT_URI };
                
        android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        
        clipboardListener = new ClipboardManager.OnPrimaryClipChangedListener() {
			
			@Override
			public void onPrimaryClipChanged() {
				clipboardUpdated();
				
			}
		};
		
        clipboardManager.addPrimaryClipChangedListener(clipboardListener);
        
        Log.d(TAG,"Plugin Started");
        
    }
	
	@Override
    public void onDestroy() {
       
		Log.d(TAG,"Plugin is destroyed");
		
		super.onDestroy();    
        
        android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.removePrimaryClipChangedListener(clipboardListener);

    }
	
	@SuppressLint("NewApi")
	public void clipboardUpdated(){
		// load
		loadCurrentClipboardContent();
		 
		// save
		saveData(currentClipboardContent);

	}
	
	private void loadCurrentClipboardContent() {
		android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clipData = clipboardManager.getPrimaryClip();

        if (clipData != null && clipData.getItemAt(0) != null) {
		    currentClipboardContent = clipData.getItemAt(0).getText().toString();
            Log.d(TAG,"CURRENT_CLIPBOARDCONTENT is now" + clipData.getItemAt(0));
        }
	}
	
	protected void saveData(String clipboardContent) {
		
		ContentValues rowData = new ContentValues();
        rowData.put(ClipboardCatcher.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
        rowData.put(ClipboardCatcher.TIMESTAMP, System.currentTimeMillis());
        rowData.put(ClipboardCatcher.CLIPBOARDCONTENT, clipboardContent);


      if(currentClipboardContent.equals(previousClipboardContent)) {
          Log.d(TAG, "Skipping saving, as content did not change");
        } else {
          Log.d(TAG, "Saving " + rowData.toString());
          getContentResolver().insert(ClipboardCatcher.CONTENT_URI, rowData);
          previousClipboardContent = currentClipboardContent;

          // distribute
          CONTEXT_PRODUCER.onContext();
        }

    }
}
