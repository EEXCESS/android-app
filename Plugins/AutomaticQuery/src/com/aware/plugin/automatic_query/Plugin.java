package com.aware.plugin.automatic_query;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;


import com.aware.utils.Aware_Plugin;

import java.util.ArrayList;

import eu.europeana.api.client.EuropeanaApi2Item;
import eu.europeana.api.client.EuropeanaApi2Results;

/**
 * A Tool, that listens to the TermCollector, does Europeana Queries and sends out a Notification
 * 
 * @author Wolfgang Lutz
 * @email: wolfgang@lutz-wiesent.de
 * 
 */

public class Plugin extends Aware_Plugin {

	private static final String TAG = "AutomaticQuery Plugin";

	public static Uri termCollectorContentUri;
	private static TermCollectorObserver termCollectorObs = null;


	/**
	 * Thread manager
	 */
	private static HandlerThread threads = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "Plugin Created");
		super.onCreate();


		threads = new HandlerThread(TAG);
		threads.start();

		// Set the observers, that run in independent threads, for
		// responsiveness

        termCollectorContentUri = Uri
				.parse("content://com.aware.provider.plugin.term_collector/plugin_term_collector");
        termCollectorObs = new TermCollectorObserver(new Handler(
				threads.getLooper()));
		getContentResolver().registerContentObserver(
                termCollectorContentUri, true, termCollectorObs);
		Log.d(TAG, "termCollectorObs registered");
		

		
		
		Log.d(TAG, "Plugin Started");
	}

	@Override
	public void onDestroy() {

		Log.d(TAG, "Plugin is destroyed");

		super.onDestroy();

		getContentResolver().unregisterContentObserver(termCollectorObs);

	}

	protected void runQuery(String term) {
		Log.d(TAG, "Running Query for term " + term);

        new ExecuteSearchTask(this).execute(term);

    }

    public void createAndSendNotification(String term) {
        int notifyID = 1;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("A new query was run")
                        .setContentText(term);

//// Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, MainActivity.class);
//
//// The stack builder object will contain an artificial back stack for the
//// started Activity.
//// This ensures that navigating backward from the Activity leads out of
//// your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//// Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
//// Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notifyID allows you to update the notification later on.
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        mNotificationManager.notify(notifyID, note);

    }

    public void postResultsFromQuery(EuropeanaApi2Results results) {

      //  Intent intent = new Intent(this, DisplayResultsActivity.class);

        // Instanciating an array list (you don't need to do this, you already have yours)
        ArrayList<String> your_array_list = new ArrayList<String>();

        int count = 0;
        for (EuropeanaApi2Item item : results.getAllItems()) {


//            Log.wtf(TAG,"**** " + (count++ + 1));
//            Log.wtf(TAG,"Title: " + item.getTitle());
            your_array_list.add(item.toJSON());
//            Log.wtf(TAG,"Europeana URL: " + item.getObjectURL());
//            Log.wtf(TAG,"Type: " + item.getType());
//            Log.wtf(TAG,"Creator(s): " + item.getDcCreator());
//            Log.wtf(TAG,"Thumbnail(s): " + item.getEdmPreview());
//            Log.wtf(TAG,"Data provider: "
//                    + item.getDataProvider());
        }


     //   intent.putStringArrayListExtra("results_list", your_array_list);

       // startActivity(intent);

        createAndSendNotification(your_array_list.size() + " new results!");


    }

	public class TermCollectorObserver extends ContentObserver {
		public TermCollectorObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange");

			// set cursor to first item
			Cursor cursor = getContentResolver().query(
					termCollectorContentUri, null, null, null,
					"timestamp" + " DESC LIMIT 1");
			if (cursor != null && cursor.moveToFirst()) {

                runQuery(cursor.getString(cursor
                        .getColumnIndex("term_content")));
			}

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}
}
