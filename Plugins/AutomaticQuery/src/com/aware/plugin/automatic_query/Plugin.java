package com.aware.plugin.automatic_query;

import android.database.ContentObserver;
import android.database.Cursor;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


import com.aware.utils.Aware_Plugin;

/**
 * A Tool, that listens to the TermCollector, does Europeana Queries and sends out a Notification
 * 
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
 * 
 */

public class Plugin extends Aware_Plugin {

	private static final String TAG = "AutomaticQuery Plugin";

	public static Uri termCollectorContentUri;
	private static TermCollectorObserver termCollectorObs = null;


	/**
	 * Thread manager
	 */
	private static HandlerThread threads = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "Plugin Created");
		super.onCreate();


		threads = new HandlerThread(TAG);
		threads.start();

		// Set the observers, that run in independent threads, for
		// responsiveness

        termCollectorContentUri = Uri
				.parse("content://com.aware.provider.plugin.term_collector/plugin_term_collector");
        termCollectorObs = new TermCollectorObserver(new Handler(
				threads.getLooper()));
		getContentResolver().registerContentObserver(
                termCollectorContentUri, true, termCollectorObs);
		Log.d(TAG, "termCollectorObs registered");
		

		
		
		Log.d(TAG, "Plugin Started");
	}

	@Override
	public void onDestroy() {

		Log.d(TAG, "Plugin is destroyed");

		super.onDestroy();

		getContentResolver().unregisterContentObserver(termCollectorObs);

	}

	protected void runQuery(String term) {
		Log.d(TAG, "Running Query for term " + term);
	}


	public class TermCollectorObserver extends ContentObserver {
		public TermCollectorObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange");

			// set cursor to first item
			Cursor cursor = getContentResolver().query(
					termCollectorContentUri, null, null, null,
					"timestamp" + " DESC LIMIT 1");
			if (cursor != null && cursor.moveToFirst()) {

                runQuery(cursor.getString(cursor
                        .getColumnIndex("term_content")));
			}

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}
}
