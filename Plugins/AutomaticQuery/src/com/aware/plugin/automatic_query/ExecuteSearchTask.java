package com.aware.plugin.automatic_query;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import eu.europeana.api.client.Api2Query;
import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.EuropeanaApi2Item;
import eu.europeana.api.client.EuropeanaApi2Results;

/**
 * Created by wmb on 30.09.13.
 */
public class ExecuteSearchTask extends AsyncTask<String, Void, EuropeanaApi2Results> {

    private String TAG = "ExecuteSearchTask";
    private String[] queryTerms;

    private ContextWrapper wrapperRef;

    public ExecuteSearchTask(ContextWrapper wrapper) {
        this.wrapperRef = wrapper;
    }
        //params[0] contains offset as String, rest is searchterms
        protected EuropeanaApi2Results doInBackground(String... params) {
            //create the query object
            Api2Query europeanaQuery = new Api2Query();


            queryTerms = Arrays.copyOfRange(params, 1, params.length);
            int offset = Integer.parseInt(params[0]);

            europeanaQuery.setGeneralTerms(queryTerms[0]);



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
                Log.wtf(TAG,"Query url: " + europeanaQuery.getQueryUrl(europeanaClient));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.wtf(TAG,"Results: " + res.getItemCount() + " / " + res.getTotalResults());

            return res;
        }

        protected void onPostExecute(EuropeanaApi2Results result) {
            if(wrapperRef.getClass() == DisplayResultsActivity.class){
                Log.d(TAG, "First case");
                ((DisplayResultsActivity) wrapperRef).postResultsFromQuery(result, queryTerms);
            } else{
                Log.d(TAG, "Second case");
                postResultsFromQuery(result, queryTerms);
            }
        }

    public void postResultsFromQuery(EuropeanaApi2Results results, String[] queryTerms) {

        Intent intent = new Intent(wrapperRef.getApplicationContext(), DisplayResultsActivity.class);



        // Instanciating an array list (you don't need to do this, you already have yours)
        ArrayList<String> your_array_list = new ArrayList<String>();

        // Only react to non-empty resultsets
        if(results.getAllItems().size() > 0) {
            for (EuropeanaApi2Item item : results.getAllItems()) {
                your_array_list.add(item.toJSON());
            }

            intent.putExtra("totalNumberOfResults", results.getTotalResults());
            intent.putExtra("queryTerms", queryTerms);
            intent.putStringArrayListExtra("results_list", your_array_list);



            int notificationNumber = 0;

            if(wrapperRef.getClass() == Plugin.class){
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notificationNumber = ((Plugin) wrapperRef).getNotificationNumber();
                ((Plugin) wrapperRef).setNotificationNumber(notificationNumber+1);
                createAndSendNotification(intent, results.getTotalResults() + " results for keywords "  + TextUtils.join(", ", queryTerms) + "!", notificationNumber);
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
                        .setContentTitle("A new query was run")
                        .setContentText(term);


        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(wrapperRef.getApplicationContext(),notifyID,intent,PendingIntent.FLAG_UPDATE_CURRENT);



        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) wrapperRef.getSystemService(Context.NOTIFICATION_SERVICE);

        // notifyID allows you to update the notification later on.
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        note.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(notifyID, note);

    }
 }
