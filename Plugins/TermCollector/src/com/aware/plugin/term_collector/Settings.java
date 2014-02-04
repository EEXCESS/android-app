package com.aware.plugin.term_collector;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Settings extends Activity {
    private final static String TAG = "TermCollector Settings";
    private static TextView geoDataCountView = null;
    private static TextView geoDataCacheCountView = null;
    private static TextView termDataCountView = null;
    private static Uri geoDataContentUri = TermCollector_Provider.TermCollectorGeoData.CONTENT_URI;
    private static Uri geoDataCacheContentUri = TermCollector_Provider.TermCollectorGeoDataCache.CONTENT_URI;
    private static Uri termDataContentUri = TermCollector_Provider.TermCollectorTermData.CONTENT_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);

        setContentView(R.layout.debug_layout);
        
        geoDataCountView = (TextView) findViewById(R.id.geodatacount);
        geoDataCacheCountView = (TextView) findViewById(R.id.geodatacachecount);
        termDataCountView = (TextView) findViewById(R.id.termdatacount);
        
        updateCounts();
    }
    
    protected void onResume() {
        super.onResume();
    	updateCounts();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

    public void updateCounts(){
        updateCount(geoDataCountView, geoDataContentUri);
        updateCount(termDataCountView, termDataContentUri);
        updateCount(geoDataCacheCountView, geoDataCacheContentUri);
    }

    public void updateCount(TextView textView, Uri uri) {
    	Cursor countCursor = getContentResolver().query(uri,
                new String[] {"count(*) AS count"},
                null,
                null,
                null);

        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        textView.setText("" + count);
    }

    public void cleanGeoData(View view){
        cleanData(geoDataContentUri);
    }

    public void cleanGeoDataCache(View view) {
                cleanData(geoDataCacheContentUri);
            }

    public void cleanTermData(View view) {
        cleanData(termDataContentUri);
    }

    private void cleanData(Uri contentUri){
        Log.d(TAG, "Trying to delete all Data.");
        getContentResolver().delete(contentUri, " 1 = 1 ", null);
        Log.d(TAG, "Deletion done.");

        updateCounts();
    }
}
