package com.aware.plugin.geoname_dissolver;

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
 * ContentProvider for the NotificationCatcher
 * @author Christian Koehler
 * @email: ckoehler@andrew.cmu.edu
 * @since: 29 May 2013
 */

public class GeonameDissolver_Provider extends ContentProvider {

		private final String TAG = "GeonameDissolver Provider";
        public static final String PLUGIN_NAME = "plugin.geoname_dissolver";
        public static final String AUTHORITY = "com.aware.provider."+PLUGIN_NAME;
        public static final String MAIN_TABLE = "plugin_geoname_dissolver";
        
        private static final int DATABASE_VERSION = 3;
        
        private static final int GEONAME_DISSOLVER = 1;
        private static final int GEONAME_DISSOLVER_ID = 2;
        
        private static UriMatcher uriMatcher = null;
        private static HashMap<String, String> contentMap = null;        
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;        
        
        public static final class GeonameDissolver implements BaseColumns {
                private GeonameDissolver() {};
                
                public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+MAIN_TABLE); //this needs to match the table name
                public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware."+PLUGIN_NAME;
                public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware."+PLUGIN_NAME;
                
                public static final String _ID = "_id";
                public static final String TIMESTAMP = "timestamp";
                public static final String DEVICE_ID = "device_id";
                public static final String NAME = "name";
                public static final String TYPE = "type";

                
        }
        
        public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/"+MAIN_TABLE+".db";
        
        public static final String[] DATABASE_TABLES = {
                MAIN_TABLE                
        };
        
        public static final String[] TABLES_FIELDS = {
        			GeonameDissolver._ID + " integer primary key autoincrement," +
        			GeonameDissolver.TIMESTAMP + " real default 0," + 
        			GeonameDissolver.DEVICE_ID + " text default ''," +
        			GeonameDissolver.NAME + " text default ''," +
                    GeonameDissolver.TYPE + " text default ''," +
                   "UNIQUE ("+GeonameDissolver.TIMESTAMP+","+GeonameDissolver.DEVICE_ID+","+ GeonameDissolver.NAME + ")"
        };
        
        static {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], GEONAME_DISSOLVER);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", GEONAME_DISSOLVER_ID);
                
                contentMap = new HashMap<String, String>();
                contentMap.put(GeonameDissolver._ID, GeonameDissolver._ID);
                contentMap.put(GeonameDissolver.TIMESTAMP, GeonameDissolver.TIMESTAMP);
                contentMap.put(GeonameDissolver.DEVICE_ID, GeonameDissolver.DEVICE_ID);
                contentMap.put(GeonameDissolver.NAME, GeonameDissolver.NAME);
                contentMap.put(GeonameDissolver.TYPE, GeonameDissolver.TYPE);
        }
        
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case GEONAME_DISSOLVER:
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
                case GEONAME_DISSOLVER:
                    return GeonameDissolver.CONTENT_TYPE;
                case GEONAME_DISSOLVER_ID:
                    return GeonameDissolver.CONTENT_ITEM_TYPE;                
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
	        
	        switch(uriMatcher.match(uri)) {
	            case GEONAME_DISSOLVER:
	                long _id = database.insert(DATABASE_TABLES[0], GeonameDissolver.TIMESTAMP, values);

                    Log.wtf(TAG, "Id:" + _id);

                    if (_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(GeonameDissolver.CONTENT_URI, _id);
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
	            case GEONAME_DISSOLVER:
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
	            case GEONAME_DISSOLVER:
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