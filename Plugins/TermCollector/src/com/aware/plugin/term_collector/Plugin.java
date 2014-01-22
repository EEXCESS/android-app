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
import com.aware.plugin.term_collector.TermCollector_Provider.TermCollectorGeoData;
import com.aware.plugin.term_collector.TermCollector_Provider.TermCollectorTermData;
import com.aware.utils.Aware_Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.mingle.v1.Mingle;

/**
 * Main Plugin for the TERM Collector
 *
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
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

    public static Uri osmpoiResolverContentUri;
    private static OSMPoiResolverObserver osmpoiResolverObs = null;

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
        CONTEXT_URIS = new Uri[]{TermCollectorTermData.CONTENT_URI, TermCollectorGeoData.CONTENT_URI};

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

        notificationCatcherContentUri = Uri
                .parse("content://com.aware.provider.plugin.notification_catcher/plugin_notification_catcher");
        notificationCatcherObs = new NotificationCatcherObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                notificationCatcherContentUri, true, notificationCatcherObs);
        Log.d(TAG, "notificationCatcherObs registered");


        smsReceiverContentUri = Uri
                .parse("content://com.aware.provider.plugin.sms_receiver/plugin_sms_receiver");
        smsReceiverObs = new SmsReceiverObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                smsReceiverContentUri, true, smsReceiverObs);
        Log.d(TAG, "smsReceiverObs registered");


        osmpoiResolverContentUri = Uri
                .parse("content://com.aware.provider.plugin.osmpoi_resolver/plugin_osmpoi_resolver");
        osmpoiResolverObs = new OSMPoiResolverObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                osmpoiResolverContentUri, true, osmpoiResolverObs);
        Log.d(TAG, "osmpoiResolverObs registered");


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
        getContentResolver().unregisterContentObserver(osmpoiResolverObs);

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

                String[] tokens = splitAndFilterContent(cursor.getString(cursor
                        .getColumnIndex("CLIPBOARDCONTENT")));

                classifyAndSaveData(cursor.getLong(cursor.getColumnIndex("timestamp")),
                        clipboardCatcherContentUri.toString(), tokens);
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

                String[] tokens = splitAndFilterContent(cursor.getString(cursor
                        .getColumnIndex("TEXT")));

                classifyAndSaveData(cursor.getLong(cursor.getColumnIndex("timestamp")),
                        notificationCatcherContentUri.toString(), tokens);
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

                String[] tokens = splitAndFilterContent(cursor.getString(cursor
                        .getColumnIndex("SMSCONTENT")));

                classifyAndSaveData(cursor.getLong(cursor.getColumnIndex("timestamp")),
                        smsReceiverContentUri.toString(), tokens);
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public class OSMPoiResolverObserver extends ContentObserver {
        public OSMPoiResolverObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.wtf(TAG, "@OSMPoiResolverObs");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    osmpoiResolverContentUri, null, null, null,
                    "timestamp" + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {

                // POIs come in packages of one, so we do not need to split and filter them, so we package each one in an array
                String[] singleTokenArray = new String[]{cursor.getString(cursor
                        .getColumnIndex("NAME"))};

                classifyAndSaveData(cursor.getLong(cursor.getColumnIndex("timestamp")),
                        osmpoiResolverContentUri.toString(), singleTokenArray);
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

    }

    private String[] splitContent(String content) {
        Log.wtf(TAG, "Splitting Content");
        Log.wtf(TAG, "Content: " + content);

        //remove all characters that are not A-Za-z
        return content.replaceAll("[^A-Za-zÄÖÜäöü]", " ").split("\\s+");
    }

    private ArrayList<String> filterTokens(String[] tokens) {
        ArrayList<String> filteredTokens = new ArrayList<String>();

        // filter Lowercase tokens and tokens shorter than 3 characters
        for (String token : tokens) {
            if (token.length() > 2) {
                if (Character.isUpperCase(token.charAt(0))) {

                    filteredTokens.add(token);
                } else {
                    Log.wtf(TAG, "Ignoring " + token + " as it is not uppercase");
                }

            } else {
                Log.wtf(TAG, "Ignoring " + token + " as it is shorter than 3 characters.");
            }

        }

        return filteredTokens;
    }

    private String[] splitAndFilterContent(String content) {

        String[] contentTokens = splitContent(content);

        //filter Stopwords
        return stopList.filteredArray(contentTokens);
    }

    private void classifyAndSaveData(long timestamp, String source, String[] contentTokens) {

        //filter Lowercase words and words with less than 3 Characters
        ArrayList<String> filteredTokens = filterTokens(contentTokens);


        //classify cities
        ArrayList<String> cityTokens = new ArrayList<String>();
        ArrayList<String> nonCityTokens = new ArrayList<String>();

        //defines, if cities are resolved sequential or all at once (WAY FASTER!)
        boolean sequential = false;

        if (!sequential) { //new, at once check
            Set<String> cities = new HashSet<String>();

            // Get Cities for filteredTokens
            try {
                Mingle mingle = new Mingle(getApplicationContext());
                cities = mingle.geonames().areCities(filteredTokens);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();

            }

            // Check, which tokens are in the cities list
            for (String token : filteredTokens) {
                if (cities.contains(token)) {
                    Log.wtf(TAG, token + " is a city (At once)");
                    cityTokens.add(token);
                } else {
                    nonCityTokens.add(token);
                    Log.wtf(TAG, token + " is not a city (At once)");
                }
            }
        } else {  // OLD, sequential check
            for (String token : filteredTokens) {
                {
                    Log.wtf(TAG, "Dissolving " + token);
                    // dissolve them with mingle
                    Mingle mingle;

                    try {
                        mingle = new Mingle(getApplicationContext());

                        if (mingle.geonames().existsPopulatedPlaceWithName(token)) {
                            Log.wtf(TAG, token + " is a city (Sequential)");
                            cityTokens.add(token);
                        } else {
                            nonCityTokens.add(token);
                            Log.wtf(TAG, token + " is not a city (Sequential)");
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                    }
                }
            }
        }

        // Save Non-City-Tokens to Term-Database, increase timestamp by 1 everytime
        int tokenIndex = 0;
        for (String token : nonCityTokens) {
            saveTermData(timestamp + tokenIndex, source, token);
            tokenIndex++;
        }


        // Save City-Tokens to Geo-Database, increase timestamp by 1 everytime
        tokenIndex = 0;
        for (String token : cityTokens) {
            saveGeoData(timestamp + tokenIndex, source, token);
            tokenIndex++;
        }

    }

    private void saveTermData(long timestamp, String source, String content) {
        Log.d(TAG, "Saving Data");

        ContentValues rowData = new ContentValues();
        rowData.put(TermCollectorTermData.DEVICE_ID, Aware.getSetting(
                getContentResolver(), Aware_Preferences.DEVICE_ID));
        rowData.put(TermCollectorTermData.TIMESTAMP, timestamp);
        rowData.put(TermCollectorTermData.TERM_SOURCE, source);
        rowData.put(TermCollectorTermData.TERM_CONTENT, content);

        Log.d(TAG, "Saving " + rowData.toString());
        getContentResolver().insert(TermCollectorTermData.CONTENT_URI, rowData);
    }

    private void saveGeoData(long timestamp, String source, String content) {
        Log.d(TAG, "Saving Data");

        ContentValues rowData = new ContentValues();
        rowData.put(TermCollectorGeoData.DEVICE_ID, Aware.getSetting(
                getContentResolver(), Aware_Preferences.DEVICE_ID));
        rowData.put(TermCollectorGeoData.TIMESTAMP, timestamp);
        rowData.put(TermCollectorGeoData.TERM_SOURCE, source);
        rowData.put(TermCollectorGeoData.TERM_CONTENT, content);

        Log.d(TAG, "Saving " + rowData.toString());
        getContentResolver().insert(TermCollectorGeoData.CONTENT_URI, rowData);
    }

}
