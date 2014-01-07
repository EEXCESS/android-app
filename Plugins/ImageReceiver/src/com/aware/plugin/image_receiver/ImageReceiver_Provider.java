package com.aware.plugin.image_receiver;

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

public class ImageReceiver_Provider extends ContentProvider {

		private final String TAG = "ImageReceiver Provider";
        public static final String PLUGIN_NAME = "plugin.image_receiver";
        public static final String AUTHORITY = "com.aware.provider."+PLUGIN_NAME;
        public static final String MAIN_TABLE = "plugin_image_receiver";
        
        
        private static final int DATABASE_VERSION = 2;
        
        private static final int IMAGE_RECEIVER = 1;
        private static final int IMAGE_RECEIVER_ID = 2;
        
        private static UriMatcher uriMatcher = null;
        private static HashMap<String, String> contentMap = null;        
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;        
        
        public static final class ImageReceiver implements BaseColumns {
                private ImageReceiver() {};
                
                public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+MAIN_TABLE); //this needs to match the table name
                public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware."+PLUGIN_NAME;
                public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware."+PLUGIN_NAME;
                
                public static final String _ID = "_id";
                public static final String TIMESTAMP = "timestamp";
                public static final String DEVICE_ID = "device_id";
                public static final String _DATA = "_data";
                public static final String _DISPLAY_NAME = "_display_name";
                public static final String _SIZE = "_size";
                public static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
                public static final String BUCKET_ID = "bucket_id";
                public static final String DATE_TAKEN = "date_taken";
                public static final String DATE_ADDED = "date_added";
                public static final String DATE_MODIFIED = "date_modified";
                public static final String DESCRIPTION = "description";
                public static final String HEIGHT = "height";
                public static final String ISPRIVATE = "isprivate";
                public static final String LATITUDE = "latitude";
                public static final String LONGITUDE = "longitude";
                public static final String MIME_TYPE = "mime_type";
                public static final String MINI_THUMB_MAGIC = "mini_thumb_magic";
                public static final String ORIENTATION = "orientation";
                public static final String PICASA_ID = "picasa_id";
                public static final String TITLE = "title";
                public static final String WIDTH = "width";
                
        }
        
        public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/"+MAIN_TABLE+".db";
        
        public static final String[] DATABASE_TABLES = {
                MAIN_TABLE                
        };
        
        public static final String[] TABLES_FIELDS = {
        			ImageReceiver._ID + " integer primary key autoincrement," +
        			ImageReceiver.TIMESTAMP + " real default 0," + 
        			ImageReceiver.DEVICE_ID + " text default ''," +
        			ImageReceiver._DATA + " text default ''," +
        			ImageReceiver._SIZE + " real default 0," + 
        			ImageReceiver._DISPLAY_NAME + " text default ''," +
        			ImageReceiver.MIME_TYPE + " text default ''," +
        			ImageReceiver.TITLE + " text default ''," +
        			ImageReceiver.DATE_TAKEN + " real default 0," + 
        			ImageReceiver.DATE_ADDED + " real default 0," + 
        			ImageReceiver.DATE_MODIFIED + " real default 0," + 
        			ImageReceiver.DESCRIPTION + " text default ''," +
        			ImageReceiver.PICASA_ID + " real default 0," + 
        			ImageReceiver.ISPRIVATE + " real default 0," + 
        			ImageReceiver.LATITUDE + " text default ''," +
        			ImageReceiver.LONGITUDE + " text default ''," +
        			ImageReceiver.ORIENTATION + " real default 0," + 
        			ImageReceiver.MINI_THUMB_MAGIC + " text default ''," +
        			ImageReceiver.BUCKET_ID + " real default 0," + 
        			ImageReceiver.BUCKET_DISPLAY_NAME + " text default ''," +
        			ImageReceiver.WIDTH + " real default 0," + 
        			ImageReceiver.HEIGHT + " real default 0," +
                   "UNIQUE ("+ImageReceiver.TIMESTAMP+","+ImageReceiver.DEVICE_ID+")"
        };
        
        static {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], IMAGE_RECEIVER);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", IMAGE_RECEIVER_ID);
                
                contentMap = new HashMap<String, String>();
                contentMap.put(ImageReceiver._ID, ImageReceiver._ID);
                contentMap.put(ImageReceiver.TIMESTAMP, ImageReceiver.TIMESTAMP);
                contentMap.put(ImageReceiver.DEVICE_ID, ImageReceiver.DEVICE_ID);
                contentMap.put(ImageReceiver._DATA, ImageReceiver._DATA);
                contentMap.put(ImageReceiver._DISPLAY_NAME, ImageReceiver._DISPLAY_NAME);
                contentMap.put(ImageReceiver._SIZE, ImageReceiver._SIZE);
                contentMap.put(ImageReceiver.BUCKET_DISPLAY_NAME, ImageReceiver.BUCKET_DISPLAY_NAME);
                contentMap.put(ImageReceiver.BUCKET_ID, ImageReceiver.BUCKET_ID);
                contentMap.put(ImageReceiver.DATE_TAKEN, ImageReceiver.DATE_TAKEN);
                contentMap.put(ImageReceiver.DATE_ADDED, ImageReceiver.DATE_ADDED);
                contentMap.put(ImageReceiver.DATE_MODIFIED, ImageReceiver.DATE_MODIFIED);
                contentMap.put(ImageReceiver.DESCRIPTION, ImageReceiver.DESCRIPTION);
                contentMap.put(ImageReceiver.HEIGHT, ImageReceiver.HEIGHT);
                contentMap.put(ImageReceiver.ISPRIVATE, ImageReceiver.ISPRIVATE);
                contentMap.put(ImageReceiver.LATITUDE, ImageReceiver.LATITUDE);
                contentMap.put(ImageReceiver.LONGITUDE, ImageReceiver.LONGITUDE);
                contentMap.put(ImageReceiver.MIME_TYPE, ImageReceiver.MIME_TYPE);
                contentMap.put(ImageReceiver.MINI_THUMB_MAGIC, ImageReceiver.MINI_THUMB_MAGIC);
                contentMap.put(ImageReceiver.ORIENTATION, ImageReceiver.ORIENTATION);
                contentMap.put(ImageReceiver.PICASA_ID, ImageReceiver.PICASA_ID);
                contentMap.put(ImageReceiver.TITLE, ImageReceiver.TITLE);
                contentMap.put(ImageReceiver.WIDTH, ImageReceiver.WIDTH);                
        }
        
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case IMAGE_RECEIVER:
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
                case IMAGE_RECEIVER:
                    return ImageReceiver.CONTENT_TYPE;
                case IMAGE_RECEIVER_ID:
                    return ImageReceiver.CONTENT_ITEM_TYPE;                
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
	        
	        switch(uriMatcher.match(uri)) {
	            case IMAGE_RECEIVER:
	                long _id = database.insert(DATABASE_TABLES[0], ImageReceiver.TIMESTAMP, values);
	                if (_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(ImageReceiver.CONTENT_URI, _id);
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
	            case IMAGE_RECEIVER:
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
	            case IMAGE_RECEIVER:
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