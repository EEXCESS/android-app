package com.aware.plugin.osmpoi_resolver;

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
 * ContentProvider for the NotificationCatcher
 * @author Christian Koehler
 * @email: ckoehler@andrew.cmu.edu
 * @since: 29 May 2013
 */

public class OSMPoiResolver_Provider extends ContentProvider {

		private final String TAG = ContextophelesConstants.TAG_OSMPOI_RESOLVER + " Provider";
        public static final String AUTHORITY = ContextophelesConstants.OSMPOI_RESOLVER_AUTHORITY;
        public static final String MAIN_TABLE = ContextophelesConstants.OSMPOI_RESOLVER_MAIN_TABLE;
        
        private static final int DATABASE_VERSION = 3;
        
        private static final int OSMPOI_RESOLVER = 1;
        private static final int OSMPOI_RESOLVER_ID = 2;
        
        private static UriMatcher uriMatcher = null;
        private static HashMap<String, String> contentMap = null;        
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;        
        
        public static final class OSMPoiResolver implements BaseColumns {
                private OSMPoiResolver() {};
                
                public static final Uri    CONTENT_URI = ContextophelesConstants.OSMPOI_RESOLVER_CONTENT_URI;
                public static final String CONTENT_TYPE = ContextophelesConstants.OSMPOI_RESOLVER_CONTENT_TYPE;
                public static final String CONTENT_ITEM_TYPE = ContextophelesConstants.OSMPOI_RESOLVER_CONTENT_ITEM_TYPE;
                
                public static final String _ID = ContextophelesConstants.OSMPOI_RESOLVER_FIELD_ID;;
                public static final String TIMESTAMP = ContextophelesConstants.OSMPOI_RESOLVER_FIELD_TIMESTAMP;
                public static final String DEVICE_ID = ContextophelesConstants.OSMPOI_RESOLVER_FIELD_DEVICE_ID;
                public static final String NAME = ContextophelesConstants.OSMPOI_RESOLVER_FIELD_NAME;
                public static final String TYPE = ContextophelesConstants.OSMPOI_RESOLVER_FIELD_TYPE;

                
        }
        
        public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/"+MAIN_TABLE+".db";
        
        public static final String[] DATABASE_TABLES = {
                MAIN_TABLE                
        };
        
        public static final String[] TABLES_FIELDS = {
        			OSMPoiResolver._ID + " integer primary key autoincrement," +
        			OSMPoiResolver.TIMESTAMP + " real default 0," + 
        			OSMPoiResolver.DEVICE_ID + " text default ''," +
        			OSMPoiResolver.NAME + " text default ''," +
                    OSMPoiResolver.TYPE + " text default ''," +
                   "UNIQUE ("+OSMPoiResolver.TIMESTAMP+","+OSMPoiResolver.DEVICE_ID+","+ OSMPoiResolver.NAME + ")"
        };
        
        static {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], OSMPOI_RESOLVER);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", OSMPOI_RESOLVER_ID);
                
                contentMap = new HashMap<String, String>();
                contentMap.put(OSMPoiResolver._ID, OSMPoiResolver._ID);
                contentMap.put(OSMPoiResolver.TIMESTAMP, OSMPoiResolver.TIMESTAMP);
                contentMap.put(OSMPoiResolver.DEVICE_ID, OSMPoiResolver.DEVICE_ID);
                contentMap.put(OSMPoiResolver.NAME, OSMPoiResolver.NAME);
                contentMap.put(OSMPoiResolver.TYPE, OSMPoiResolver.TYPE);
        }
        
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case OSMPOI_RESOLVER:
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
                case OSMPOI_RESOLVER:
                    return OSMPoiResolver.CONTENT_TYPE;
                case OSMPOI_RESOLVER_ID:
                    return OSMPoiResolver.CONTENT_ITEM_TYPE;                
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
	        
	        switch(uriMatcher.match(uri)) {
	            case OSMPOI_RESOLVER:
	                long _id = database.insert(DATABASE_TABLES[0], OSMPoiResolver.TIMESTAMP, values);

                    if (_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(OSMPoiResolver.CONTENT_URI, _id);
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
	            case OSMPOI_RESOLVER:
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
	            case OSMPOI_RESOLVER:
	                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
	                break;            
	            default:
	                database.close();
	                throw new IllegalArgumentException("Unknown URI " + uri);
	        }

	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
        }
}