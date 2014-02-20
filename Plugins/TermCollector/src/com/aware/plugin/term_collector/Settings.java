package com.aware.plugin.term_collector;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;

public class Settings extends Activity {
    private final static String TAG = ContextophelesConstants.TAG_TERM_COLLECTOR + " Settings";
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

    public void updateCounts() {
        updateCount(geoDataCountView, geoDataContentUri);
        updateCount(termDataCountView, termDataContentUri);
        updateCount(geoDataCacheCountView, geoDataCacheContentUri);
    }


    public void cleanGeoData(View view) {
        CommonSettings.cleanDataForUri(getContentResolver(), geoDataContentUri);
        updateCounts();
    }

    public void cleanGeoDataCache(View view) {

        CommonSettings.cleanDataForUri(getContentResolver(), geoDataCacheContentUri);
        updateCounts();
    }

    public void cleanTermData(View view) {
        CommonSettings.cleanDataForUri(getContentResolver(), termDataContentUri);
        updateCounts();
    }

    public void updateCount(TextView textView, Uri uri) {
        textView.setText("" + CommonSettings.getCountForUri(getContentResolver(), uri));
    }


}
