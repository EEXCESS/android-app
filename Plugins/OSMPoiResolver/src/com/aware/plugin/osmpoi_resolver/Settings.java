package com.aware.plugin.osmpoi_resolver;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Settings extends Activity {
    private final static String TAG = "OSMPoiResolver Settings";
    private static TextView countView = null;
    private static Uri contentUri = OSMPoiResolver_Provider.OSMPoiResolver.CONTENT_URI;

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
    	Cursor countCursor = getContentResolver().query(contentUri,
                new String[] {"count(*) AS count"},
                null,
                null,
                null);

        countCursor.moveToFirst();
        int count = countCursor.getInt(0);

        countView.setText("" + count);
    }

    public void cleanData(View view){
        Log.d(TAG, "Trying to delete all Data.");
        getContentResolver().delete(contentUri, " 1 = 1 ", null);
        Log.d(TAG, "Deletion done.");
        updateCount();
    }
}
