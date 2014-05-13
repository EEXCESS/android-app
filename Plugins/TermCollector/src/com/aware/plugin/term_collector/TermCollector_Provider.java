package com.aware.plugin.term_collector;

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

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

/**
 * ContentProvider for the Term Collector
 */

public class TermCollector_Provider extends ContentProvider {

    private final String TAG = ContextophelesConstants.TAG_TERM_COLLECTOR + " Provider";
    public static final String AUTHORITY = ContextophelesConstants.TERM_COLLECTOR_AUTHORITY;

    public static final String TERM_TABLE = ContextophelesConstants.TERM_COLLECTOR_TERM_TABLE;
    public static final String GEODATA_TABLE = ContextophelesConstants.TERM_COLLECTOR_GEODATA_TABLE;
    public static final String CACHE_TABLE = ContextophelesConstants.TERM_COLLECTOR_CACHE_TABLE;

    private static final int DATABASE_VERSION = 7;

    private static final int TERM_COLLECTOR_TERMS = 1;
    private static final int TERM_COLLECTOR_TERMS_ID = 2;
    private static final int TERM_COLLECTOR_GEODATA = 3;
    private static final int TERM_COLLECTOR_GEODATA_ID = 4;
    private static final int TERM_COLLECTOR_GEODATA_CACHE = 5;
    private static final int TERM_COLLECTOR_GEODATA_CACHE_ID = 6;

    private static UriMatcher uriMatcher = null;
    private static HashMap<String, String> contentMapTerms = null;
    private static HashMap<String, String> contentMapGeodata = null;
    private static HashMap<String, String> contentMapGeodataCache = null;
    private static DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    public static final class TermCollectorTermData implements BaseColumns {
        private TermCollectorTermData() {
        }

        public static final Uri     CONTENT_URI = ContextophelesConstants.TERM_COLLECTOR_TERM_CONTENT_URI;
        public static final String  CONTENT_TYPE = ContextophelesConstants.TERM_COLLECTOR_CONTENT_TYPE;
        public static final String  CONTENT_ITEM_TYPE = ContextophelesConstants.TERM_COLLECTOR_CONTENT_ITEM_TYPE;

        public static final String  _ID =       ContextophelesConstants.TERM_COLLECTOR_FIELD_ID;
        public static final String  TIMESTAMP = ContextophelesConstants.TERM_COLLECTOR_FIELD_TIMESTAMP;
        public static final String  DEVICE_ID = ContextophelesConstants.TERM_COLLECTOR_FIELD_DEVICE_ID;

        public static final String  TERM_CONTENT = ContextophelesConstants.TERM_COLLECTOR_TERM_FIELD_TERM_CONTENT;
        public static final String  TERM_SOURCE  = ContextophelesConstants.TERM_COLLECTOR_TERM_FIELD_TERM_SOURCE ;

    }

    public static final class TermCollectorGeoData implements BaseColumns {
        private TermCollectorGeoData() {
        }

        public static final Uri     CONTENT_URI = ContextophelesConstants.TERM_COLLECTOR_GEODATA_CONTENT_URI;
        public static final String  CONTENT_TYPE = ContextophelesConstants.TERM_COLLECTOR_CONTENT_TYPE;
        public static final String  CONTENT_ITEM_TYPE = ContextophelesConstants.TERM_COLLECTOR_CONTENT_ITEM_TYPE;

        public static final String  _ID =       ContextophelesConstants.TERM_COLLECTOR_FIELD_ID;
        public static final String  TIMESTAMP = ContextophelesConstants.TERM_COLLECTOR_FIELD_TIMESTAMP;
        public static final String  DEVICE_ID = ContextophelesConstants.TERM_COLLECTOR_FIELD_DEVICE_ID;

        public static final String  TERM_CONTENT = ContextophelesConstants.TERM_COLLECTOR_GEODATA_FIELD_TERM_CONTENT;
        public static final String  TERM_SOURCE  = ContextophelesConstants.TERM_COLLECTOR_GEODATA_FIELD_TERM_SOURCE;
    }

    public static final class TermCollectorGeoDataCache implements BaseColumns {
        private TermCollectorGeoDataCache() {
        }

        public static final Uri     CONTENT_URI = ContextophelesConstants.TERM_COLLECTOR_CACHE_CONTENT_URI;
        public static final String  CONTENT_TYPE = ContextophelesConstants.TERM_COLLECTOR_CONTENT_TYPE;
        public static final String  CONTENT_ITEM_TYPE = ContextophelesConstants.TERM_COLLECTOR_CONTENT_ITEM_TYPE;

        public static final String  _ID =       ContextophelesConstants.TERM_COLLECTOR_FIELD_ID;
        public static final String  TIMESTAMP = ContextophelesConstants.TERM_COLLECTOR_FIELD_TIMESTAMP;

        public static final String  TERM_CONTENT = ContextophelesConstants.TERM_COLLECTOR_CACHE_FIELD_TERM_CONTENT;
        public static final String  IS_CITY =      ContextophelesConstants.TERM_COLLECTOR_CACHE_FIELD_IS_CITE;
    }

    public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_term_collector.db";

    public static final String[] DATABASE_TABLES = {
            TERM_TABLE,
            GEODATA_TABLE,
            CACHE_TABLE
    };

    public static final String[] TABLES_FIELDS = {
            //TermData
            TermCollectorTermData._ID + " integer primary key autoincrement," +
                    TermCollectorTermData.TIMESTAMP + " real default 0," +
                    TermCollectorTermData.DEVICE_ID + " text default ''," +
                    TermCollectorTermData.TERM_CONTENT + " text default ''," +
                    TermCollectorTermData.TERM_SOURCE + " text default ''",

            //GeoData
            TermCollectorGeoData._ID + " integer primary key autoincrement," +
                    TermCollectorGeoData.TIMESTAMP + " real default 0," +
                    TermCollectorGeoData.DEVICE_ID + " text default ''," +
                    TermCollectorGeoData.TERM_CONTENT + " text default ''," +
                    TermCollectorGeoData.TERM_SOURCE + " text default ''",

            //GeoDataCache
            TermCollectorGeoDataCache._ID + " integer primary key autoincrement," +
                    TermCollectorGeoDataCache.TIMESTAMP + " real default 0," +
                    TermCollectorGeoDataCache.TERM_CONTENT + " text default ''," +
                    TermCollectorGeoDataCache.IS_CITY + " real default 0"
    };

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], TERM_COLLECTOR_TERMS);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", TERM_COLLECTOR_TERMS_ID);

        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1], TERM_COLLECTOR_GEODATA);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1] + "/#", TERM_COLLECTOR_GEODATA_ID);

        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[2], TERM_COLLECTOR_GEODATA_CACHE);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[2] + "/#", TERM_COLLECTOR_GEODATA_CACHE_ID);

        contentMapTerms = new HashMap<String, String>();
        contentMapTerms.put(TermCollectorTermData._ID, TermCollectorTermData._ID);
        contentMapTerms.put(TermCollectorTermData.TIMESTAMP, TermCollectorTermData.TIMESTAMP);
        contentMapTerms.put(TermCollectorTermData.DEVICE_ID, TermCollectorTermData.DEVICE_ID);
        contentMapTerms.put(TermCollectorTermData.TERM_CONTENT, TermCollectorTermData.TERM_CONTENT);
        contentMapTerms.put(TermCollectorTermData.TERM_SOURCE, TermCollectorTermData.TERM_SOURCE);

        contentMapGeodata = new HashMap<String, String>();
        contentMapGeodata.put(TermCollectorGeoData._ID, TermCollectorGeoData._ID);
        contentMapGeodata.put(TermCollectorGeoData.TIMESTAMP, TermCollectorGeoData.TIMESTAMP);
        contentMapGeodata.put(TermCollectorGeoData.DEVICE_ID, TermCollectorGeoData.DEVICE_ID);
        contentMapGeodata.put(TermCollectorGeoData.TERM_CONTENT, TermCollectorGeoData.TERM_CONTENT);
        contentMapGeodata.put(TermCollectorGeoData.TERM_SOURCE, TermCollectorGeoData.TERM_SOURCE);

        contentMapGeodataCache = new HashMap<String, String>();
        contentMapGeodataCache.put(TermCollectorGeoDataCache._ID, TermCollectorGeoDataCache._ID);
        contentMapGeodataCache.put(TermCollectorGeoDataCache.TIMESTAMP, TermCollectorGeoDataCache.TIMESTAMP);
        contentMapGeodataCache.put(TermCollectorGeoDataCache.TERM_CONTENT, TermCollectorGeoDataCache.TERM_CONTENT);
        contentMapGeodataCache.put(TermCollectorGeoDataCache.IS_CITY, TermCollectorGeoDataCache.IS_CITY);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (database == null || !database.isOpen()) database = databaseHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
            case TERM_COLLECTOR_TERMS:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            case TERM_COLLECTOR_GEODATA:
                count = database.delete(DATABASE_TABLES[1], selection, selectionArgs);
                break;
            case TERM_COLLECTOR_GEODATA_CACHE:
                count = database.delete(DATABASE_TABLES[2], selection, selectionArgs);
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
            case TERM_COLLECTOR_TERMS:
                return TermCollectorTermData.CONTENT_TYPE;
            case TERM_COLLECTOR_TERMS_ID:
                return TermCollectorTermData.CONTENT_ITEM_TYPE;
            case TERM_COLLECTOR_GEODATA:
                return TermCollectorGeoData.CONTENT_TYPE;
            case TERM_COLLECTOR_GEODATA_ID:
                return TermCollectorGeoData.CONTENT_ITEM_TYPE;
            case TERM_COLLECTOR_GEODATA_CACHE:
                return TermCollectorGeoDataCache.CONTENT_TYPE;
            case TERM_COLLECTOR_GEODATA_CACHE_ID:
                return TermCollectorGeoDataCache.CONTENT_ITEM_TYPE;
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
            case TERM_COLLECTOR_TERMS:
                _id = database.insert(DATABASE_TABLES[0], TermCollectorTermData.TIMESTAMP, values);
                //Log.wtf(TAG, "Id: " + _id);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(TermCollectorTermData.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case TERM_COLLECTOR_GEODATA:
                _id = database.insert(DATABASE_TABLES[1], TermCollectorGeoData.TIMESTAMP, values);
                //Log.wtf(TAG, "Id: " + _id);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(TermCollectorGeoData.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case TERM_COLLECTOR_GEODATA_CACHE:
                _id = database.insert(DATABASE_TABLES[2], TermCollectorGeoDataCache.TIMESTAMP, values);
                Log.wtf(TAG, "Id: " + _id);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(TermCollectorGeoDataCache.CONTENT_URI, _id);
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
            case TERM_COLLECTOR_TERMS:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(contentMapTerms);
                break;
            case TERM_COLLECTOR_GEODATA:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(contentMapGeodata);
                break;
            case TERM_COLLECTOR_GEODATA_CACHE:
                qb.setTables(DATABASE_TABLES[2]);
                qb.setProjectionMap(contentMapGeodataCache);
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
            case TERM_COLLECTOR_TERMS:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            case TERM_COLLECTOR_GEODATA:
                count = database.update(DATABASE_TABLES[1], values, selection, selectionArgs);
                break;
            case TERM_COLLECTOR_GEODATA_CACHE:
                count = database.update(DATABASE_TABLES[2], values, selection, selectionArgs);
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