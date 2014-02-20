package com.aware.plugin.ui_content;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.aware.utils.Aware_Plugin;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

public class Plugin extends Aware_Plugin{
    private static final String TAG = ContextophelesConstants.TAG_UI_CONTENT + " Plugin";
	public static final String ACTION_AWARE_UICONTENT = "ACTION_AWARE_UICONTENT";
	
	@Override
    public void onCreate() {
        super.onCreate();

        //Share the context back to the framework and other applications
        CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
            @Override
            public void onContext() {
                Intent notification = new Intent(ACTION_AWARE_UICONTENT);
                sendBroadcast(notification);
            }
        };
        DATABASE_TABLES = UIContent_Provider.DATABASE_TABLES;
        TABLES_FIELDS = UIContent_Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ UIContent_Provider.UIContents.CONTENT_URI };
        
        Log.d(TAG, "Plugin Started");
                
    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();        
    }
}
