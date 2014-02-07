package com.aware.plugin.automatic_query;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.aware.plugin.automatic_query.situations.SituationManager;
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

    public int getNotificationNumber() {
        return notificationNumber;
    }

    public void setNotificationNumber(int notificationNumber) {
        this.notificationNumber = notificationNumber;
    }

    private int notificationNumber = 0;

    public static Uri geoCollectorContentUri;
	private static GeoCollectorObserver geoCollectorObs = null;

    public static Uri termCollectorContentUri;
    private static TermCollectorObserver termCollectorObs = null;

    public static Uri lightContentUri;
    private static LightObserver lightObs = null;

    private SituationManager situationManager;

	/**
	 * Thread manager
	 */
	private static HandlerThread threads = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "Plugin Created");
		super.onCreate();

        situationManager = new SituationManager(getApplicationContext());

		threads = new HandlerThread(TAG);
		threads.start();

		// Set the observers, that run in independent threads, for
		// responsiveness

        termCollectorContentUri = Uri
				.parse("content://com.aware.provider.plugin.term_collector/plugin_term_collector_terms");
        termCollectorObs = new TermCollectorObserver(new Handler(
				threads.getLooper()));
		getContentResolver().registerContentObserver(
                termCollectorContentUri, true, termCollectorObs);
		Log.d(TAG, "termCollectorObs registered");


        geoCollectorContentUri = Uri
                .parse("content://com.aware.provider.plugin.geo_collector/plugin_geo_collector_terms");
        geoCollectorObs = new GeoCollectorObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                geoCollectorContentUri, true, geoCollectorObs);
        Log.d(TAG, "geoCollectorObs registered");


        lightContentUri = Uri.parse("content://com.aware.provider.light/light");
        lightObs = new LightObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                lightContentUri, true, lightObs);
        Log.d(TAG, "lightObs registered");


		
		Log.d(TAG, "Plugin Started");
	}

	@Override
	public void onDestroy() {

		Log.d(TAG, "Plugin is destroyed");

		super.onDestroy();

		getContentResolver().unregisterContentObserver(termCollectorObs);
        getContentResolver().unregisterContentObserver(geoCollectorObs);
        getContentResolver().unregisterContentObserver(lightObs);

	}

	protected void runQuery(String term) {
		Log.d(TAG, "Running Query for term " + term);

        new ExecuteSearchTask(this).execute(new String[]{"0", term});

    }

	public class TermCollectorObserver extends ContentObserver {
		public TermCollectorObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange of Term Content");

			// set cursor to first item
			Cursor cursor = getContentResolver().query(
					termCollectorContentUri, null, null, null,
					"timestamp" + " DESC LIMIT 1");
			if (cursor != null && cursor.moveToFirst()) {

                if (situationManager.allowsQuery()){
                    runQuery(cursor.getString(cursor
                        .getColumnIndex("term_content")));
                }
			}

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

    public class GeoCollectorObserver extends ContentObserver {
        public GeoCollectorObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.d(TAG, "@onChange of Geo Content");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    geoCollectorContentUri, null, null, null,
                    "timestamp" + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {

                if (situationManager.allowsQuery()){
                    Log.d(TAG, "Query allowed");
                    runQuery(cursor.getString(cursor
                        .getColumnIndex("term_content")));
                } {
                    Log.d(TAG, "Query dissallowed");
                }
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public class LightObserver extends ContentObserver {
        public LightObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.d(TAG, "@onChange of Light");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    lightContentUri, null, null, null,
                    "timestamp" + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {

                Double lux = Double.parseDouble(cursor.getString(cursor
                        .getColumnIndex("double_light_lux")));
                Log.d(TAG, "Light changed to " + lux);
                situationManager.putContextValue("Light", lux);
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
