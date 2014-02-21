package com.aware.plugin.ui_content;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;

public class Settings extends Activity {
    private final static String TAG = ContextophelesConstants.TAG_UI_CONTENT + " Settings";
    private static TextView countView = null;
    private static Uri contentUri = UIContent_Provider.UIContents.CONTENT_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);

        setContentView(R.layout.debug_layout);
        
        countView = (TextView) findViewById(R.id.count);
        
        updateCount();

        // Minimal Token Length

        // Set Status of Slider
        ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getMinimalUIContentLength(getContentResolver()));
        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                CommonSettings.setMinimalUIContentLength(getContentResolver(), i);
                ((EditText)findViewById(R.id.tokenLengthValue)).setText("" + CommonSettings.getMinimalUIContentLength(getContentResolver()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });


        // Set Value of Token Length
        ((EditText)findViewById(R.id.tokenLengthValue)).setText("" + CommonSettings.getMinimalUIContentLength(getContentResolver()));
        ((EditText)findViewById(R.id.tokenLengthValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CommonSettings.setMinimalUIContentLengthFromString(getContentResolver(), s.toString());
                ((SeekBar)findViewById(R.id.seekBar)).setProgress(CommonSettings.getMinimalUIContentLength(getContentResolver()));
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
}
