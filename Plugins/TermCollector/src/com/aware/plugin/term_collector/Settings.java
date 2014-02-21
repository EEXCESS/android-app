package com.aware.plugin.term_collector;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

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

        // Apply Stop Word List
        ((ToggleButton)findViewById(R.id.stopWordButton)).setChecked(CommonSettings.getTermCollectorApplyStopwords(getContentResolver()));

        // Minimal Token Length

        // Set Status of Slider
        ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getMinimalTermCollectorTokenLength(getContentResolver()));
        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                CommonSettings.setMinimalTermCollectorTokenLength(getContentResolver(), i);
                ((EditText)findViewById(R.id.tokenLengthValue)).setText("" + CommonSettings.getMinimalTermCollectorTokenLength(getContentResolver()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });


        // Set Value of Token Length
        ((EditText)findViewById(R.id.tokenLengthValue)).setText("" + CommonSettings.getMinimalTermCollectorTokenLength(getContentResolver()));
        ((EditText)findViewById(R.id.tokenLengthValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setMinimalTermCollectorTokenLengthFromString(getContentResolver(), s.toString());
                ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getMinimalTermCollectorTokenLength(getContentResolver()));
            }

        });
    }


    public void onToggleButtonClicked(View view) {
        // Is the view now checked?
        boolean checked = ((ToggleButton) view).isChecked();

        // Check which checkbox was clicked
        if (view.getId() == R.id.stopWordButton) {
            CommonSettings.setTermCollectorApplyStopwords(getContentResolver(), checked);
        }
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
