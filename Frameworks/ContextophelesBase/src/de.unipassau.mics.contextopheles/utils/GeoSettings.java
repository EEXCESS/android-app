package de.unipassau.mics.contextopheles.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import de.unipassau.mics.contextopheles.R;
import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

public class GeoSettings extends Activity {
    protected static String TAG = "GeoSettings";
    protected  Uri contentUri = null;
    protected TextView countView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(TAG);
        setContentView(R.layout.debug_layout_geosettings);

        countView = (TextView) findViewById(R.id.count);

        updateCount();

        init();

    }

    private  void init() {
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

    public void cleanData(View view) {
        CommonSettings.cleanDataForUri(getContentResolver(), contentUri);
        updateCount();
    }


    public void onSwitchClicked(View view) {
        // Is the view now checked?
        boolean checked = ((ToggleButton) view).isChecked();

        // Check which checkbox was clicked
        if (view.getId() == R.id.useFakeLocationButton) {

                CommonSettings.setUseFakeLocation(getContentResolver(), checked);
                if(CommonSettings.getUseFakeLocation(getContentResolver())){
                    notifyForLocationChange();
                }
        }
    }

    public void onChooseClicked(View view) {
        showFakeLocationDialog();
    }

    //Todo: load locations from a json file
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
        CommonSettings.setUseFakeLocation(getContentResolver(), true);

        init();

        if(CommonSettings.getUseFakeLocation(getContentResolver())){
            notifyForLocationChange();
        }
    }


    private void notifyForLocationChange(){
        getContentResolver().notifyChange(Uri.parse(ContextophelesConstants.LOCATION_URI), null);
    }

    public void onResetButtonClicked(View view){
        CommonSettings.setUseFakeLocation(getContentResolver(), ContextophelesConstants.SETTINGS_USE_FAKE_LOCATION_DEFAULT);
        CommonSettings.setFakeLatitude(getContentResolver(), ContextophelesConstants.SETTINGS_FAKE_LATITUDE_DEFAULT);
        CommonSettings.setFakeLongitude(getContentResolver(), ContextophelesConstants.SETTINGS_FAKE_LONGITUDE_DEFAULT);
        init();
    }
}
