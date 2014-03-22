package com.aware.plugin.term_collector;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.aware.plugin.term_collector.TermCollector_Provider.TermCollectorGeoData;
import com.aware.plugin.term_collector.TermCollector_Provider.TermCollectorTermData;
import com.aware.utils.Aware_Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.BlacklistedApps;
import de.unipassau.mics.contextopheles.utils.CommonConnectors;
import de.unipassau.mics.contextopheles.utils.CommonSettings;
import de.unipassau.mics.contextopheles.utils.StopWords;
import io.mingle.v1.Mingle;

/**
 * Main Plugin for the TERM Collector
 *
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
 */

public class Plugin extends Aware_Plugin {

    private static final String TAG = ContextophelesConstants.TAG_TERM_COLLECTOR + " Plugin";
    public static final String ACTION_AWARE_TERMCOLLECTOR = "ACTION_AWARE_TERMCOLLECTOR";

    private static StopWords stopWords;
    private static CommonConnectors commonConnectors;
    private static BlacklistedApps blacklistedApps;

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

    public static Uri uiContentContentUri;
    private static UIContentObserver uiContentObs = null;

    public static Uri geonameResolverContentUri;
    private static GeonameResolverObserver geonameResolverObs = null;

    private static String sanitizingString = "[^A-Za-zÄÖÜäöüß\\- ]";

    /**
     * Thread manager
     */
    private static HandlerThread threads = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "Plugin Created");
        super.onCreate();

        stopWords = new StopWords(getApplicationContext());
        commonConnectors = new CommonConnectors(getApplicationContext());
        blacklistedApps = new BlacklistedApps(getApplicationContext());

        // Share the context back to the framework and other applications
        CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
            @Override
            public void onContext() {
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

        clipboardCatcherContentUri = ContextophelesConstants.CLIPBOARD_CATCHER_CONTENT_URI;
        clipboardCatcherObs = new ClipboardCatcherObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                clipboardCatcherContentUri, true, clipboardCatcherObs);
        Log.d(TAG, "clipboardCatcherObs registered");

        notificationCatcherContentUri = ContextophelesConstants.NOTIFICATION_CATCHER_CONTENT_URI;
        notificationCatcherObs = new NotificationCatcherObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                notificationCatcherContentUri, true, notificationCatcherObs);
        Log.d(TAG, "notificationCatcherObs registered");


        smsReceiverContentUri = ContextophelesConstants.SMS_RECEIVER_CONTENT_URI;
        smsReceiverObs = new SmsReceiverObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                smsReceiverContentUri, true, smsReceiverObs);
        Log.d(TAG, "smsReceiverObs registered");


        osmpoiResolverContentUri = ContextophelesConstants.OSMPOI_RESOLVER_CONTENT_URI;
        osmpoiResolverObs = new OSMPoiResolverObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                osmpoiResolverContentUri, true, osmpoiResolverObs);
        Log.d(TAG, "osmpoiResolverObs registered");


        uiContentContentUri = ContextophelesConstants.UI_CONTENT_CONTENT_URI;
        uiContentObs = new UIContentObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                uiContentContentUri, true, uiContentObs);
        Log.d(TAG, "uiContentObs registered");


        geonameResolverContentUri = ContextophelesConstants.GEONAME_RESOLVER_CONTENT_URI;
        geonameResolverObs = new GeonameResolverObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                geonameResolverContentUri, true, geonameResolverObs);
        Log.d(TAG, "geonameResolverObs registered");

        Log.d(TAG, "Plugin Started");
    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "Plugin is destroyed");

        super.onDestroy();

        getContentResolver().unregisterContentObserver(clipboardCatcherObs);
        getContentResolver().unregisterContentObserver(notificationCatcherObs);
        getContentResolver().unregisterContentObserver(smsReceiverObs);
        getContentResolver().unregisterContentObserver(osmpoiResolverObs);
        getContentResolver().unregisterContentObserver(uiContentObs);
        getContentResolver().unregisterContentObserver(geonameResolverObs);
    }

    public class ClipboardCatcherObserver extends ContentObserver {
        public ClipboardCatcherObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.d(TAG, "@onChange (ClipboardCatcherObserver)");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    clipboardCatcherContentUri, null, null, null,
                    ContextophelesConstants.CLIPBOARD_CATCHER_FIELD_TIMESTAMP + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {

                String content = cursor.getString(cursor
                        .getColumnIndex(ContextophelesConstants.CLIPBOARD_CATCHER_FIELD_CLIPBOARDCONTENT));

                String[] contentTokens = splitAndReformulateContent(content);

                ArrayList<String>[] lists = classifyData(contentTokens);

                long timestamp = cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.CLIPBOARD_CATCHER_FIELD_TIMESTAMP));
                String source = clipboardCatcherContentUri.toString();

                DataHelper.saveGeoDataFromList(getContentResolver(),  timestamp, source, lists[0]);
                DataHelper.saveTermDataFromList(getContentResolver(), timestamp, source, lists[1]);
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

            Log.d(TAG, "@onChange (NotificationCatcherObserver)");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    notificationCatcherContentUri, null, null, null,
                    ContextophelesConstants.NOTIFICATION_CATCHER_FIELD_TIMESTAMP + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {

                String applicationName =  cursor.getString(cursor
                        .getColumnIndex(ContextophelesConstants.NOTIFICATION_CATCHER_FIELD_APP_NAME));

                if (!blacklistedApps.isBlacklistedApp(applicationName)) {
                    // get title and content_text
                    String content = cursor.getString(cursor
                            .getColumnIndex(ContextophelesConstants.NOTIFICATION_CATCHER_FIELD_TEXT)) + " " + cursor.getString(cursor
                            .getColumnIndex(ContextophelesConstants.NOTIFICATION_CATCHER_FIELD_TITLE));

                    String[] contentTokens = splitAndReformulateContent(content);

                    ArrayList<String>[] lists = classifyData(contentTokens);

                    long timestamp = cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.NOTIFICATION_CATCHER_FIELD_TIMESTAMP));
                    String source = notificationCatcherContentUri.toString();

                    DataHelper.saveGeoDataFromList(getContentResolver(),  timestamp, source, lists[0]);
                    DataHelper.saveTermDataFromList(getContentResolver(), timestamp, source, lists[1]);
                } else {
                    Log.d(TAG, "Notification from Application " + cursor.getString(cursor
                            .getColumnIndex(ContextophelesConstants.NOTIFICATION_CATCHER_FIELD_APP_NAME)) + " was ignored (Cause: Blacklist)");
                }
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
                    ContextophelesConstants.SMS_RECEIVER_FIELD_TIMESTAMP + " DESC LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {
                String content = cursor.getString(cursor.getColumnIndex(ContextophelesConstants.SMS_RECEIVER_FIELD_SMSContent));

                String[] contentTokens = splitAndReformulateContent(content);

                long timestamp = cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.SMS_RECEIVER_FIELD_TIMESTAMP));
                String source = smsReceiverContentUri.toString();
                ArrayList<String>[] lists = classifyData(contentTokens);

                DataHelper.saveGeoDataFromList(getContentResolver(),  timestamp, source, lists[0]);
                DataHelper.saveTermDataFromList(getContentResolver(), timestamp, source, lists[1]);
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

            // run only, if use of location is allowed
            if (CommonSettings.getQueryUseOfLocation(getContentResolver())) {
                // set cursor to first item
                Cursor cursor = getContentResolver().query(
                        osmpoiResolverContentUri, null, null, null,
                        ContextophelesConstants.OSMPOI_RESOLVER_FIELD_TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {


                    // POIs come in packages of one, so we do not need to split and filter them, so we package each one in an array
                    String content = cursor.getString(cursor
                            .getColumnIndex(ContextophelesConstants.OSMPOI_RESOLVER_FIELD_NAME));
                    String type =  cursor.getString(cursor
                            .getColumnIndex(ContextophelesConstants.OSMPOI_RESOLVER_FIELD_TYPE));

                    long timestamp = cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.OSMPOI_RESOLVER_FIELD_TIMESTAMP));
                    String source = osmpoiResolverContentUri.toString();

                    // String[] singleTokenArray = new String[]{content};
                    //ArrayList<String>[] lists = classifyData(singleTokenArray);

                    // As we already know the type of the POI, we can easily add it to GeoData if needed
                    if(type.equals("POI_SUBURB") || type.equals("POI_TOWN") || type.equals("POI_CITY")){
                        DataHelper.saveGeoData(getContentResolver(), timestamp, source, content);
                    }

                     DataHelper.saveTermData(getContentResolver(), timestamp, source, content);
                }

                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } else {
                Log.d(TAG, "Termcollector ignores input, as useOfLocation is disabled.");
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

            Log.wtf(TAG, "@GeonameResolverObserver");

            // run only, if use of location is allowed
            if (CommonSettings.getQueryUseOfLocation(getContentResolver())) {
                // set cursor to first item
                Cursor cursor = getContentResolver().query(
                        geonameResolverContentUri, null, null, null,
                        ContextophelesConstants.GEONAME_RESOLVER_FIELD_TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {

                    String content = cursor.getString(cursor
                            .getColumnIndex(ContextophelesConstants.GEONAME_RESOLVER_FIELD_NAME));

                    // Data from the GeonameResolver is directly saved to both data and geotable
                    DataHelper.saveTermData(getContentResolver(), cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.GEONAME_RESOLVER_FIELD_TIMESTAMP)), geonameResolverContentUri.toString(), content);
                    DataHelper.saveGeoData(getContentResolver(), cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.GEONAME_RESOLVER_FIELD_TIMESTAMP)), geonameResolverContentUri.toString(), content);
                }

                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }else {
                Log.d(TAG, "GeonameResolver ignores input, as useOfLocation is disabled.");
            }
        }

    }

    public class UIContentObserver extends ContentObserver {
        long lastId;

        public UIContentObserver(Handler handler) {
            super(handler);
            lastId = -1;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.wtf(TAG, "@UIContentObs");

            // set cursor to first item
            Cursor cursor = getContentResolver().query(
                    uiContentContentUri, null, null, null,
                    ContextophelesConstants.UI_CONTENT_FIELD_TIMESTAMP + " DESC LIMIT 1");

            if (cursor != null && cursor.moveToFirst()) {

                String applicationName = cursor.getString(cursor
                        .getColumnIndex(ContextophelesConstants.UI_CONTENT_FIELD_SOURCE_APP));

                if (!blacklistedApps.isBlacklistedApp(applicationName)) {


                    //check, if the ID has changed, if not abort
                    long newId = cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.UI_CONTENT_FIELD_ID));

                    // ID has not changed
                    if (lastId == newId) {
                        return;
                    } else {
                        lastId = newId;
                    }

                    String content = cursor.getString(cursor.getColumnIndex(ContextophelesConstants.UI_CONTENT_FIELD_TEXT));

                    String[] contentTokens = splitAndReformulateContent(content);

                    ArrayList<String>[] lists = classifyData( contentTokens);

                    long timestamp = cursor.getLong(cursor.getColumnIndex(ContextophelesConstants.UI_CONTENT_FIELD_TIMESTAMP));
                    String source = uiContentContentUri.toString();

                    DataHelper.saveGeoDataFromList(getContentResolver(),  timestamp, source, lists[0]);
                    DataHelper.saveTermDataFromList(getContentResolver(), timestamp, source, lists[1]);

                } else {
                    Log.d(TAG, "UIContent from Application " + cursor.getString(cursor
                            .getColumnIndex(ContextophelesConstants.UI_CONTENT_FIELD_SOURCE_APP)) + " was ignored (Cause: Blacklist)");
                }
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

    }

    // Takes a String and splits it by Whitespace.
    // It then filters out unallowed Nouns (e.g. if they are too short
    // if 3 tokens in the array have the form <AllowedNoun>, <CommonConnector>, <AllowedNoun> or
    // <AllowedNoun>, <NoPunctuation>, <AllowedNoun> they are joined with " " and added additionally 
    private String[] splitAndReformulateContent(String content) {
        Log.wtf(TAG, "Splitting Content: " + content);

        String[] tokenArray = content.split("\\s+");
        //remove all characters that are not A-Za-z
        ArrayList<String> resultList = new ArrayList<String>();

        String latex = "\\boxed{" + TextUtils.join("} \\boxed{", tokenArray) + "}";
        int chunksize = 1000;
        for(int i=0; i< latex.length(); i = i + chunksize){
            Log.wtf(TAG, "Reformulating Contentlist: "  + latex.substring(i, Math.min(i+chunksize, latex.length())));
        }



        for (int i = 0; i < tokenArray.length; i++) {
            String token = tokenArray[i];
            if (tokenIsAllowedNoun(token)) {
                resultList.add(token);
                //token does not end in a punctuation mark
                if (!token.matches(".*[\\p{Punct}]")) {

                    // there is still another token
                    if (i + 1 < tokenArray.length) {
                        //and it is an allowed Noun or a commonConnector
                        if (tokenIsAllowedNoun(tokenArray[i + 1])) {
                            // Add the Combination of the two to the List
                            resultList.add(TextUtils.join(" ", new String[]{token, tokenArray[i + 1]}));
                        } else {
                            if (commonConnectors.isCommonConnector(tokenArray[i + 1])) {
                                // there is yet another token
                                if (i + 2 < tokenArray.length) {
                                    if (tokenIsAllowedNoun(tokenArray[i + 2])) {
                                        // Add the Combination of the three to the List
                                        resultList.add(token + " " + tokenArray[i + 1] + " " + tokenArray[i + 2]);
                                    }
                                }
                            }
                        }
                    }

                    // there are even two more tokens
                    if (i + 3 < tokenArray.length) {
                        //it is a common connector with a space
                        if (commonConnectors.isCommonConnector(tokenArray[i + 1] + " " + tokenArray[i + 2])) {
                            if (tokenIsAllowedNoun(tokenArray[i + 3])) {
                                resultList.add(token + " " + tokenArray[i + 1] + " " + tokenArray[i + 2] + " " + tokenArray[i + 3]);
                            }
                        }

                        //it is a common connector without a space
                        if (commonConnectors.isCommonConnector(tokenArray[i + 1] + tokenArray[i + 2])) {
                            if (tokenIsAllowedNoun(tokenArray[i + 3])) {
                                resultList.add(token + " " + tokenArray[i + 1] + tokenArray[i + 2] + " " + tokenArray[i + 3]);
                            }
                        }
                    }
                }
            }
        }
        return resultList.toArray(new String[resultList.size()]);

    }

    //Takes an array of Strings, removes unwanted characters in every string and returns the contents as a list
    private ArrayList<String> sanitizeTokens(String[] tokens) {
        Log.wtf(TAG, "Sanitizing tokens: \\boxed{" + TextUtils.join("} \\boxed{", tokens) + "}");
        ArrayList<String> filteredTokens = new ArrayList<String>();

        for (String token : tokens) {
            filteredTokens.add(token.replaceAll(sanitizingString, ""));
        }

        //deduplicate tokens
        filteredTokens= new ArrayList<String>(new LinkedHashSet<String>(filteredTokens));

        Log.wtf(TAG, "Sanitized tokens: \\boxed{" + TextUtils.join("} \\boxed{", filteredTokens) + "}");
        return filteredTokens;
    }

    // Takes a list of contentTokens and returns an Array of length 2, with a list of cityTokens at position 0 and a list of nonCityTokens at position 1.
    private ArrayList<String>[] classifyData(String[] contentTokens) {
        Log.wtf(TAG, "Classifying tokens: \\boxed{" + TextUtils.join("} \\boxed{", contentTokens) + "}");

        //filter any unwanted Characters from the input
        ArrayList<String> filteredTokens = sanitizeTokens(contentTokens);

        //classify cities
        ArrayList<String> cityTokens = new ArrayList<String>();
        ArrayList<String> nonCityTokens = new ArrayList<String>();
        ArrayList<String> tokensToCheck = new ArrayList<String>();

        //defines, if cities are resolved sequential or all at once (WAY FASTER!)
        boolean sequential = false;

        // check if the token is a known city/geoname from the cache
        // if the token is in the cache, get the value and enter it into cityTokens or nonCityTokens
        // add it to tokensToCheck otherwise
        for (String filteredToken : filteredTokens) {
            if (DataHelper.isInCache(getContentResolver(), filteredToken)) {
                if (DataHelper.isCityFromCache(getContentResolver(), filteredToken)) {
                    // Add City to both Lists, to use in What and When field
                    nonCityTokens.add(filteredToken);
                    cityTokens.add(filteredToken);
                } else {
                    // Use it only in WhatField
                    nonCityTokens.add(filteredToken);
                }
            } else {
                tokensToCheck.add(filteredToken);
            }
        }


        if (!sequential) { //new, at once check
            Set<String> cities = new HashSet<String>();

            // Get Cities for filteredTokens
            try {
                Mingle mingle = new Mingle(getApplicationContext());
                cities = mingle.geonames().areCities(tokensToCheck);
            } catch (Exception e) {
                // e.printStackTrace();
            }

            // Check, which tokens are in the cities list
            for (String token : tokensToCheck) {
                if (cities.contains(token)) {
                    Log.wtf(TAG, token + " is a city (At once)");
                    // Add City to both Lists, to use in What and When field
                    nonCityTokens.add(token);
                    cityTokens.add(token);
                    DataHelper.saveToCache(getContentResolver(), System.currentTimeMillis(), true, token);
                } else {
                    nonCityTokens.add(token);
                    DataHelper.saveToCache(getContentResolver(), System.currentTimeMillis(), false, token);
                    Log.wtf(TAG, token + " is not a city (At once)");
                }
            }
        } else {  // OLD, sequential check
            for (String token : filteredTokens) {
                {
                    Log.wtf(TAG, "Resolving " + token);
                    // dissolve them with mingle
                    Mingle mingle;

                    try {
                        mingle = new Mingle(getApplicationContext());

                        if (mingle.geonames().existsPopulatedPlaceWithName(token)) {
                            Log.wtf(TAG, token + " is a city (Sequential)");
                            nonCityTokens.add(token);
                            cityTokens.add(token);
                            DataHelper.saveToCache(getContentResolver(), System.currentTimeMillis(), true, token);
                        } else {
                            nonCityTokens.add(token);
                            DataHelper.saveToCache(getContentResolver(), System.currentTimeMillis(), false, token);
                            Log.wtf(TAG, token + " is not a city (Sequential)");
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                    }
                }
            }
        }

        return new ArrayList[]{cityTokens, nonCityTokens};
    }

    //Checks, whether the given token does not break a set of rules, e.g. minlength or stopword
    private boolean tokenIsAllowedNoun(String token) {
        Log.wtf(TAG, "Testing for allowed noun: " + token);
        int minLength = CommonSettings.getMinimalTermCollectorTokenLength(getContentResolver());

        // Sanitize token temporarily:
        token = token.replaceAll(sanitizingString, "");

        boolean applyStopWords = CommonSettings.getTermCollectorApplyStopwords(getContentResolver());
        // filter tokens shorter than minLength characters
        if (token.length() >= minLength) {
            //only allow Uppercase tokens, which have no more Uppercase characters (avoids strange CamelCase errors like PassauTown)
            if (Character.isUpperCase(token.charAt(0)) &&
                    token.substring(1).equals(token.substring(1).toLowerCase())
                    ) {
                if(applyStopWords) {
                    if (stopWords.isStopWord(token)) {
                        Log.wtf(TAG, "Ignoring " + token + " as it is a stopword.");
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    // Stop Words are not applied
                    return true;
                }
            } else {
                Log.wtf(TAG, "Ignoring " + token + " as it is not uppercase.");
                return false;
            }

        } else {
            Log.wtf(TAG, "Ignoring " + token + " as it is shorter than " + minLength + " characters.");
            return false;
        }
    }
}
