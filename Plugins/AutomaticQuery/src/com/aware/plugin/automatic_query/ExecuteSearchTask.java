package com.aware.plugin.automatic_query;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.aware.Aware;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import eu.europeana.api.client.Api2Query;
import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.EuropeanaApi2Item;
import eu.europeana.api.client.EuropeanaApi2Results;

/**
 * Created by wmb on 30.09.13.
 */
public class ExecuteSearchTask extends AsyncTask<String, Void, EuropeanaApi2Results> {

    private String TAG = "ExecuteSearchTask";
    private String where = "";
    private String what = "";

    private int maxNumberOfMessages = 5;

    private ContextWrapper wrapperRef;

    public ExecuteSearchTask(ContextWrapper wrapper) {
        this.wrapperRef = wrapper;
    }

    //params[0] contains offset as String, rest is searchterms
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
        Log.wtf(TAG, "Query: " + europeanaQuery.getSearchTerms());

        try {
            Log.wtf(TAG, "Query url: " + europeanaQuery.getQueryUrl(europeanaClient));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.wtf(TAG, "Results: " + res.getItemCount() + " / " + res.getTotalResults());

        return res;
    }

    protected void onPostExecute(EuropeanaApi2Results result) {
        if (wrapperRef.getClass() == DisplayResultsActivity.class) {
            Log.d(TAG, "First case, " + where + " " + what);
            ((DisplayResultsActivity) wrapperRef).postResultsFromQuery(result, where, what);
        } else {
            Log.d(TAG, "Second case, " + where + " " + what);
            postResultsFromQuery(result, where, what);
        }
    }

    public void postResultsFromQuery(EuropeanaApi2Results results, String where, String what) {

        Intent intent = new Intent(wrapperRef.getApplicationContext(), DisplayResultsActivity.class);


        // Instanciating an array list (you don't need to do this, you already have yours)
        ArrayList<String> your_array_list = new ArrayList<String>();

        // Only react to more than 5 objects
        if (results.getAllItems().size() > 5) {
            for (EuropeanaApi2Item item : results.getAllItems()) {
                your_array_list.add(item.toJSON());
            }

            intent.putExtra("totalNumberOfResults", results.getTotalResults());
            intent.putExtra("what", what);
            intent.putExtra("where", where);
            intent.putStringArrayListExtra("results_list", your_array_list);


            int notificationNumber = 0;

            if (wrapperRef.getClass() == Plugin.class) {
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // Apply modulo on notification number

                notificationNumber = ((Plugin) wrapperRef).getNotificationNumber() % maxNumberOfMessages;
                ((Plugin) wrapperRef).setNotificationNumber(notificationNumber + 1);
                String msgText;

                if (what.equals("")) {
                    msgText = " results in " + where;
                } else if (where.equals("")) {
                    msgText = " results for " + what;
                } else {
                    msgText = " results in " + where + " for "  + what;
                }

                createAndSendNotification(intent, results.getTotalResults() + msgText, notificationNumber);
            } else {
                // SearchTask was started from DisplayResultsAdapter
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                wrapperRef.startActivity(intent);
            }
        }


    }

    public void createAndSendNotification(Intent intent, String term, int notifyID) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(wrapperRef.getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New Europeana results")
                        .setContentText(term);


        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(wrapperRef.getApplicationContext(), notifyID, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) wrapperRef.getSystemService(Context.NOTIFICATION_SERVICE);

        // notifyID allows you to update the notification later on.
        Notification note = mBuilder.build();


        if(getNotificationUsesVibration()){
            note.defaults |= Notification.DEFAULT_VIBRATE;
        }

        if(getNotificationUsesSound()){
            note.defaults |= Notification.DEFAULT_SOUND;
        }

        note.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(notifyID, note);

        setTimeOfLastSuccessfulQuery(System.currentTimeMillis());
    }

    public void setTimeOfLastSuccessfulQuery(Long endTime) {
        Aware.setSetting(wrapperRef.getContentResolver(), Settings.AWARE_LAST_SUCCESSFUL_QUERY, endTime);
    }


    public boolean getNotificationUsesSound(){
        String useSoundString = Aware.getSetting(wrapperRef.getContentResolver(), Settings.AWARE_QUERY_NOTIFICATION_SOUND);
        if(useSoundString != null) {
            try {
                return Boolean.parseBoolean(useSoundString);
            } catch (NumberFormatException e) {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean getNotificationUsesVibration(){
        String useVibrationString = Aware.getSetting(wrapperRef.getContentResolver(), Settings.AWARE_QUERY_NOTIFICATION_VIBRATE);
        if(useVibrationString != null) {
            try {
                return Boolean.parseBoolean(useVibrationString);
            } catch (NumberFormatException e) {
                return true;
            }
        } else {
            return true;
        }
    }
}
