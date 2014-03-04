package com.aware.plugin.automatic_query;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.aware.plugin.automatic_query.europeana.ExecuteSearchTask;
import com.aware.plugin.automatic_query.querymanagement.QueryManager;
import com.aware.plugin.automatic_query.querymanagement.QueryObject;
import com.aware.plugin.automatic_query.querymanagement.WhatObject;
import com.aware.plugin.automatic_query.querymanagement.WhereObject;
import com.aware.plugin.automatic_query.situations.SituationManager;
import com.aware.utils.Aware_Plugin;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

/**
 * A Tool, that listens to the TermCollector, does Europeana Queries and sends out a Notification
 *
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
 */

public class Plugin extends Aware_Plugin {

    private final static String TAG = ContextophelesConstants.TAG_AUTOMATIC_QUERY + " Plugin";
    public static Uri geoCollectorContentUri;
    public static Uri termCollectorContentUri;
    public static Uri lightContentUri;
    private static GeoCollectorObserver geoCollectorObs = null;
    private static TermCollectorObserver termCollectorObs = null;
    private static LightObserver lightObs = null;
    private static HandlerThread threads = null;
    private int notificationNumber = 0;
    private static SituationManager situationManager;
    private QueryManager queryManager;
    private boolean isRunnableRunning = false;
    private int runNumber;
    private Runnable runnable = new Runnable() {

        public void run() {
            // running query
            runNumber = runNumber + 1;
            maybeRunQuery();

            if (runNumber < ContextophelesConstants.AQ_PLUGIN_MAX_NUMBER_OF_EMPTY_RUNS_BEFORE_SLEEP) {
                handler.postDelayed(this, ContextophelesConstants.AQ_PLUGIN_TIME_TO_WAIT_BETWEEN_RUNS);
            } else {
                isRunnableRunning = false;
            }
        }
    };

    private Handler handler = new Handler();

    public static SituationManager getSituationManager(){
        return situationManager;
    }

    public int getNotificationNumber() {
        return notificationNumber;
    }

    public void setNotificationNumber(int notificationNumber) {
        this.notificationNumber = notificationNumber;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating Plugin");
        super.onCreate();

        situationManager = new SituationManager(getApplicationContext());

        queryManager = new QueryManager(getApplicationContext());

        threads = new HandlerThread(TAG);
        threads.start();

        // Set the observers, that run in independent threads, for
        // responsiveness

        termCollectorContentUri = ContextophelesConstants.TERM_COLLECTOR_TERM_CONTENT_URI;
        termCollectorObs = new TermCollectorObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                termCollectorContentUri, true, termCollectorObs);
        Log.d(TAG, "termCollectorObs registered");


        geoCollectorContentUri = ContextophelesConstants.GEO_COLLECTOR_CONTENT_URI;
        geoCollectorObs = new GeoCollectorObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                geoCollectorContentUri, true, geoCollectorObs);
        Log.d(TAG, "geoCollectorObs registered");


        lightContentUri = Uri.parse(ContextophelesConstants.LIGHT_URI);
        lightObs = new LightObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                lightContentUri, true, lightObs);
        Log.d(TAG, "lightObs registered");

        Log.d(TAG, "Plugin Started");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying Plugin");
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

                // TODO: Log to Databse
                Log.d(TAG, "Running Query with where =  " + queryObject.getWhereObject().getValue() + " what = " + queryObject.getWhatObject().getValue()
                        + " and Affinity of " + queryObject.getAffinity());
                new ExecuteSearchTask(this).execute(new String[]{"0", queryObject.getWhereObject().getValue(), queryObject.getWhatObject().getValue()});
            } else {
                // TODO: Log to Databse
                Log.d(TAG, "No QueryObject available");
            }
        } else {
            // TODO: Log to Databse
            Log.d(TAG, "Query not allowed at the moment");
        }
    }

    private void maybeStartRunnable() {
        // always reset runnumber when this is called, i.e. when context arrived
        runNumber = 0;

        if (!isRunnableRunning) {
            isRunnableRunning = true;
            runnable.run();
        }

    }

    public class TermCollectorObserver extends ContentObserver {
        public TermCollectorObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            // TODO: Log to Databse
            Log.d(TAG, "@onChange of TermCollectorObserver");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    termCollectorContentUri, null, null, null,
                    ContextophelesConstants.TERM_COLLECTOR_FIELD_TIMESTAMP + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {
                String localWhat = cursor.getString(cursor.getColumnIndex(ContextophelesConstants.TERM_COLLECTOR_TERM_FIELD_TERM_CONTENT));
                long localTimestamp = cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.TERM_COLLECTOR_FIELD_TIMESTAMP));
                String localSource = cursor.getString(cursor.getColumnIndex(ContextophelesConstants.TERM_COLLECTOR_TERM_FIELD_TERM_SOURCE));

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

            // TODO: Log to Databse
            Log.d(TAG, "@onChange of GeoCollectorObserver");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    geoCollectorContentUri, null, null, null,
                    ContextophelesConstants.GEO_COLLECTOR_FIELD_TIMESTAMP  + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {
                String localWhere = cursor.getString(cursor
                        .getColumnIndex(ContextophelesConstants.GEO_COLLECTOR_FIELD_TERM_CONTENT));
                long localTimestamp = cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.GEO_COLLECTOR_FIELD_TIMESTAMP));
                String localSource = cursor.getString(cursor.getColumnIndex(ContextophelesConstants.GEO_COLLECTOR_FIELD_TERM_SOURCE));

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
            // TODO: Log to Databse
            //Log.d(TAG, "@onChange of LightObserver");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    lightContentUri, null, null, null,
                    ContextophelesConstants.LIGHT_FIELD_TIMESTAMP + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {

                Double lux = Double.parseDouble(cursor.getString(cursor
                        .getColumnIndex(ContextophelesConstants.LIGHT_FIELD_DOUBLE_LUX)));

                situationManager.putContextValue(ContextophelesConstants.SITUATION_MANAGER_LIGHT, lux);
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
