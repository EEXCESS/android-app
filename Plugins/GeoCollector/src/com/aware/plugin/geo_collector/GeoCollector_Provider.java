package com.aware.plugin.geo_collector;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

/**
 * ContentProvider for the Geo Collector
 */

public class GeoCollector_Provider extends ContentProvider {

    private final String TAG = "GeoCollector Provider";
    public static final String AUTHORITY = "com.aware.provider.plugin.geo_collector";

    private static final int DATABASE_VERSION = 5;

    private static final int GEO_COLLECTOR_TERMS = 1;
    private static final int GEO_COLLECTOR_TERMS_ID = 2;

    private static UriMatcher uriMatcher = null;
    private static HashMap<String, String> contentMapTerms = null;
    private static DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    public static final class GeoCollectorTermData implements BaseColumns {
        private GeoCollectorTermData() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/plugin_geo_collector_terms"); //this needs to match the table name
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.geo_collector";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.geo_collector";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";

        /**
         * Content of Geo
         */
        public static final String TERM_CONTENT = "term_content";

        /**
         * Source of Geo
         */
        public static final String TERM_SOURCE = "term_source";
    }

    public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_geo_collector.db";

    public static final String[] DATABASE_TABLES = {
            "plugin_geo_collector_terms"
    };

    public static final String[] TABLES_FIELDS = {
            //GeoData
            GeoCollectorTermData._ID + " integer primary key autoincrement," +
                    GeoCollectorTermData.TIMESTAMP + " real default 0," +
                    GeoCollectorTermData.DEVICE_ID + " text default ''," +
                    GeoCollectorTermData.TERM_CONTENT + " text default ''," +
                    GeoCollectorTermData.TERM_SOURCE + " text default ''",
                    //+ "UNIQUE (" + GeoCollectorTermData.TIMESTAMP + "," + GeoCollectorTermData.DEVICE_ID + "," + GeoCollectorTermData.GEO_CONTENT + ")",


    };

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], GEO_COLLECTOR_TERMS);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", GEO_COLLECTOR_TERMS_ID);


        contentMapTerms = new HashMap<String, String>();
        contentMapTerms.put(GeoCollectorTermData._ID, GeoCollectorTermData._ID);
        contentMapTerms.put(GeoCollectorTermData.TIMESTAMP, GeoCollectorTermData.TIMESTAMP);
        contentMapTerms.put(GeoCollectorTermData.DEVICE_ID, GeoCollectorTermData.DEVICE_ID);
        contentMapTerms.put(GeoCollectorTermData.TERM_CONTENT, GeoCollectorTermData.TERM_CONTENT);
        contentMapTerms.put(GeoCollectorTermData.TERM_SOURCE, GeoCollectorTermData.TERM_SOURCE);


    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (database == null || !database.isOpen()) database = databaseHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
            case GEO_COLLECTOR_TERMS:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case GEO_COLLECTOR_TERMS:
                return GeoCollectorTermData.CONTENT_TYPE;
            case GEO_COLLECTOR_TERMS_ID:
                return GeoCollectorTermData.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (database == null || !database.isOpen()) database = databaseHelper.getWritableDatabase();

        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

        long _id;
        switch (uriMatcher.match(uri)) {
            case GEO_COLLECTOR_TERMS:
                _id = database.insert(DATABASE_TABLES[0], GeoCollectorTermData.TIMESTAMP, values);
                //Log.wtf(TAG, "Id: " + _id);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(GeoCollectorTermData.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        if (databaseHelper == null)
            databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        database = databaseHelper.getWritableDatabase();
        return (databaseHelper != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (database == null || !database.isOpen()) database = databaseHelper.getWritableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case GEO_COLLECTOR_TERMS:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(contentMapTerms);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG) Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        if (database == null || !database.isOpen()) database = databaseHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
            case GEO_COLLECTOR_TERMS:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                database.close();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Log.d(TAG, "Notifying about change");
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}