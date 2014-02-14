package com.aware.plugin.automatic_query;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.aware.plugin.automatic_query.querymanagement.QueryManager;
import com.aware.plugin.automatic_query.querymanagement.QueryObject;
import com.aware.plugin.automatic_query.querymanagement.WhatObject;
import com.aware.plugin.automatic_query.querymanagement.WhereObject;
import com.aware.plugin.automatic_query.situations.SituationManager;
import com.aware.utils.Aware_Plugin;

/**
 * A Tool, that listens to the TermCollector, does Europeana Queries and sends out a Notification
 *
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
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

    private QueryManager queryManager;

    private boolean isRunnableRunning = false;

    private int runNumber;


    /**
     * Thread manager
     */
    private static HandlerThread threads = null;
    private Handler handler = new Handler();


    @Override
    public void onCreate() {
        Log.d(TAG, "Plugin Created");
        super.onCreate();

        situationManager = new SituationManager(getApplicationContext());

        queryManager = new QueryManager();

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
        handler.removeCallbacks(runnable);

    }

    protected void maybeRunQuery() {
        if (situationManager.allowsQuery()) {
            QueryObject queryObject = queryManager.getNextQueryObject();
            if (queryObject != null) {
                Log.d(TAG, "Running Query with where =  " + queryObject.getWhereObject().getValue() + " what = " + queryObject.getWhatObject().getValue()
                        + " and Affinity of " + queryObject.getAffinity());
                new ExecuteSearchTask(this).execute(new String[]{"0", queryObject.getWhereObject().getValue(), queryObject.getWhatObject().getValue()});
            } else {
                Log.d(TAG, "No QueryObject available");
            }
        } else {
            Log.d(TAG, "Query not allowed at the moment");
        }
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
                String localWhat = cursor.getString(cursor.getColumnIndex("term_content"));
                long localTimestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                String localSource = cursor.getString(cursor.getColumnIndex("term_source"));

                queryManager.addWhatObject(new WhatObject(localTimestamp, localSource, localWhat));

                maybeStartRunnable();
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
                String localWhere = cursor.getString(cursor
                        .getColumnIndex("term_content"));
                long localTimestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                String localSource = cursor.getString(cursor.getColumnIndex("term_source"));

                queryManager.addWhereObject(new WhereObject(localTimestamp, localSource, localWhere));

                maybeStartRunnable();
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


    private Runnable runnable = new Runnable()
    {

        public void run()
        {
            // running query
            runNumber = runNumber + 1;
            maybeRunQuery();
            Log.d(TAG, "Runnumber " + runNumber);
            if(runNumber < 150) {
                handler.postDelayed(this, 1000);
            } else {
                isRunnableRunning = false;
            }
        }
    };


    private void maybeStartRunnable(){
        // always reset runnumber when this is called, i.e. when context arrived
        Log.d(TAG, "@maybeStartRunnable");

        runNumber = 0;

        if(!isRunnableRunning){
            isRunnableRunning = true;
            runnable.run();
        }

    }
}
