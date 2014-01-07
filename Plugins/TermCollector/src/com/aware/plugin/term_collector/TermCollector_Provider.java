package com.aware.plugin.term_collector;

import java.util.HashMap;

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

/**
 * ContentProvider for the Term Collector
 */

public class TermCollector_Provider extends ContentProvider {

		private final String TAG = "TermCollector Provider";
        public static final String AUTHORITY = "com.aware.provider.plugin.term_collector";
        
        private static final int DATABASE_VERSION = 3;
        
        private static final int TERM_COLLECTOR = 1;
        private static final int TERM_COLLECTOR_ID = 2;
        
        private static UriMatcher uriMatcher = null;
        private static HashMap<String, String> contentMap = null;        
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;        
        
        public static final class TermCollector implements BaseColumns {
                private TermCollector() {};
                
                public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_term_collector"); //this needs to match the table name
                public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.term_collector";
                public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.term_collector";
                
                public static final String _ID = "_id";
                public static final String TIMESTAMP = "timestamp";
                public static final String DEVICE_ID = "device_id";
                
                /**
                 * Content of Term
                 */
                public static final String TERM_CONTENT = "term_content";
                
                /**
                 * Source of Term
                 */
                public static final String TERM_SOURCE = "term_source";
        }
        
        public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_term_collector.db";
        
        public static final String[] DATABASE_TABLES = {
                "plugin_term_collector"                
        };
        
        public static final String[] TABLES_FIELDS = {
        		TermCollector._ID + " integer primary key autoincrement," +
                TermCollector.TIMESTAMP + " real default 0," + 
                TermCollector.DEVICE_ID + " text default ''," +
                TermCollector.TERM_CONTENT + " text default ''," +
                TermCollector.TERM_SOURCE + " text default ''," +
                "UNIQUE ("+TermCollector.TIMESTAMP+","+TermCollector.DEVICE_ID+ "," + TermCollector.TERM_CONTENT + ")"
        };
        
        static {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], TERM_COLLECTOR);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", TERM_COLLECTOR_ID);
                
                contentMap = new HashMap<String, String>();
                contentMap.put(TermCollector._ID, TermCollector._ID);
                contentMap.put(TermCollector.TIMESTAMP, TermCollector.TIMESTAMP);
                contentMap.put(TermCollector.DEVICE_ID, TermCollector.DEVICE_ID);
                contentMap.put(TermCollector.TERM_CONTENT, TermCollector.TERM_CONTENT);
                contentMap.put(TermCollector.TERM_SOURCE, TermCollector.TERM_SOURCE);
        }
        
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case TERM_COLLECTOR:
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
                case TERM_COLLECTOR:
                    return TermCollector.CONTENT_TYPE;
                case TERM_COLLECTOR_ID:
                    return TermCollector.CONTENT_ITEM_TYPE;                
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
	        
	        switch(uriMatcher.match(uri)) {
	            case TERM_COLLECTOR:
	                long _id = database.insert(DATABASE_TABLES[0], TermCollector.TIMESTAMP, values);
	                if (_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(TermCollector.CONTENT_URI, _id);
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
            if( databaseHelper == null ) databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
	        database = databaseHelper.getWritableDatabase();
	        return (databaseHelper != null);
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
                
	        if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
	        
	        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	        switch (uriMatcher.match(uri)) {
	            case TERM_COLLECTOR:
	                qb.setTables(DATABASE_TABLES[0]);
	                qb.setProjectionMap(contentMap);
	                break;            
	            default:
	                throw new IllegalArgumentException("Unknown URI " + uri);
	        }
	        try {
	            Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
	            c.setNotificationUri(getContext().getContentResolver(), uri);
	            return c;
	        }catch ( IllegalStateException e ) {
	            if ( Aware.DEBUG ) Log.e(Aware.TAG,e.getMessage());
	            return null;
	        }
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection,
                        String[] selectionArgs) {
                
	                if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
	        
	                int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case TERM_COLLECTOR:
	                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
	                break;            
	            default:
	                database.close();
	                throw new IllegalArgumentException("Unknown URI " + uri);
	        }
	        Log.d(TAG,"Notifying about change");
	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
        }
}