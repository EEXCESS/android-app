package com.aware.plugin.automatic_query;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;

public class Settings extends Activity {
    private final static String TAG = ContextophelesConstants.TAG_AUTOMATIC_QUERY + " Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(TAG);

        setContentView(R.layout.debug_layout);

        final CheckBox soundCheckBox = (CheckBox) findViewById(R.id.usesSound);
        soundCheckBox.setChecked(CommonSettings.getNotificationUsesSound(getContentResolver()));

        final CheckBox vibrationCheckBox = (CheckBox) findViewById(R.id.usesVibration);
        vibrationCheckBox.setChecked(CommonSettings.getNotificationUsesVibration(getContentResolver()));
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

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.usesSound:
                CommonSettings.setNotificationUsesSound(getContentResolver(), checked);
                break;
            case R.id.usesVibration:
                CommonSettings.setNotificationUsesVibration(getContentResolver(), checked);
        }
    }
}
