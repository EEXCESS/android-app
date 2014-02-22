package com.aware.plugin.automatic_query.europeana;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.aware.plugin.automatic_query.Plugin;
import com.aware.plugin.automatic_query.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;
import eu.europeana.api.client.Api2Query;
import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.EuropeanaApi2Item;
import eu.europeana.api.client.EuropeanaApi2Results;

/**
 * Created by wmb on 30.09.13.
 */
public class ExecuteSearchTask extends AsyncTask<String, Void, EuropeanaApi2Results> {

    private final static String TAG = ContextophelesConstants.TAG_AUTOMATIC_QUERY +  " ExecuteSearchTask";
    private String where = "";
    private String what = "";

    private int maxNumberOfMessages = ContextophelesConstants.AQ_PLUGIN_MAX_NUMBER_OF_NOTIFICATIONS_AT_ONCE;

    private ContextWrapper wrapperRef;

    public ExecuteSearchTask(ContextWrapper wrapper) {
        this.wrapperRef = wrapper;
    }

    //params[0] contains offset as String,  params[1] contains where, params[2] contains what, rest is ignored;
    protected EuropeanaApi2Results doInBackground(String... params) {
        //create the query object
        Api2Query europeanaQuery = new Api2Query();

        int offset = Integer.parseInt(params[0]);
        where = params[1];
        what = params[2];

        europeanaQuery.setWhatTerms(what);
        europeanaQuery.setWhereTerms(where);

        //perform search
        EuropeanaApi2Client europeanaClient = new EuropeanaApi2Client();
        EuropeanaApi2Results res = null;
        try {
            res = europeanaClient.searchApi2(europeanaQuery, -1, offset);
        } catch (IOException e) {
            e.printStackTrace();
            return new EuropeanaApi2Results();
        }


        //print out search results
        // TODO: Log to Databse
        Log.wtf(TAG, "Query: " + europeanaQuery.getSearchTerms());

        try {
            // TODO: Log to Databse
            Log.wtf(TAG, "Query url: " + europeanaQuery.getQueryUrl(europeanaClient));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // TODO: Log to Databse
        Log.wtf(TAG, "Results: " + res.getItemCount() + " / " + res.getTotalResults());

        return res;
    }

    protected void onPostExecute(EuropeanaApi2Results result) {
        if (wrapperRef.getClass() == DisplayResultsActivity.class) {
            // Display Results in querying Activity
            ((DisplayResultsActivity) wrapperRef).postResultsFromQuery(result, where, what);
        } else {
            // Display Results as Notification
            postResultsFromQuery(result, where, what);
        }
    }

    public void postResultsFromQuery(EuropeanaApi2Results results, String where, String what) {

        Intent intent = new Intent(wrapperRef.getApplicationContext(), DisplayResultsActivity.class);

        ArrayList<String> resultJSONStringList = new ArrayList<String>();

        // Only react to more than the specieifed number of objects
        Log.d(TAG, "Necessary mnimum number:" + CommonSettings.getMinimumNumberOfResultsToDisplayNotification(wrapperRef.getApplicationContext().getContentResolver()));
        if (results.getAllItems().size() > CommonSettings.getMinimumNumberOfResultsToDisplayNotification(wrapperRef.getApplicationContext().getContentResolver())) {
            for (EuropeanaApi2Item item : results.getAllItems()) {
                resultJSONStringList.add(item.toJSON());
            }

            intent.putExtra("totalNumberOfResults", results.getTotalResults());
            intent.putExtra("what", what);
            intent.putExtra("where", where);
            intent.putStringArrayListExtra("results_list", resultJSONStringList);


            int notificationNumber = 0;

            if (wrapperRef.getClass() == Plugin.class) {
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // Apply modulo on notification number
                notificationNumber = (((Plugin) wrapperRef).getNotificationNumber() + 1) % maxNumberOfMessages;
                ((Plugin) wrapperRef).setNotificationNumber(notificationNumber);

                createAndSendNotification(intent, results.getTotalResults(), notificationNumber);
            } else {
                // SearchTask was started from DisplayResultsAdapter
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                wrapperRef.startActivity(intent);
            }
        }


    }

    public void createAndSendNotification(Intent intent, long numberOfResults, int notifyID) {

        Resources res = wrapperRef.getResources();
        String resultsFound = res.getQuantityString(R.plurals.numberOfResultsAvailable, (int) numberOfResults, (int) numberOfResults);

        if (!what.equals("")) {
            resultsFound += " " + res.getString(R.string.result_for) + " " + what;
        }

        if (!where.equals("")) {
            resultsFound += " " + res.getString(R.string.result_in) + " " + where;
        }

        resultsFound += ".";


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(wrapperRef.getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(res.getString(R.string.notification_title))
                        .setContentText(resultsFound);


        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(wrapperRef.getApplicationContext(), notifyID, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) wrapperRef.getSystemService(Context.NOTIFICATION_SERVICE);

        // notifyID allows you to update the notification later on.
        Notification note = mBuilder.build();


        if (CommonSettings.getNotificationUsesVibration(wrapperRef.getContentResolver())) {
            note.defaults |= Notification.DEFAULT_VIBRATE;
        }

        if (CommonSettings.getNotificationUsesSound(wrapperRef.getContentResolver())) {
            note.defaults |= Notification.DEFAULT_SOUND;
        }

        note.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(notifyID, note);

        CommonSettings.setTimeOfLastSuccessfulQuery(wrapperRef.getContentResolver(), System.currentTimeMillis());
    }


}
