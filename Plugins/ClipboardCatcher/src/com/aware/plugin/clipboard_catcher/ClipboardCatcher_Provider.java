package com.aware.plugin.clipboard_catcher;

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
// */

public class ClipboardCatcher_Provider extends ContentProvider {

		private final String TAG = "ClipboardCatcher Provider";
        public static final String PLUGIN_NAME = "plugin.clipboard_catcher";
        public static final String AUTHORITY = "com.aware.provider." + PLUGIN_NAME;
        public static final String MAIN_TABLE = "plugin_clipboard_catcher";


        private static final int DATABASE_VERSION = 3;
        
        private static final int CLIPBOARD_CATCHER = 1;
        private static final int CLIPBOARD_CATCHER_ID = 2;
        
        private static UriMatcher uriMatcher = null;
        private static HashMap<String, String> contentMap = null;        
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;        
        
        public static final class ClipboardCatcher implements BaseColumns {
                private ClipboardCatcher() {};

                public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+ MAIN_TABLE); //this needs to match the table name
                public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware."+PLUGIN_NAME;
                public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware."+PLUGIN_NAME;

                public static final String _ID = "_id";
                public static final String TIMESTAMP = "timestamp";
                public static final String DEVICE_ID = "device_id";
                /**
                 * Content of Clipboard
                 */
                public static final String CLIPBOARDCONTENT = "CLIPBOARDCONTENT";
                
        }
        
        public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/" + MAIN_TABLE + ".db";
        
        public static final String[] DATABASE_TABLES = {
                MAIN_TABLE
        };
        
        public static final String[] TABLES_FIELDS = {
        		ClipboardCatcher._ID + " integer primary key autoincrement," +
        		ClipboardCatcher.TIMESTAMP + " real default 0," + 
        		ClipboardCatcher.DEVICE_ID + " text default ''," +
        		ClipboardCatcher.CLIPBOARDCONTENT + " text default ''," +
                "UNIQUE ("+ClipboardCatcher.TIMESTAMP+","+ClipboardCatcher.DEVICE_ID+","+ ClipboardCatcher.CLIPBOARDCONTENT + ")"
        };
        
        static {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], CLIPBOARD_CATCHER);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", CLIPBOARD_CATCHER_ID);
                
                contentMap = new HashMap<String, String>();
                contentMap.put(ClipboardCatcher._ID, ClipboardCatcher._ID);
                contentMap.put(ClipboardCatcher.TIMESTAMP, ClipboardCatcher.TIMESTAMP);
                contentMap.put(ClipboardCatcher.DEVICE_ID, ClipboardCatcher.DEVICE_ID);
                contentMap.put(ClipboardCatcher.CLIPBOARDCONTENT, ClipboardCatcher.CLIPBOARDCONTENT);                
        }
        
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case CLIPBOARD_CATCHER:
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
                case CLIPBOARD_CATCHER:
                    return ClipboardCatcher.CONTENT_TYPE;
                case CLIPBOARD_CATCHER_ID:
                    return ClipboardCatcher.CONTENT_ITEM_TYPE;                
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
	        
	        switch(uriMatcher.match(uri)) {
	            case CLIPBOARD_CATCHER:
	                long _id = database.insert(DATABASE_TABLES[0], ClipboardCatcher.TIMESTAMP, values);
	                if (_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(ClipboardCatcher.CONTENT_URI, _id);
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
	            case CLIPBOARD_CATCHER:
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
	            case CLIPBOARD_CATCHER:
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