package com.aware.plugin.term_collector;

import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.term_collector.TermCollector_Provider.TermCollector;
import com.aware.utils.Aware_Plugin;

/**
 * Main Plugin for the TERM Collector
 * 
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
 * 
 */

public class Plugin extends Aware_Plugin {

	private static final String TAG = "TermCollector Plugin";
	public static final String ACTION_AWARE_TERMCOLLECTOR = "ACTION_AWARE_TERMCOLLECTOR";

    private static StopList stopList;
	private ClipboardManager.OnPrimaryClipChangedListener clipboardListener;

	public static final String EXTRA_TERMCONTENT = "termcontent";
	public static String lastTermContent = "";
	
	public static Uri clipboardCatcherContentUri;
	private static ClipboardCatcherObserver clipboardCatcherObs = null;
	
	public static Uri notificationCatcherContentUri;
	private static NotificationCatcherObserver notificationCatcherObs = null;
	
	public static Uri smsReceiverContentUri;
	private static SmsReceiverObserver smsReceiverObs = null;

    public static Uri locationDissolverContentUri;
    private static LocationDissolverObserver locationDissolverObs = null;

	/**
	 * Thread manager
	 */
	private static HandlerThread threads = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "Plugin Created");
		super.onCreate();

        stopList = new StopList();

		// Share the context back to the framework and other applications
		CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
			@Override
			public void onContext() {
				Log.d(TAG, "Putting extra context into intent");
				Intent notification = new Intent(ACTION_AWARE_TERMCOLLECTOR);
				notification
						.putExtra(Plugin.EXTRA_TERMCONTENT, lastTermContent);
				sendBroadcast(notification);
			}
		};

		DATABASE_TABLES = TermCollector_Provider.DATABASE_TABLES;
		TABLES_FIELDS = TermCollector_Provider.TABLES_FIELDS;
		CONTEXT_URIS = new Uri[] { TermCollector.CONTENT_URI };

		threads = new HandlerThread(TAG);
		threads.start();

		// Set the observers, that run in independent threads, for
		// responsiveness
		
		clipboardCatcherContentUri = Uri
				.parse("content://com.aware.provider.plugin.clipboard_catcher/plugin_clipboard_catcher");
		clipboardCatcherObs = new ClipboardCatcherObserver(new Handler(
				threads.getLooper()));
		getContentResolver().registerContentObserver(
				clipboardCatcherContentUri, true, clipboardCatcherObs);
		Log.d(TAG, "clipboardCatcherObs registered");
		
		notificationCatcherContentUri= Uri
				.parse("content://com.aware.provider.plugin.notification_catcher/plugin_notification_catcher");
		notificationCatcherObs = new NotificationCatcherObserver(new Handler(
				threads.getLooper()));
		getContentResolver().registerContentObserver(
				notificationCatcherContentUri, true, notificationCatcherObs);
		Log.d(TAG, "notificationCatcherObs registered");
		
		
		smsReceiverContentUri= Uri
				.parse("content://com.aware.provider.plugin.sms_receiver/plugin_sms_receiver");
		smsReceiverObs = new SmsReceiverObserver(new Handler(
				threads.getLooper()));
		getContentResolver().registerContentObserver(
				smsReceiverContentUri, true, smsReceiverObs);
		Log.d(TAG, "smsReceiverObs registered");


        locationDissolverContentUri= Uri
                .parse("content://com.aware.provider.plugin.location_dissolver/plugin_location_dissolver");
        locationDissolverObs = new LocationDissolverObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                locationDissolverContentUri, true, locationDissolverObs);
        Log.d(TAG, "locationDissolverObs registered");
		
		
		Log.d(TAG, "Plugin Started");
	}

	@Override
	public void onDestroy() {

		Log.d(TAG, "Plugin is destroyed");

		super.onDestroy();

		android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clipboardManager.removePrimaryClipChangedListener(clipboardListener);

		getContentResolver().unregisterContentObserver(clipboardCatcherObs);
		getContentResolver().unregisterContentObserver(notificationCatcherObs);
		getContentResolver().unregisterContentObserver(smsReceiverObs);

	}

	protected void saveData(long timestamp, String source, String content) {
		Log.d(TAG, "Saving Data");

		ContentValues rowData = new ContentValues();
		rowData.put(TermCollector.DEVICE_ID, Aware.getSetting(
				getContentResolver(), Aware_Preferences.DEVICE_ID));
		rowData.put(TermCollector.TIMESTAMP, timestamp);
		rowData.put(TermCollector.TERM_SOURCE, source);
		rowData.put(TermCollector.TERM_CONTENT, content);

		Log.d(TAG, "Saving " + rowData.toString());
		getContentResolver().insert(TermCollector.CONTENT_URI, rowData);
	}

    protected void splitAndFilterAndSaveData(long timestamp, String source, String content) {
        Log.d(TAG, "Splitting Content");
        //remove all characters that are not A-Za-z
        String[] contentTokens = content.replaceAll("[^A-Za-zÄÖÜäöü]", " ").split("\\s+");

        int tokenIndex = 0;

        //filter Stopwords
        contentTokens = stopList.filteredArray(contentTokens);

        for(String token: contentTokens) {
            if(Character.isUpperCase(token.charAt(0))){
               if(token.length() > 2) {
                   Log.wtf(TAG, "Accepting " + token);
                   // add TokenIndex to avoid duplicate timestamps
                   saveData(timestamp + tokenIndex, source, token);
                   tokenIndex++;
               } else {
                   Log.wtf(TAG, "Ignoring " + token + " as it is shorter than 3 characters.");
               }
            } else {
                Log.wtf(TAG, "Ignoring " + token + " as it is not uppercase");
            }
        }
    }

	public class ClipboardCatcherObserver extends ContentObserver {
		public ClipboardCatcherObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange");

			// set cursor to first item
			Cursor cursor = getContentResolver().query(
					clipboardCatcherContentUri, null, null, null,
					"timestamp" + " DESC LIMIT 1");
			if (cursor != null && cursor.moveToFirst()) {

                splitAndFilterAndSaveData(cursor.getLong(cursor.getColumnIndex("timestamp")),
						clipboardCatcherContentUri.toString(),
						cursor.getString(cursor
								.getColumnIndex("CLIPBOARDCONTENT")));
			}

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	public class NotificationCatcherObserver extends ContentObserver {
		public NotificationCatcherObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange");

			// set cursor to first item
			Cursor cursor = getContentResolver().query(
					notificationCatcherContentUri, null, null, null,
					"timestamp" + " DESC LIMIT 1");
			if (cursor != null && cursor.moveToFirst()) {

                splitAndFilterAndSaveData(cursor.getLong(cursor.getColumnIndex("timestamp")),
						notificationCatcherContentUri.toString(),
						cursor.getString(cursor
								.getColumnIndex("TEXT")));
			}

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	public class SmsReceiverObserver extends ContentObserver {
		public SmsReceiverObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			// set cursor to first item
			Cursor cursor = getContentResolver().query(
					smsReceiverContentUri, null, null, null,
					"timestamp" + " DESC LIMIT 1");
			if (cursor != null && cursor.moveToFirst()) {

                splitAndFilterAndSaveData(cursor.getLong(cursor.getColumnIndex("timestamp")),
						smsReceiverContentUri.toString(),
						cursor.getString(cursor
								.getColumnIndex("SMSCONTENT")));
			}

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}


    public class LocationDissolverObserver extends ContentObserver {
        public LocationDissolverObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    locationDissolverContentUri, null, null, null,
                    "timestamp" + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {

                saveData(cursor.getLong(cursor.getColumnIndex("timestamp")),
                        locationDissolverContentUri.toString(),
                        cursor.getString(cursor
                                .getColumnIndex("NAME")));
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
