package com.aware.plugin.geoname_resolver;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
