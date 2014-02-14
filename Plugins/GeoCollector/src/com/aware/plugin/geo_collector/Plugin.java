package com.aware.plugin.geo_collector;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.geo_collector.GeoCollector_Provider.GeoCollectorTermData;
import com.aware.utils.Aware_Plugin;

/**
 * Main Plugin for the GEO Collector
 *
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
 */

public class Plugin extends Aware_Plugin {

    private static final String TAG = "GeoCollector Plugin";
    public static final String ACTION_AWARE_GEOCOLLECTOR = "ACTION_AWARE_GEOCOLLECTOR";

    public static final String EXTRA_TERMCONTENT = "termcontent";
    public static String lastTermContent = "";

    public static Uri geonameResolverContentUri;
    private static GeonameResolverObserver geonameResolverObs = null;

    public static Uri termCollectorGeoTermContentUri;
    private static TermCollectorGeoTermObserver termCollectorGeoTermObs = null;

    /**
     * Thread manager
     */
    private static HandlerThread threads = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "Plugin Created");
        super.onCreate();


        // Share the context back to the framework and other applications
        CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
            @Override
            public void onContext() {
                Log.d(TAG, "Putting extra context into intent");
                Intent notification = new Intent(ACTION_AWARE_GEOCOLLECTOR);
                notification
                        .putExtra(Plugin.EXTRA_TERMCONTENT, lastTermContent);
                sendBroadcast(notification);
            }
        };

        DATABASE_TABLES = GeoCollector_Provider.DATABASE_TABLES;
        TABLES_FIELDS = GeoCollector_Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{GeoCollectorTermData.CONTENT_URI};

        threads = new HandlerThread(TAG);
        threads.start();

        // Set the observers, that run in independent threads, for
        // responsiveness


        geonameResolverContentUri = Uri
                .parse("content://com.aware.provider.plugin.geoname_resolver/plugin_geoname_resolver");
        geonameResolverObs = new GeonameResolverObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                geonameResolverContentUri, true, geonameResolverObs);
        Log.d(TAG, "geonameResolverObs registered");

        termCollectorGeoTermContentUri = Uri
                .parse("content://com.aware.provider.plugin.term_collector/plugin_term_collector_geodata");
        termCollectorGeoTermObs = new TermCollectorGeoTermObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                termCollectorGeoTermContentUri, true, termCollectorGeoTermObs);
        Log.d(TAG, "termCollectorGeoTermObs registered");


        Log.d(TAG, "Plugin Started");
    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "Plugin is destroyed");

        super.onDestroy();

        getContentResolver().unregisterContentObserver(geonameResolverObs);
        getContentResolver().unregisterContentObserver(termCollectorGeoTermObs);

    }


    public class TermCollectorGeoTermObserver extends ContentObserver {
        public TermCollectorGeoTermObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.wtf(TAG, "@onChange (TermCollectorGeoTermObserver)");


            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    termCollectorGeoTermContentUri, null, null, null,
                    "timestamp" + " DESC LIMIT 1");

            if (cursor != null && cursor.moveToFirst()) {

                saveGeoData(cursor.getLong(cursor.getColumnIndex("timestamp")),
                        termCollectorGeoTermContentUri.toString(), cursor.getString(cursor
                        .getColumnIndex("term_content")));
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

    }

    public class GeonameResolverObserver extends ContentObserver {
        public GeonameResolverObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.wtf(TAG, "@onChange (GeonameResolverObserver)");

            // run only, if use of location is allowed
            if  (getUseLocation()){
            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    geonameResolverContentUri, null, null, null,
                    "timestamp" + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {

                saveGeoData(cursor.getLong(cursor.getColumnIndex("timestamp")),
                        geonameResolverContentUri.toString(), cursor.getString(cursor
                        .getColumnIndex("NAME")));
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            }
        }

    }

    private void saveGeoData(long timestamp, String source, String content) {
        //Log.d(TAG, "Saving Data");

        ContentValues rowData = new ContentValues();
        rowData.put(GeoCollectorTermData.DEVICE_ID, Aware.getSetting(
                getContentResolver(), Aware_Preferences.DEVICE_ID));
        rowData.put(GeoCollectorTermData.TIMESTAMP, timestamp);
        rowData.put(GeoCollectorTermData.TERM_SOURCE, source);
        rowData.put(GeoCollectorTermData.TERM_CONTENT, content);

        Log.d(TAG, "Saving " + rowData.toString());
        getContentResolver().insert(GeoCollectorTermData.CONTENT_URI, rowData);
    }

    public boolean getUseLocation(){
        String useLocationString = Aware.getSetting(getContentResolver(), "AWARE_USE_LOCATION");
        if(useLocationString != null) {
            try {
                return Boolean.parseBoolean(useLocationString);
            } catch (NumberFormatException e) {
                return true;
            }
        } else {
            return true;
        }
    }

}
