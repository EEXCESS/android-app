package com.aware.plugin.automatic_query;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;

public class Settings extends Activity {
    private final static String TAG = ContextophelesConstants.TAG_AUTOMATIC_QUERY + " Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(TAG);

        setContentView(R.layout.debug_layout);

        init();

    }

    private void init(){
        final ToggleButton soundToggleButton = (ToggleButton) findViewById(R.id.soundButton);
        soundToggleButton.setChecked(CommonSettings.getNotificationUsesSound(getContentResolver()));

        final ToggleButton vibrationToggleButton = (ToggleButton) findViewById(R.id.vibrateButton);
        vibrationToggleButton.setChecked(CommonSettings.getNotificationUsesVibration(getContentResolver()));

        final NumberPicker minResultsNumberPicker = (NumberPicker) findViewById(R.id.numberPicker);

        minResultsNumberPicker.setMaxValue(50);
        minResultsNumberPicker.setMinValue(0);
        minResultsNumberPicker.setValue(CommonSettings.getMinimumNumberOfResultsToDisplayNotification(getContentResolver()));
        minResultsNumberPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int
                    oldVal, int newVal) {
                CommonSettings.setMinimumNumberOfResultsToDisplayNotification(getContentResolver(), newVal);
            }
        });


        // Querylist Wearoff Time

        // Set Status of Slider
        SeekBar seekBar = ((SeekBar)findViewById(R.id.seekBar));
        seekBar.setProgress(CommonSettings.getQueryListWearOffTime(getContentResolver())/1000);
        seekBar.setMax(600);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                CommonSettings.setQueryListWearOffTime(getContentResolver(), i * 1000);
                ((EditText) findViewById(R.id.wearOffTimeValue)).setText("" + (CommonSettings.getQueryListWearOffTime(getContentResolver()) / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });


        // Set Value of Token Length
        ((EditText)findViewById(R.id.wearOffTimeValue)).setText("" + (CommonSettings.getQueryListWearOffTime(getContentResolver())/1000));
        ((EditText)findViewById(R.id.wearOffTimeValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setQueryListWearOffTimeFromString(getContentResolver(), s.toString() + "000");
                ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getQueryListWearOffTime(getContentResolver())/1000);
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

    public void onToggleButtonClicked(View view) {
        // Is the view now checked?
        boolean checked = ((ToggleButton) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.soundButton:
                CommonSettings.setNotificationUsesSound(getContentResolver(), checked);
                break;
            case R.id.vibrateButton:
                CommonSettings.setNotificationUsesVibration(getContentResolver(), checked);
        }
    }


    public void onResetButtonClicked(View view){
        CommonSettings.setQueryListWearOffTime(getContentResolver(), ContextophelesConstants.SETTINGS_AQ_QUERYLIST_WEAROFF_TIME_DEFAULT);
        CommonSettings.setNotificationUsesVibration(getContentResolver(), ContextophelesConstants.SETTINGS_AQ_NOTIFICATION_VIBRATE_DEFAULT);
        CommonSettings.setNotificationUsesSound(getContentResolver(), ContextophelesConstants.SETTINGS_AQ_NOTIFICATION_SOUND_DEFAULT);
        CommonSettings.setMinimumNumberOfResultsToDisplayNotification(getContentResolver(), ContextophelesConstants.SETTINGS_AQ_MINIMUM_NUMBER_OF_RESULTS_TO_DISPLAY_NOTIFICATION_DEFAULT);
        init();
    }

}
