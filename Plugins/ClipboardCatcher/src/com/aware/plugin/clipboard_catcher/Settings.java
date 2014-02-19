package com.aware.plugin.clipboard_catcher;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;

public class Settings extends Activity {
    private final static String TAG = ContextophelesConstants.TAG_CLIPBOARD_CATCHER + " Settings";
    private static TextView countView = null;
    private static Uri contentUri = ClipboardCatcher_Provider.ClipboardCatcher.CONTENT_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(TAG);

        setContentView(R.layout.debug_layout);
        
        countView = (TextView) findViewById(R.id.count);
        
        updateCount();
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
}
