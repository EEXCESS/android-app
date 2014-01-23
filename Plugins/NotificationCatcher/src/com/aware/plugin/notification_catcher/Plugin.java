package com.aware.plugin.notification_catcher;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.aware.plugin.notification_catcher.NotificationCatcher_Provider.Notifications;
import com.aware.utils.Aware_Plugin;

/**
 * Main Plugin for the NotificationCatcher
 * @author Christian Koehler
 * @email: ckoehler@andrew.cmu.edu
 *
 */

public class Plugin extends Aware_Plugin{

	public static final String ACTION_AWARE_NOTIFICATIONCATCHER = "ACTION_AWARE_NOTIFICATIONCATCHER";
	
	@Override
    public void onCreate() {
        super.onCreate();
        
        //Set plugin values on the framework
        TAG = "NotificationCatcher";
        
        //Share the context back to the framework and other applications
        CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
            @Override
            public void onContext() {
                Intent notification = new Intent(ACTION_AWARE_NOTIFICATIONCATCHER);
                sendBroadcast(notification);
            }
        };
        DATABASE_TABLES = NotificationCatcher_Provider.DATABASE_TABLES;
        TABLES_FIELDS = NotificationCatcher_Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Notifications.CONTENT_URI };
        
        Log.d("NotificationCatcher Plugin","Plugin Started");
                
    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();        
    }
}
