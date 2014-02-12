package com.aware.plugin.automatic_query;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Settings extends Activity {
    private final static String TAG = "AutomaticQuery Settings";
    private static TextView countView = null;

    public static final String AWARE_END_OF_DND = "AWARE_END_OF_DND";
    public static final String AWARE_USE_LOCATION = "AWARE_USE_LOCATION";
    public static final String AWARE_LAST_SUCCESSFUL_QUERY = "AWARE_LAST_SUCCESSFUL_QUERY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(TAG);

        setContentView(R.layout.debug_layout);
        
        countView = (TextView) findViewById(R.id.count);
        
        //updateCount();
    }
    
    protected void onResume() {
        super.onResume();
    	//updateCount();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

}
