package com.aware.plugin.osmpoi_resolver;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;
import de.unipassau.mics.contextopheles.utils.GeoSettings;

public class Settings extends GeoSettings {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.TAG = ContextophelesConstants.TAG_OSMPOI_RESOLVER + " Settings";
        this.contentUri = contentUri = OSMPoiResolver_Provider.OSMPoiResolver.CONTENT_URI;

        super.onCreate(savedInstanceState);

      init();
    }

    private void init(){
        // Set Status of Slider
        ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getOSMPoiDistanceSeekBarProgress(getContentResolver()));
        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                CommonSettings.setOSMPoiDistanceSeekBarProgress(getContentResolver(), i);
                CommonSettings.setOSMPoiDistance(getContentResolver(), i * 0.1f);
                ((EditText)findViewById(R.id.distanceValue)).setText("" + CommonSettings.getOSMPoiDistance(getContentResolver()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });


        // Set Value of Distance
        ((EditText)findViewById(R.id.distanceValue)).setText("" + CommonSettings.getOSMPoiDistance(getContentResolver()));
        ((EditText)findViewById(R.id.distanceValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setOSMPoiDistanceFromString(getContentResolver(), s.toString());
                double distance = CommonSettings.getOSMPoiDistance(getContentResolver());
                CommonSettings.setOSMPoiDistanceSeekBarProgress(getContentResolver(), (int) Math.round(distance / 0.1f));
                ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getOSMPoiDistanceSeekBarProgress(getContentResolver()));
            }

        });


        // Set Status of Min Distance Slider
        ((SeekBar)findViewById(R.id.minDistanceSeekBar)).setMax(1000);
        ((SeekBar)findViewById(R.id.minDistanceSeekBar)).setProgress(CommonSettings.getOSMPoiMinimalDistanceBetweenPositions(getContentResolver()));
        ((SeekBar)findViewById(R.id.minDistanceSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                CommonSettings.setOSMPoiMinimalDistanceBetweenPositions(getContentResolver(), i);
                ((EditText)findViewById(R.id.minDistanceValue)).setText("" + CommonSettings.getOSMPoiMinimalDistanceBetweenPositions(getContentResolver()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });


        // Set Value of  Min Distance
        ((EditText)findViewById(R.id.minDistanceValue)).setText("" + CommonSettings.getOSMPoiMinimalDistanceBetweenPositions(getContentResolver()));
        ((EditText)findViewById(R.id.minDistanceValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setOSMPoiMinimalDistanceBetweenPositionsFromString(getContentResolver(), s.toString());
                ((SeekBar)findViewById(R.id.minDistanceSeekBar)).setProgress(CommonSettings.getOSMPoiMinimalDistanceBetweenPositions(getContentResolver()));
            }

        });
    }

    @Override
    public void onResetButtonClicked(View view){
        super.onResetButtonClicked(view);
        CommonSettings.setOSMPoiDistance(getContentResolver(), ContextophelesConstants.SETTINGS_OR_DISTANCE_DEFAULT);
        CommonSettings.setOSMPoiMinimalDistanceBetweenPositions(getContentResolver(), ContextophelesConstants.SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS_DEFAULT);
        init();
    }
}
