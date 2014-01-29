package com.aware.plugin.browser_history;

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
import com.aware.providers.Screen_Provider.Screen_Data;
import com.aware.utils.DatabaseHelper;

public class Browser_History_Provider extends ContentProvider {

	public static final int DATABASE_VERSION = 2;

	/**
	 * Authority
	 */
	public static final String AUTHORITY = "com.aware.provider.plugin.browser_history";

	// ContentProvider query paths
	private static final int BROWSER = 1;
	private static final int BROWSER_ID = 2;


	private static UriMatcher sUriMatcher = null;
	private static HashMap<String, String> browserProjectionMap = null;
	private static DatabaseHelper databaseHelper = null;
	private static SQLiteDatabase database = null;

	public static final class Browser_History_Data implements BaseColumns {
		private Browser_History_Data() {
		};

		public static final Uri CONTENT_URI = Uri.parse("content://"+ Browser_History_Provider.AUTHORITY + "/plugin_browser_history");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.browser_history";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.browser_history";

		public static final String _ID = "_id";
		public static final String TIMESTAMP = "timestamp";
		public static final String DEVICE_ID = "device_id";
		public static final String BROWSER_TITLE = "browser_title";
		public static final String BROWSER_URL = "browser_url";
		public static final String BROWSER_VISITED_TIME = "browser_visited_time";
	}

	public static String DATABASE_NAME = Environment.getExternalStorageDirectory()+"/AWARE/plugin_browser_history.db";
	public static final String[] DATABASE_TABLES = {
		"plugin_browser_history"
	};

	public static final String[] TABLES_FIELDS = {
		// browser history
		Browser_History_Data._ID + " integer primary key autoincrement,"
		+ Browser_History_Data.TIMESTAMP + " real default 0,"
		+ Browser_History_Data.DEVICE_ID + " text default '',"
		+ Browser_History_Data.BROWSER_TITLE + " text default '',"
		+ Browser_History_Data.BROWSER_URL + " text default '',"
		+ Browser_History_Data.BROWSER_VISITED_TIME + " text default '',"
		+ "UNIQUE("
		+ Browser_History_Data.TIMESTAMP + "," + Browser_History_Data.DEVICE_ID + ")" 
	};


	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Browser_History_Provider.AUTHORITY, DATABASE_TABLES[0], BROWSER);
		sUriMatcher.addURI(Browser_History_Provider.AUTHORITY, DATABASE_TABLES[0] + "/#", BROWSER_ID);

		browserProjectionMap = new HashMap<String, String>();
		browserProjectionMap.put(Browser_History_Data._ID, Browser_History_Data._ID);
		browserProjectionMap.put(Browser_History_Data.TIMESTAMP, Browser_History_Data.TIMESTAMP);
		browserProjectionMap.put(Browser_History_Data.DEVICE_ID, Browser_History_Data.DEVICE_ID);
		browserProjectionMap.put(Browser_History_Data.BROWSER_TITLE, Browser_History_Data.BROWSER_TITLE);
		browserProjectionMap.put(Browser_History_Data.BROWSER_URL, Browser_History_Data.BROWSER_URL);
		browserProjectionMap.put(Browser_History_Data.BROWSER_VISITED_TIME, Browser_History_Data.BROWSER_VISITED_TIME);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (database == null || !database.isOpen())
			database = databaseHelper.getWritableDatabase();

		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case BROWSER:
			count = database.delete(DATABASE_TABLES[0], selection,
					selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case BROWSER:
			return Browser_History_Data.CONTENT_TYPE;
		case BROWSER_ID:
			return Screen_Data.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}


	/**
	 * Insert entry to the database
	 */
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (database == null || !database.isOpen())
			database = databaseHelper.getWritableDatabase();

		ContentValues values = (initialValues != null) ? new ContentValues(
				initialValues) : new ContentValues();

				switch (sUriMatcher.match(uri)) {
				case BROWSER:
					long screen_id = database.insert(DATABASE_TABLES[0],
							Browser_History_Data.DEVICE_ID, values);

					if (screen_id > 0) {
						Uri screenUri = ContentUris.withAppendedId(
								Browser_History_Data.CONTENT_URI, screen_id);
						getContext().getContentResolver().notifyChange(screenUri, null);
						return screenUri;
					}
					throw new SQLException("Failed to insert row into " + uri);
				default:

					throw new IllegalArgumentException("Unknown URI " + uri);
				}
	}

	@Override
	public boolean onCreate() {
		if (databaseHelper == null)
			databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME,
					null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
		database = databaseHelper.getWritableDatabase();
		return (databaseHelper != null);
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (database == null || !database.isOpen())
			database = databaseHelper.getWritableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		System.out.println(uri);
		switch (sUriMatcher.match(uri)) {
		case BROWSER:
			qb.setTables(DATABASE_TABLES[0]);
			qb.setProjectionMap(browserProjectionMap);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		try {
			Cursor c = qb.query(database, projection, selection, selectionArgs,
					null, null, sortOrder);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		} catch (IllegalStateException e) {
			if (Aware.DEBUG)
				Log.e(Aware.TAG, e.getMessage());

			return null;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (database == null || !database.isOpen())
			database = databaseHelper.getWritableDatabase();
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case BROWSER:
			count = database.update(DATABASE_TABLES[0], values, selection,
					selectionArgs);
			break;
		default:

			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
