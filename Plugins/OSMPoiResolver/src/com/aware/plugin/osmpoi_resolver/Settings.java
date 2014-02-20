package com.aware.plugin.osmpoi_resolver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private final static String TAG = ContextophelesConstants.TAG_OSMPOI_RESOLVER + " Settings";
    private static TextView countView = null;
    private static Uri contentUri = OSMPoiResolver_Provider.OSMPoiResolver.CONTENT_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);

        setContentView(R.layout.debug_layout);
        
        countView = (TextView) findViewById(R.id.count);
        
        updateCount();

        // Set On/off for fake Location

        ((ToggleButton)findViewById(R.id.useFakeLocationButton)).setChecked(CommonSettings.getUseFakeLocation(getContentResolver()));

        // Set Value for Latitude

        ((EditText)findViewById(R.id.fakeLatitudeValue)).setText("" + CommonSettings.getFakeLatitude(getContentResolver()));
        ((EditText)findViewById(R.id.fakeLatitudeValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setFakeLatitudeFromString(getContentResolver(), s.toString());
                if(CommonSettings.getUseFakeLocation(getContentResolver())){
                    notifyForLocationChange();
                }
            }

        });



        // Set Value for Longitude

        ((EditText)findViewById(R.id.fakeLongitudeValue)).setText("" + CommonSettings.getFakeLongitude(getContentResolver()));
        ((EditText)findViewById(R.id.fakeLongitudeValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setFakeLongitudeFromString(getContentResolver(), s.toString());
                if(CommonSettings.getUseFakeLocation(getContentResolver())){
                    notifyForLocationChange();
                }
            }

        });


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
    }
    
    protected void onResume() {
        super.onResume();
    	updateCount();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    
    public void updateCount() {
        countView.setText("" + CommonSettings.getCountForUri(getContentResolver(), contentUri));
    }

    public void cleanData(View view){
        CommonSettings.cleanDataForUri(getContentResolver(), contentUri);
        updateCount();
    }

    public void onSwitchClicked(View view) {
        // Is the view now checked?
        boolean checked = ((ToggleButton) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.useFakeLocationButton:
                CommonSettings.setUseFakeLocation(getContentResolver(), checked);
                if(CommonSettings.getUseFakeLocation(getContentResolver())){
                    notifyForLocationChange();
                }
                break;
        }
    }

    public void onChooseClicked(View view) {
        showFakeLocationDialog();
    }

    //Todo: Make this part of the base project and load locations from a json file
    public void showFakeLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose the location to fake");
        final CharSequence[] items = new CharSequence[]{"Rome", "Berlin", "Regensburg", "Passau", "London", "Luxembourg"};
        builder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                // Rome
                                setLatitudeAndLongitudeInSettingsAndMenu(41.9000, 12.5000);
                                break;
                            case 1:
                                // Berlin
                                setLatitudeAndLongitudeInSettingsAndMenu(52.5167, 13.3833);
                                break;
                            case 2:
                                // Regensburg
                                setLatitudeAndLongitudeInSettingsAndMenu(49.015, 12.09556);
                                break;
                            case 3:
                                // Passau
                                setLatitudeAndLongitudeInSettingsAndMenu(48.5667, 13.4667);
                                break;
                            case 4:
                                // London
                                setLatitudeAndLongitudeInSettingsAndMenu(51.5072, 0.1275);
                                break;
                            case 5:
                                // Luxembourg
                                setLatitudeAndLongitudeInSettingsAndMenu(49.6117, 6.1300);
                                break;
                        }
                    }
                });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();

    }

    private void setLatitudeAndLongitudeInSettingsAndMenu(double latitude, double longitude){
        CommonSettings.setFakeLatitude(getContentResolver(), latitude);
        CommonSettings.setFakeLongitude(getContentResolver(), longitude);
        ((EditText)findViewById(R.id.fakeLatitudeValue)).setText("" + CommonSettings.getFakeLatitude(getContentResolver()));
        ((EditText)findViewById(R.id.fakeLongitudeValue)).setText("" + CommonSettings.getFakeLongitude(getContentResolver()));
        if(CommonSettings.getUseFakeLocation(getContentResolver())){
            notifyForLocationChange();
        }
    }


    private void notifyForLocationChange(){
        getContentResolver().notifyChange(Uri.parse(ContextophelesConstants.LOCATION_URI), null);
    }
}
