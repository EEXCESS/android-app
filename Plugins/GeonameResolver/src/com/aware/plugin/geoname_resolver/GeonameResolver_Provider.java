package com.aware.plugin.geoname_resolver;

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


public class GeonameResolver_Provider extends ContentProvider {

		private final String TAG = ContextophelesConstants.TAG_GEONAME_RESOLVER + " Provider";
        public static final String AUTHORITY = ContextophelesConstants.GEONAME_RESOLVER_AUTHORITY;
        public static final String MAIN_TABLE = ContextophelesConstants.GEONAME_RESOLVER_MAIN_TABLE;
        
        private static final int DATABASE_VERSION = 4;
        
        private static final int GEONAME_RESOLVER = 1;
        private static final int GEONAME_RESOLVER_ID = 2;
        
        private static UriMatcher uriMatcher = null;
        private static HashMap<String, String> contentMap = null;        
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;        
        
        public static final class GeonameResolver implements BaseColumns {
                private GeonameResolver() {}
                
                public static final Uri CONTENT_URI = ContextophelesConstants.GEONAME_RESOLVER_CONTENT_URI;
                public static final String CONTENT_TYPE = ContextophelesConstants.GEONAME_RESOLVER_CONTENT_TYPE;
                public static final String CONTENT_ITEM_TYPE = ContextophelesConstants.GEONAME_RESOLVER_CONTENT_ITEM_TYPE;
                
                public static final String _ID =        ContextophelesConstants.GEONAME_RESOLVER_FIELD_ID;
                public static final String TIMESTAMP =  ContextophelesConstants.GEONAME_RESOLVER_FIELD_TIMESTAMP;
                public static final String DEVICE_ID =  ContextophelesConstants.GEONAME_RESOLVER_FIELD_DEVICE_ID;
                public static final String NAME =       ContextophelesConstants.GEONAME_RESOLVER_FIELD_NAME;
        }
        
        public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/"+MAIN_TABLE+".db";
        
        public static final String[] DATABASE_TABLES = {
                MAIN_TABLE                
        };
        
        public static final String[] TABLES_FIELDS = {
        			GeonameResolver._ID + " integer primary key autoincrement," +
        			GeonameResolver.TIMESTAMP + " real default 0," + 
        			GeonameResolver.DEVICE_ID + " text default ''," +
        			GeonameResolver.NAME + " text default ''," +
                   "UNIQUE ("+GeonameResolver.TIMESTAMP+","+GeonameResolver.DEVICE_ID+","+ GeonameResolver.NAME + ")"
        };
        
        static {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], GEONAME_RESOLVER);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", GEONAME_RESOLVER_ID);
                
                contentMap = new HashMap<String, String>();
                contentMap.put(GeonameResolver._ID, GeonameResolver._ID);
                contentMap.put(GeonameResolver.TIMESTAMP, GeonameResolver.TIMESTAMP);
                contentMap.put(GeonameResolver.DEVICE_ID, GeonameResolver.DEVICE_ID);
                contentMap.put(GeonameResolver.NAME, GeonameResolver.NAME);
        }
        
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case GEONAME_RESOLVER:
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
                case GEONAME_RESOLVER:
                    return GeonameResolver.CONTENT_TYPE;
                case GEONAME_RESOLVER_ID:
                    return GeonameResolver.CONTENT_ITEM_TYPE;                
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
	        
	        switch(uriMatcher.match(uri)) {
	            case GEONAME_RESOLVER:
	                long _id = database.insert(DATABASE_TABLES[0], GeonameResolver.TIMESTAMP, values);

                    if (_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(GeonameResolver.CONTENT_URI, _id);
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
	            case GEONAME_RESOLVER:
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
	            case GEONAME_RESOLVER:
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