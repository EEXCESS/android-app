package com.aware.plugin.notification_catcher;

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

public class NotificationCatcher_Provider extends ContentProvider {

        public static final String AUTHORITY = "com.aware.provider.plugin.notification_catcher";
        
        private static final int DATABASE_VERSION = 3;
        
        private static final int NOTIFICATION = 1;
        private static final int NOTIFICATION_ID = 2;
        private static final int CONTACT = 3;
        private static final int CONTACT_ID = 4;
        
        private static UriMatcher uriMatcher = null;
        private static HashMap<String, String> notificationMap = null;
        private static HashMap<String, String> contactMap = null; 
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;        
        
        public static final class Notifications implements BaseColumns {
            private Notifications() {};
            
            public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_notification_catcher"); //this needs to match the table name
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.notification_catcher";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.notification_catcher";
            
            public static final String _ID = "_id";
            public static final String TIMESTAMP = "timestamp";
            public static final String DEVICE_ID = "device_id";
            public static final String TITLE = "title";
            public static final String TEXT = "content_text";
            public static final String CONTACT_ID = "contact_id";
            public static final String APP_NAME = "app_name";
        }
        
        public static final class Hashed_Contacts implements BaseColumns {
        	public Hashed_Contacts() {}
        	
        	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_notification_catcher_contacts"); //this needs to match the table name
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.notification_catcher.contacts";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.notification_catcher.contacts";
            
            public static final String _ID = "_id";
            public static final String CONTACT_HASH = "contact_hash";
            public static final String CONTACT_ALT_HASH = "contact_alt_hash";
            public static final String TIMESTAMP = "timestamp";
        }
        
        public static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_notification_catcher.db";
        
        public static final String[] DATABASE_TABLES = {
                "plugin_notification_catcher",
                "plugin_notification_catcher_contacts"
        };
        
        public static final String[] TABLES_FIELDS = {
        		Notifications._ID + " integer primary key autoincrement," +
        		Notifications.TIMESTAMP + " real default 0," + 
        		Notifications.DEVICE_ID + " text default ''," +
        		Notifications.TITLE + " text default ''," +
                Notifications.TEXT + " text default ''," +
        		Notifications.APP_NAME + " text default ''," +
                Notifications.CONTACT_ID + " real default 0," +
                "UNIQUE ("+Notifications.TIMESTAMP+","+Notifications.DEVICE_ID+")",
                
                Hashed_Contacts._ID + " integer primary key autoincrement," +
                Hashed_Contacts.TIMESTAMP + " real default 0," +
                Hashed_Contacts.CONTACT_HASH + " text default ''," +
                Hashed_Contacts.CONTACT_ALT_HASH + " text default ''," +
                "UNIQUE (" + Hashed_Contacts.CONTACT_HASH + ")"
        };
        
        static {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], NOTIFICATION);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", NOTIFICATION_ID);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1], CONTACT);
                uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1]+"/#", CONTACT_ID);
                
                notificationMap = new HashMap<String, String>();
                notificationMap.put(Notifications._ID, Notifications._ID);
                notificationMap.put(Notifications.TIMESTAMP, Notifications.TIMESTAMP);
                notificationMap.put(Notifications.DEVICE_ID, Notifications.DEVICE_ID);
                notificationMap.put(Notifications.TITLE, Notifications.TITLE);
                notificationMap.put(Notifications.TEXT, Notifications.TEXT);
                notificationMap.put(Notifications.APP_NAME, Notifications.APP_NAME);
                notificationMap.put(Notifications.CONTACT_ID, Notifications.CONTACT_ID);
                
                contactMap = new HashMap<String, String>();
                contactMap.put(Hashed_Contacts.TIMESTAMP, Hashed_Contacts.TIMESTAMP);
                contactMap.put(Hashed_Contacts._ID, Hashed_Contacts._ID);                
                contactMap.put(Hashed_Contacts.CONTACT_HASH, Hashed_Contacts.CONTACT_HASH);
                contactMap.put(Hashed_Contacts.CONTACT_ALT_HASH, Hashed_Contacts.CONTACT_ALT_HASH);
        }
        
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        int count = 0;
	        switch (uriMatcher.match(uri)) {
	            case NOTIFICATION:
	                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
	                break;
	            case CONTACT:
	            	count = database.delete(DATABASE_TABLES[1], selection, selectionArgs);
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
                case NOTIFICATION:
                    return Notifications.CONTENT_TYPE;
                case NOTIFICATION_ID:
                    return Notifications.CONTENT_ITEM_TYPE;
                case CONTACT:
                	return Hashed_Contacts.CONTENT_TYPE;
                case CONTACT_ID:
                	return Hashed_Contacts.CONTENT_ITEM_TYPE;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
            if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
	        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
	        
	        switch(uriMatcher.match(uri)) {
	            case NOTIFICATION:
	                long _id = database.insert(DATABASE_TABLES[0], Notifications.TIMESTAMP, values);
	                if (_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(Notifications.CONTENT_URI, _id);
	                    getContext().getContentResolver().notifyChange(dataUri, null);
	                    return dataUri;
	                }
	                throw new SQLException("Failed to insert row into " + uri);
	            case CONTACT:
	                long contact_id = database.insert(DATABASE_TABLES[1], Hashed_Contacts.TIMESTAMP, values);
	                if (contact_id > 0) {
	                    Uri dataUri = ContentUris.withAppendedId(Hashed_Contacts.CONTENT_URI, contact_id);
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
	            case NOTIFICATION:
	                qb.setTables(DATABASE_TABLES[0]);
	                qb.setProjectionMap(notificationMap);
	                break;
	            case CONTACT:
	                qb.setTables(DATABASE_TABLES[1]);
	                qb.setProjectionMap(contactMap);
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
	            case NOTIFICATION:
	                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
	                break;
	            case CONTACT:
	                count = database.update(DATABASE_TABLES[1], values, selection, selectionArgs);
	                break;
	            default:
	                database.close();
	                throw new IllegalArgumentException("Unknown URI " + uri);
	        }
	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
        }
}