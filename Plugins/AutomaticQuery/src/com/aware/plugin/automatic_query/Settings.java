package com.aware.plugin.automatic_query;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.aware.Aware;

public class Settings extends Activity {
    private final static String TAG = "AutomaticQuery Settings";
    private static TextView countView = null;

    public static final String AWARE_END_OF_DND = "AWARE_END_OF_DND";
    public static final String AWARE_USE_LOCATION = "AWARE_USE_LOCATION";
    public static final String AWARE_LAST_SUCCESSFUL_QUERY = "AWARE_LAST_SUCCESSFUL_QUERY";
    public static final String AWARE_LAST_TIME_USER_CLICKED_RESULTITEM = "AWARE_LAST_TIME_USER_CLICKED_RESULTITEM";
    public static final String AWARE_QUERY_NOTIFICATION_VIBRATE = "AWARE_QUERY_NOTIFICATION_VIBRATE";
    public static final String AWARE_QUERY_NOTIFICATION_SOUND = "AWARE_QUERY_NOTIFICATION_SOUND";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(TAG);

        setContentView(R.layout.debug_layout);
        
        countView = (TextView) findViewById(R.id.count);

        final CheckBox soundCheckBox = (CheckBox) findViewById(R.id.usesSound);
        soundCheckBox.setChecked(getNotificationUsesSound());

        final CheckBox vibrationCheckBox = (CheckBox) findViewById(R.id.usesSound);
        vibrationCheckBox.setChecked(getNotificationUsesVibration());

        
        //updateCount();
    }
    
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

    public void onSoundCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();


        //TODO: FIX
        Log.d(TAG, "@onSoundCheckboxClicked: "  + checked);
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.usesSound:
                setNotificationUsesSound(checked);
                break;
            case R.id.usesVibration:
                setNotificationUsesVibration(checked);
        }
    }


    public void setNotificationUsesSound(Boolean value){
        Aware.setSetting(getContentResolver(), AWARE_QUERY_NOTIFICATION_SOUND, value.toString());
    }

    public void setNotificationUsesVibration(Boolean value){
        Aware.setSetting(getContentResolver(), AWARE_QUERY_NOTIFICATION_VIBRATE, value.toString());
    }

    public boolean getNotificationUsesSound(){
        String useSoundString = Aware.getSetting(getContentResolver(), Settings.AWARE_QUERY_NOTIFICATION_SOUND);
        if(useSoundString != null) {
            try {
                return Boolean.parseBoolean(useSoundString);
            } catch (NumberFormatException e) {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean getNotificationUsesVibration(){
        String useVibrationString = Aware.getSetting(getContentResolver(), Settings.AWARE_QUERY_NOTIFICATION_VIBRATE);
        if(useVibrationString != null) {
            try {
                return Boolean.parseBoolean(useVibrationString);
            } catch (NumberFormatException e) {
                return true;
            }
        } else {
            return true;
        }
    }

}
