package de.unipassau.mics.lutzw.simpleeuropeanaquery;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;


import eu.europeana.api.client.EuropeanaApi2Item;

public class DisplayResultsActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        ArrayList<String> your_array_list = intent.getStringArrayListExtra("results_list");

        EuropeanaApi2Item[] items = new EuropeanaApi2Item[your_array_list.size()];

        Gson gson = new Gson();
        int i = 0;

        for (String json : your_array_list) {
            items[i] = gson.fromJson(json, EuropeanaApi2Item.class);
            i++;
        }

            // This is the array adapter, it takes the context of the activity as a first // parameter, the type of list view as a second parameter and your array as a third parameter
        //ArrayAdapter<String> arrayAdapter =
        //        new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, your_array_list);


        EuropeanaApi2ResultAdapter adapter = new EuropeanaApi2ResultAdapter(this, R.layout.row, items);

        this.getListView().setAdapter(adapter);

        this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View
                    v, int position, long id) {
                TextView textView = (TextView) v.findViewById(R.id.textView);
                EuropeanaApi2Item item = (EuropeanaApi2Item) a.getItemAtPosition(position);

                Log.wtf("OnCLickListener", item.getTitle().get(0));
                Log.wtf("OnCLickListener", item.getEdmPreview().get(0));


                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getObjectURL()));
                startActivity(browserIntent);
            }
        });

    }
}