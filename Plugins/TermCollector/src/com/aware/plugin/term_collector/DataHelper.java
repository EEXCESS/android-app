package com.aware.plugin.term_collector;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
* Utility Class to encapsulate Methods that save term and geodata
*/
public class DataHelper {
    final static String TAG = "TermCollectorDataHelper";

    //Takes a list of tokens, removes duplicates  and saves every single item as term data
    final static public void saveTermDataFromList(ContentResolver resolver, long timestamp, String source, ArrayList<String> tokens){
        //quick and easy dedup of the list:
        ArrayList<String> dedupeTokens= new ArrayList<String>(new LinkedHashSet<String>(tokens));

        StringBuilder sb = new StringBuilder();

        for (String token: dedupeTokens){
            sb.append("\\boxed{" + token + "} ");
        }


        Log.wtf(TAG, "Saving term Data List: " + sb.toString());

        // Save Non-City-Tokens to Term-Database, increase timestamp by 1 everytime
        int tokenIndex = 0;
        for (String token : dedupeTokens) {
            saveTermData(resolver, timestamp + tokenIndex, source, token);
            tokenIndex++;
        }
    }

    //Takes a list of tokens, removes duplicates and saves every single item as Geo data
    final static public void saveGeoDataFromList(ContentResolver resolver, long timestamp, String source, ArrayList<String> tokens){
        //quick and easy dedup of the list:
        ArrayList<String> dedupeTokens= new ArrayList<String>(new LinkedHashSet<String>(tokens));

        StringBuilder sb = new StringBuilder();

        for (String token: dedupeTokens){
            sb.append("\\boxed{" + token + "} ");
        }

        Log.wtf(TAG, "Saving geo Data List: " + sb.toString());


        // Save City-Tokens to Geo-Database, increase timestamp by 1 everytime
        int tokenIndex = 0;
        for (String token : dedupeTokens) {
            saveGeoData(resolver, timestamp + tokenIndex, source, token);
            tokenIndex++;
        }
    }

    //Takes a token and saves it as term data
    final static public void saveTermData(ContentResolver resolver, long timestamp, String source, String content) {

        ContentValues rowData = new ContentValues();
        rowData.put(TermCollector_Provider.TermCollectorTermData.DEVICE_ID, Aware.getSetting(
                resolver, Aware_Preferences.DEVICE_ID));
        rowData.put(TermCollector_Provider.TermCollectorTermData.TIMESTAMP, timestamp);
        rowData.put(TermCollector_Provider.TermCollectorTermData.TERM_SOURCE, source);
        rowData.put(TermCollector_Provider.TermCollectorTermData.TERM_CONTENT, content);

        Log.d(TAG, "Saving " + rowData.toString());
        resolver.insert(TermCollector_Provider.TermCollectorTermData.CONTENT_URI, rowData);
    }

    //Takes a token and saves it as geo data
    final static public void saveGeoData(ContentResolver resolver, long timestamp, String source, String content) {
        ContentValues rowData = new ContentValues();
        rowData.put(TermCollector_Provider.TermCollectorGeoData.DEVICE_ID, Aware.getSetting(
                resolver, Aware_Preferences.DEVICE_ID));
        rowData.put(TermCollector_Provider.TermCollectorGeoData.TIMESTAMP, timestamp);
        rowData.put(TermCollector_Provider.TermCollectorGeoData.TERM_SOURCE, source);
        rowData.put(TermCollector_Provider.TermCollectorGeoData.TERM_CONTENT, content);

        Log.d(TAG, "Saving " + rowData.toString());
        resolver.insert(TermCollector_Provider.TermCollectorGeoData.CONTENT_URI, rowData);
    }

    public static boolean isInCache(ContentResolver resolver, String token) {
        Cursor c = resolver.query(TermCollector_Provider.TermCollectorGeoDataCache.CONTENT_URI, null, TermCollector_Provider.TermCollectorGeoDataCache.TERM_CONTENT + " = " + DatabaseUtils.sqlEscapeString(token), null, null);

        boolean result = false;


        if (c != null && c.moveToFirst()) {
            result = c.getCount() > 0;
        }

        if (c != null && !c.isClosed()) {
            c.close();
        }

        if (result) {
            Log.d(TAG, "Cache hit " + token);
        } else {
            Log.d(TAG, "Cache miss " + token);
        }
        return result;
    }

    public static boolean isCityFromCache(ContentResolver resolver, String token) {
        Cursor c = resolver.query(TermCollector_Provider.TermCollectorGeoDataCache.CONTENT_URI, null, TermCollector_Provider.TermCollectorGeoDataCache.TERM_CONTENT + " = " + DatabaseUtils.sqlEscapeString(token), null, "timestamp" + " DESC LIMIT 1");
        boolean result = false;

        if (c != null && c.moveToFirst()) {
            result = c.getInt(c.getColumnIndex(TermCollector_Provider.TermCollectorGeoDataCache.IS_CITY)) > 0;
        }

        if (c != null && !c.isClosed()) {
            c.close();
        }
        return result;
    }

    public static void saveToCache(ContentResolver resolver, long timestamp, boolean isCity, String token) {
        ContentValues rowData = new ContentValues();

        rowData.put(TermCollector_Provider.TermCollectorGeoDataCache.TIMESTAMP, timestamp);

        if (isCity) {
            rowData.put(TermCollector_Provider.TermCollectorGeoDataCache.IS_CITY, 1);
        } else {
            rowData.put(TermCollector_Provider.TermCollectorGeoDataCache.IS_CITY, 0);
        }

        rowData.put(TermCollector_Provider.TermCollectorGeoDataCache.TERM_CONTENT, token);

        Log.d(TAG, "Saving to Cache: " + rowData.toString());
        resolver.insert(TermCollector_Provider.TermCollectorGeoDataCache.CONTENT_URI, rowData);
    }
}
