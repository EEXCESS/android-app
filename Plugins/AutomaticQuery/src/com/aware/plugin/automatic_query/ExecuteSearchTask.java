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

        protected EuropeanaApi2Results doInBackground(String... terms) {
            //create the query object
            Api2Query europeanaQuery = new Api2Query();
            queryTerms = terms;
//            europeanaQuery.setCreator("picasso");
//            europeanaQuery.setType(EuropeanaComplexQuery.TYPE.IMAGE);
//            europeanaQuery.setNotProvider("Hispana");

            europeanaQuery.setGeneralTerms(terms[0]);


            //perform search
            EuropeanaApi2Client europeanaClient = new EuropeanaApi2Client();
            EuropeanaApi2Results res = null;
            try {
                res = europeanaClient.searchApi2(europeanaQuery, -1, 1);
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

                ((DisplayResultsActivity) wrapperRef).postResultsFromQuery(result, queryTerms);
            } else{
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

            intent.putExtra("queryTerms", queryTerms);
            intent.putStringArrayListExtra("results_list", your_array_list);

            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);


            int notificationNumber = 0;

            if(wrapperRef.getClass() == Plugin.class){
                notificationNumber = ((Plugin) wrapperRef).getNotificationNumber();
                ((Plugin) wrapperRef).setNotificationNumber(notificationNumber+1);
                createAndSendNotification(intent, your_array_list.size() + " new results for keywords "  + TextUtils.join(", ", queryTerms) + "!", notificationNumber);
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
