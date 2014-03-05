package com.aware.plugin.image_receiver;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.image_receiver.ImageReceiver_Provider.ImageReceiver;
import com.aware.utils.Aware_Plugin;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

public class Plugin extends Aware_Plugin {

	private static final String TAG = ContextophelesConstants.TAG_IMAGE_RECEIVER + " Plugin";
	public static final String ACTION_AWARE_IMAGERECEIVER = "ACTION_AWARE_IMAGERECEIVER";
	
	private static long previousTimestamp = 0L;
	
	public static Uri ExternalImageContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	private static ExternalImageContentObserver externalImageObs = null;
	
	public static Uri InternalImageContentUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
	private static InternalImageContentObserver internalImageObs = null;

	
	/**
	 * Thread manager
	 */
	private static HandlerThread threads = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "Plugin Created");
		super.onCreate();

		// Share the context back to the framework and other applications
		CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
			@Override
			public void onContext() {
				Intent notification = new Intent(ACTION_AWARE_IMAGERECEIVER);
				sendBroadcast(notification);
			}
		};

		DATABASE_TABLES = ImageReceiver_Provider.DATABASE_TABLES;
		TABLES_FIELDS = ImageReceiver_Provider.TABLES_FIELDS;
		CONTEXT_URIS = new Uri[] { ImageReceiver.CONTENT_URI };

		threads = new HandlerThread(TAG);
		threads.start();

		// Set the observers, that run in independent threads, for
		// responsiveness
		
		externalImageObs = new ExternalImageContentObserver(new Handler(
				threads.getLooper()));
		getContentResolver().registerContentObserver(
				ExternalImageContentUri, true, externalImageObs);
		Log.d(TAG, "externalImageObs registered");
		
		internalImageObs = new InternalImageContentObserver(new Handler(
				threads.getLooper()));
		getContentResolver().registerContentObserver(
				InternalImageContentUri, true, internalImageObs);
		Log.d(TAG, "internalImageObs registered");
		
		Log.d(TAG, "Plugin Started");
	}

	@Override
	public void onDestroy() {

		Log.d(TAG, "Plugin is destroyed");

		super.onDestroy();
		
		getContentResolver().unregisterContentObserver(externalImageObs);
		getContentResolver().unregisterContentObserver(internalImageObs);
		

	}

	protected void saveData(Cursor cursor) {
		Log.d(TAG, "@saveData(Cursor cursor)");
		
		if (cursor != null && cursor.moveToFirst()) {

			ContentValues rowData = new ContentValues();
			
			rowData.put(ImageReceiver.DEVICE_ID, Aware.getSetting(
					getContentResolver(), Aware_Preferences.DEVICE_ID));
			long timestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)));
			rowData.put(ImageReceiver.TIMESTAMP, timestamp);
			rowData.put(ImageReceiver._DATA, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
			rowData.put(ImageReceiver._DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)));
		
			rowData.put(ImageReceiver._SIZE, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)));
			rowData.put(ImageReceiver.BUCKET_DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)));
		
			rowData.put(ImageReceiver.BUCKET_ID, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)));
		
			rowData.put(ImageReceiver.DATE_TAKEN, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)));
		
			rowData.put(ImageReceiver.DATE_ADDED, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED)));
		
			rowData.put(ImageReceiver.DATE_MODIFIED, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)));
			rowData.put(ImageReceiver.DESCRIPTION, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DESCRIPTION)));
		
			rowData.put(ImageReceiver.HEIGHT, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT)));
			
			rowData.put(ImageReceiver.ISPRIVATE, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.IS_PRIVATE)));
			rowData.put(ImageReceiver.LATITUDE, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE)));
			rowData.put(ImageReceiver.LONGITUDE, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE)));
			rowData.put(ImageReceiver.MIME_TYPE, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)));
			rowData.put(ImageReceiver.MINI_THUMB_MAGIC, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)));
			rowData.put(ImageReceiver.ORIENTATION, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)));
			rowData.put(ImageReceiver.PICASA_ID, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.PICASA_ID)));
			rowData.put(ImageReceiver.TITLE, cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.TITLE)));
			rowData.put(ImageReceiver.WIDTH, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)));

			// used to prevent double insert (and insertion of older, downloaded images)
			if(timestamp > previousTimestamp) {
				Log.d(TAG, "Saving " + rowData.toString());
				getContentResolver().insert(ImageReceiver.CONTENT_URI, rowData);
				previousTimestamp = timestamp;
			} else {
				Log.d(TAG, "Skipping saving, as timestamp already occured");	
			}
		}

	}

	public class ExternalImageContentObserver extends ContentObserver {
		public ExternalImageContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange ExternalImageContentObserver");

			Cursor cursor = getContentResolver().query(
					ExternalImageContentUri, null, null, null,
					MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC LIMIT 1");

			saveData(cursor);
			
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	public class InternalImageContentObserver extends ContentObserver {
		public InternalImageContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange InternalImageContentObserver");

			Cursor cursor = getContentResolver().query(
					InternalImageContentUri, null, null, null,
					MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC LIMIT 1");
			
			saveData(cursor);
			
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}
}
