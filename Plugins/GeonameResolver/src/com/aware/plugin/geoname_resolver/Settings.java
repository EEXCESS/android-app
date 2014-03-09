package com.aware.plugin.geoname_resolver;

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
        this.TAG = ContextophelesConstants.TAG_GEONAME_RESOLVER + " Settings";
        this.contentUri = GeonameResolver_Provider.GeonameResolver.CONTENT_URI;

        super.onCreate(savedInstanceState);

        init();
    }

    private void init(){
        // Set Status of Slider
        ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getGeonameDistanceSeekBarProgress(getContentResolver()));
        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                CommonSettings.setGeonameDistanceSeekBarProgress(getContentResolver(), i);
                CommonSettings.setGeonameDistance(getContentResolver(), i * 0.1f);
                ((EditText)findViewById(R.id.distanceValue)).setText("" + CommonSettings.getGeonameDistance(getContentResolver()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });


        // Set Vaue of Distance
        ((EditText)findViewById(R.id.distanceValue)).setText("" + CommonSettings.getGeonameDistance(getContentResolver()));
        ((EditText)findViewById(R.id.distanceValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setGeonameDistanceFromString(getContentResolver(), s.toString());
                double distance = CommonSettings.getGeonameDistance(getContentResolver());
                CommonSettings.setGeonameDistanceSeekBarProgress(getContentResolver(), (int) Math.round(distance / 0.1f));
                ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getGeonameDistanceSeekBarProgress(getContentResolver()));
            }

        });

        // Set Status of Min Distance Slider
        ((SeekBar)findViewById(R.id.minDistanceSeekBar)).setMax(1000);
        ((SeekBar)findViewById(R.id.minDistanceSeekBar)).setProgress(CommonSettings.getGeonameMinimalDistanceBetweenPositions(getContentResolver()));
        ((SeekBar)findViewById(R.id.minDistanceSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                CommonSettings.setGeonameMinimalDistanceBetweenPositions(getContentResolver(), i);
                ((EditText)findViewById(R.id.minDistanceValue)).setText("" + CommonSettings.getGeonameMinimalDistanceBetweenPositions(getContentResolver()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });


        // Set Value of Distance
        ((EditText)findViewById(R.id.minDistanceValue)).setText("" + CommonSettings.getGeonameMinimalDistanceBetweenPositions(getContentResolver()));
        ((EditText)findViewById(R.id.minDistanceValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setGeonameMinimalDistanceBetweenPositionsFromString(getContentResolver(), s.toString());
                ((SeekBar)findViewById(R.id.minDistanceSeekBar)).setProgress(CommonSettings.getGeonameMinimalDistanceBetweenPositions(getContentResolver()));
            }

        });
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResetButtonClicked(View view){
        super.onResetButtonClicked(view);
        CommonSettings.setGeonameDistance(getContentResolver(), ContextophelesConstants.SETTINGS_GR_DISTANCE_DEFAULT);
        CommonSettings.setGeonameMinimalDistanceBetweenPositions(getContentResolver(), ContextophelesConstants.SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS_DEFAULT);
        init();
    }
}
