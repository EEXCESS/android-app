package com.aware.plugin.clipboard_catcher;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Settings extends Activity {
    private final static String TAG = "ClipboardCatcher Settings";
    private static TextView countView = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
    	Cursor countCursor = getContentResolver().query(ClipboardCatcher_Provider.ClipboardCatcher.CONTENT_URI,
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
        getContentResolver().delete(ClipboardCatcher_Provider.ClipboardCatcher.CONTENT_URI, " 1 = 1 ", null);
        Log.d(TAG, "Deletion done.");
        updateCount();
    }
}
