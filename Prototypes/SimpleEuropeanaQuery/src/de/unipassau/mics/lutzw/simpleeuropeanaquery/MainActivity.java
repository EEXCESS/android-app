package de.unipassau.mics.lutzw.simpleeuropeanaquery;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import eu.europeana.api.client.*;

public class MainActivity extends Activity {

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /** Called when the user clicks the Send button */
    public void startQuery(View view) {


        EditText editText = (EditText) findViewById(R.id.termsField);
        String message = editText.getText().toString();

        new ExecuteSearchTask(this).execute(message);



    }

    public void postResultsFromQuery(EuropeanaApi2Results results) {

        Intent intent = new Intent(this, DisplayResultsActivity.class);

        // Instanciating an array list (you don't need to do this, you already have yours)
        ArrayList<String> your_array_list = new ArrayList<String>();

        int count = 0;
        for (EuropeanaApi2Item item : results.getAllItems()) {


//            Log.wtf(TAG,"**** " + (count++ + 1));
//            Log.wtf(TAG,"Title: " + item.getTitle());
            your_array_list.add(item.toJSON());
//            Log.wtf(TAG,"Europeana URL: " + item.getObjectURL());
//            Log.wtf(TAG,"Type: " + item.getType());
//            Log.wtf(TAG,"Creator(s): " + item.getDcCreator());
//            Log.wtf(TAG,"Thumbnail(s): " + item.getEdmPreview());
//            Log.wtf(TAG,"Data provider: "
//                    + item.getDataProvider());
        }


        intent.putStringArrayListExtra("results_list", your_array_list);

        startActivity(intent);

    }
}
