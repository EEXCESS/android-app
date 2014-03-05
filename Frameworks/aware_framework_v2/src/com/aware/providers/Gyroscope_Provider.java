/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware.providers;

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
 * AWARE Gyroscope Content Provider Allows you to access all the recorded
 * gyroscope readings on the database Database is located at the SDCard :
 * /AWARE/gyroscope.db
 * 
 * @author denzil
 * 
 */
public class Gyroscope_Provider extends ContentProvider {

	public static final int DATABASE_VERSION = 2;

	/**
	 * Authority of Gyroscope content provider
	 */
	public static final String AUTHORITY = "com.aware.provider.gyroscope";

	// ContentProvider query paths
	private static final int GYRO_DEV = 1;
	private static final int GYRO_DEV_ID = 2;
	private static final int GYRO_DATA = 3;
	private static final int GYRO_DATA_ID = 4;

	/**
	 * Accelerometer device info
	 * 
	 * @author denzil
	 * 
	 */
	public static final class Gyroscope_Sensor implements BaseColumns {
		private Gyroscope_Sensor() {
		};

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ Gyroscope_Provider.AUTHORITY + "/sensor_gyroscope");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.gyroscope.sensor";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.gyroscope.sensor";

		public static final String _ID = "_id";
		public static final String TIMESTAMP = "timestamp";
		public static final String DEVICE_ID = "device_id";
		public static final String MAXIMUM_RANGE = "double_sensor_maximum_range";
		public static final String MINIMUM_DELAY = "double_sensor_minimum_delay";
		public static final String NAME = "sensor_name";
		public static final String POWER_MA = "double_sensor_power_ma";
		public static final String RESOLUTION = "double_sensor_resolution";
		public static final String TYPE = "sensor_type";
		public static final String VENDOR = "sensor_vendor";
		public static final String VERSION = "sensor_version";
	}

	public static final class Gyroscope_Data implements BaseColumns {
		private Gyroscope_Data() {
		};

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ Gyroscope_Provider.AUTHORITY + "/gyroscope");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.gyroscope.data";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.gyroscope.data";

		public static final String _ID = "_id";
		public static final String TIMESTAMP = "timestamp";
		public static final String DEVICE_ID = "device_id";
		public static final String VALUES_0 = "axis_x";
		public static final String VALUES_1 = "axis_y";
		public static final String VALUES_2 = "axis_z";
		public static final String ACCURACY = "accuracy";
		public static final String LABEL = "label";
	}

	public static String DATABASE_NAME = Environment
			.getExternalStorageDirectory() + "/AWARE/gyroscope.db";
	public static final String[] DATABASE_TABLES = { "sensor_gyroscope",
			"gyroscope" };
	public static final String[] TABLES_FIELDS = {
			// gyroscope device information
			Gyroscope_Sensor._ID + " integer primary key autoincrement,"
					+ Gyroscope_Sensor.TIMESTAMP + " real default 0,"
					+ Gyroscope_Sensor.DEVICE_ID + " text default '',"
					+ Gyroscope_Sensor.MAXIMUM_RANGE + " real default 0,"
					+ Gyroscope_Sensor.MINIMUM_DELAY + " real default 0,"
					+ Gyroscope_Sensor.NAME + " text default '',"
					+ Gyroscope_Sensor.POWER_MA + " real default 0,"
					+ Gyroscope_Sensor.RESOLUTION + " real default 0,"
					+ Gyroscope_Sensor.TYPE + " text default '',"
					+ Gyroscope_Sensor.VENDOR + " text default '',"
					+ Gyroscope_Sensor.VERSION + " text default '',"
					+ "UNIQUE(" + Gyroscope_Sensor.TIMESTAMP + ","
					+ Gyroscope_Sensor.DEVICE_ID + ")",
			// gyroscope data
			Gyroscope_Data._ID + " integer primary key autoincrement,"
					+ Gyroscope_Data.TIMESTAMP + " real default 0,"
					+ Gyroscope_Data.DEVICE_ID + " text default '',"
					+ Gyroscope_Data.VALUES_0 + " real default 0,"
					+ Gyroscope_Data.VALUES_1 + " real default 0,"
					+ Gyroscope_Data.VALUES_2 + " real default 0,"
					+ Gyroscope_Data.ACCURACY + " integer default 0,"
					+ Gyroscope_Data.LABEL + " text default ''," + "UNIQUE("
					+ Gyroscope_Data.TIMESTAMP + "," + Gyroscope_Data.DEVICE_ID
					+ ")" };

	private static UriMatcher sUriMatcher = null;
	private static HashMap<String, String> gyroDeviceMap = null;
	private static HashMap<String, String> gyroDataMap = null;
	private static DatabaseHelper databaseHelper = null;
	private static SQLiteDatabase database = null;

	/**
	 * Delete entry from the database
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		if (database == null || !database.isOpen())
			database = databaseHelper.getWritableDatabase();

		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case GYRO_DEV:
			count = database.delete(DATABASE_TABLES[0], selection,
					selectionArgs);
			break;
		case GYRO_DATA:
			count = database.delete(DATABASE_TABLES[1], selection,
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
		case GYRO_DEV:
			return Gyroscope_Sensor.CONTENT_TYPE;
		case GYRO_DEV_ID:
			return Gyroscope_Sensor.CONTENT_ITEM_TYPE;
		case GYRO_DATA:
			return Gyroscope_Data.CONTENT_TYPE;
		case GYRO_DATA_ID:
			return Gyroscope_Data.CONTENT_ITEM_TYPE;
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
		case GYRO_DEV:
			long gyro_id = database.insert(DATABASE_TABLES[0],
					Gyroscope_Sensor.DEVICE_ID, values);

			if (gyro_id > 0) {
				Uri gyroUri = ContentUris.withAppendedId(
						Gyroscope_Sensor.CONTENT_URI, gyro_id);
				getContext().getContentResolver().notifyChange(gyroUri, null);
				return gyroUri;
			}
			throw new SQLException("Failed to insert row into " + uri);
		case GYRO_DATA:
			long gyroData_id = database.insert(DATABASE_TABLES[1],
					Gyroscope_Data.DEVICE_ID, values);

			if (gyroData_id > 0) {
				Uri gyroDataUri = ContentUris.withAppendedId(
						Gyroscope_Data.CONTENT_URI, gyroData_id);
				getContext().getContentResolver().notifyChange(gyroDataUri,
						null);
				return gyroDataUri;
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

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Gyroscope_Provider.AUTHORITY, DATABASE_TABLES[0],
				GYRO_DEV);
		sUriMatcher.addURI(Gyroscope_Provider.AUTHORITY, DATABASE_TABLES[0]
				+ "/#", GYRO_DEV_ID);
		sUriMatcher.addURI(Gyroscope_Provider.AUTHORITY, DATABASE_TABLES[1],
				GYRO_DATA);
		sUriMatcher.addURI(Gyroscope_Provider.AUTHORITY, DATABASE_TABLES[1]
				+ "/#", GYRO_DATA_ID);

		gyroDeviceMap = new HashMap<String, String>();
		gyroDeviceMap.put(Gyroscope_Sensor._ID, Gyroscope_Sensor._ID);
		gyroDeviceMap.put(Gyroscope_Sensor.TIMESTAMP,
				Gyroscope_Sensor.TIMESTAMP);
		gyroDeviceMap.put(Gyroscope_Sensor.DEVICE_ID,
				Gyroscope_Sensor.DEVICE_ID);
		gyroDeviceMap.put(Gyroscope_Sensor.MAXIMUM_RANGE,
				Gyroscope_Sensor.MAXIMUM_RANGE);
		gyroDeviceMap.put(Gyroscope_Sensor.MINIMUM_DELAY,
				Gyroscope_Sensor.MINIMUM_DELAY);
		gyroDeviceMap.put(Gyroscope_Sensor.NAME, Gyroscope_Sensor.NAME);
		gyroDeviceMap.put(Gyroscope_Sensor.POWER_MA, Gyroscope_Sensor.POWER_MA);
		gyroDeviceMap.put(Gyroscope_Sensor.RESOLUTION,
				Gyroscope_Sensor.RESOLUTION);
		gyroDeviceMap.put(Gyroscope_Sensor.TYPE, Gyroscope_Sensor.TYPE);
		gyroDeviceMap.put(Gyroscope_Sensor.VENDOR, Gyroscope_Sensor.VENDOR);
		gyroDeviceMap.put(Gyroscope_Sensor.VERSION, Gyroscope_Sensor.VERSION);

		gyroDataMap = new HashMap<String, String>();
		gyroDataMap.put(Gyroscope_Data._ID, Gyroscope_Data._ID);
		gyroDataMap.put(Gyroscope_Data.TIMESTAMP, Gyroscope_Data.TIMESTAMP);
		gyroDataMap.put(Gyroscope_Data.DEVICE_ID, Gyroscope_Data.DEVICE_ID);
		gyroDataMap.put(Gyroscope_Data.VALUES_0, Gyroscope_Data.VALUES_0);
		gyroDataMap.put(Gyroscope_Data.VALUES_1, Gyroscope_Data.VALUES_1);
		gyroDataMap.put(Gyroscope_Data.VALUES_2, Gyroscope_Data.VALUES_2);
		gyroDataMap.put(Gyroscope_Data.ACCURACY, Gyroscope_Data.ACCURACY);
		gyroDataMap.put(Gyroscope_Data.LABEL, Gyroscope_Data.LABEL);
	}

	/**
	 * Query entries from the database
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		if (database == null || !database.isOpen())
			database = databaseHelper.getWritableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (sUriMatcher.match(uri)) {
		case GYRO_DEV:
			qb.setTables(DATABASE_TABLES[0]);
			qb.setProjectionMap(gyroDeviceMap);
			break;
		case GYRO_DATA:
			qb.setTables(DATABASE_TABLES[1]);
			qb.setProjectionMap(gyroDataMap);
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

	/**
	 * Update application on the database
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		if (database == null || !database.isOpen())
			database = databaseHelper.getWritableDatabase();
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case GYRO_DEV:
			count = database.update(DATABASE_TABLES[0], values, selection,
					selectionArgs);
			break;
		case GYRO_DATA:
			count = database.update(DATABASE_TABLES[1], values, selection,
					selectionArgs);
			break;
		default:

			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}