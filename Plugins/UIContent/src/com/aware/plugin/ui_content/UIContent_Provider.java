package com.aware.plugin.ui_content;

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
 * ContentProvider for the UIContent
 * @author Christian Koehler
 * @email: ckoehler@andrew.cmu.edu
 * @since: 29 May 2013
 */

public class UIContent_Provider extends ContentProvider {

        public static final String AUTHORITY = "com.aware.provider.plugin.ui_content";
        
        private static final int DATABASE_VERSION = 4;
        
        private static final int UICONTENT = 1;
        private static final int UICONTENT_ID = 2;

        private static UriMatcher uriMatcher = null;
        private static HashMap<String, String> uicontentMap = null;
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;        
        
        public static final class UIContents implements BaseColumns {
            private UIContents() {};
            
            public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_ui_content"); //this needs to match the table name
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.ui_content";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.ui_content";
            
            public static final String _ID = "_id";
            public static final String TIMESTAMP = "timestamp";
            public static final String DEVICE_ID = "device_id";
            public static final String SOURCE_APP = "source_app";
            public static final String TEXT = "content_text";
        }

        
        public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_ui_content.db";
        
        public static final String[] DATABASE_TABLES = {
                "plugin_ui_content"
        };
        
        public static final String[] TABLES_FIELDS = {
        		UIContents._ID + " integer primary key autoincrement," +
        		UIContents.TIMESTAMP + " real default 0," +
        		UIContents.DEVICE_ID + " text default ''," +
        		UIContents.SOURCE_APP + " text default ''," +
                UIContents.TEXT + " text default ''," +
                "UNIQUE ("+ UIContents.TIMESTAMP+","+ UIContents.DEVICE_ID+")",
        };
        
        static {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], UICONTENT);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", UICONTENT_ID);
                
                uicontentMap = new HashMap<String, String>();
                uicontentMap.put(UIContents._ID, UIContents._ID);
                uicontentMap.put(UIContents.TIMESTAMP, UIContents.TIMESTAMP);
                uicontentMap.put(UIContents.DEVICE_ID, UIContents.DEVICE_ID);
                uicontentMap.put(UIContents.SOURCE_APP, UIContents.SOURCE_APP);
                uicontentMap.put(UIContents.TEXT, UIContents.TEXT);
        }
        
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case UICONTENT:
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
                case UICONTENT:
                    return UIContents.CONTENT_TYPE;
                case UICONTENT_ID:
                    return UIContents.CONTENT_ITEM_TYPE;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
	        
	        switch(uriMatcher.match(uri)) {
	            case UICONTENT:
	                long _id = database.insert(DATABASE_TABLES[0], UIContents.TIMESTAMP, values);
	                if (_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(UIContents.CONTENT_URI, _id);
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
	            case UICONTENT:
	                qb.setTables(DATABASE_TABLES[0]);
	                qb.setProjectionMap(uicontentMap);
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
	            case UICONTENT:
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