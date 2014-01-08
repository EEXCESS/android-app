package com.aware.plugin.automatic_query;

import android.os.AsyncTask;
import android.util.Log;

import com.aware.utils.Aware_Plugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import eu.europeana.api.client.Api2Query;
import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.EuropeanaApi2Results;

/**
 * Created by wmb on 30.09.13.
 */
public class ExecuteSearchTask extends AsyncTask<String, Void, EuropeanaApi2Results> {

    private String TAG = "ExecuteSearchTask";
    private String[] queryTerms;

    private Plugin pluginRef;

    public ExecuteSearchTask(Plugin plugin) {
        this.pluginRef = plugin;
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
            pluginRef.postResultsFromQuery(result, queryTerms);
        }
 }
