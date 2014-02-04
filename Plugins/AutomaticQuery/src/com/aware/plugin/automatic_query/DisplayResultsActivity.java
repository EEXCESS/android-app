package com.aware.plugin.automatic_query;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.aware.Aware;
import com.google.gson.Gson;

import java.util.ArrayList;

import eu.europeana.api.client.EuropeanaApi2Item;

public class DisplayResultsActivity extends ListActivity {

    public static final String TAG = "DisplayResultsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.wtf(TAG, "@OnCreate");

        onNewIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //show the right button for deactivating or activating DND
        boolean deactivateVisible = System.currentTimeMillis() < getEndOfDND();
        menu.findItem(R.id.action_activate_dnd).setVisible(!deactivateVisible);
        menu.findItem(R.id.action_deactivate_dnd).setVisible(deactivateVisible);

        //show the right button for enabling or disabling Location
        boolean useLocationEnabled = getUseLocation();
        menu.findItem(R.id.action_enable_location).setVisible(!useLocationEnabled);
        menu.findItem(R.id.action_disable_location).setVisible(useLocationEnabled);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                //show a prefilled toast with the query
                return true;
            case R.id.action_activate_dnd:
                showDNDDialog();
                return true;
            case R.id.action_deactivate_dnd:
                setEndOfDND(System.currentTimeMillis());
                createAndSendToast("Do not disturb disabled.");
                return true;
            case R.id.action_disable_location:
                setUseLocation(false);
                createAndSendToast("Use of location disabled.");
                return true;
            case R.id.action_enable_location:
                setUseLocation(true);
                createAndSendToast("Use of location enabled.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.wtf(TAG, "@OnNewIntent");

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

                EuropeanaApi2Item item = (EuropeanaApi2Item) a.getItemAtPosition(position);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getObjectURL()));
                startActivity(browserIntent);
            }
        });

    }

    public void createAndSendToast(String text) {
        Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_LONG).show();
    }

    public void showDNDDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle("Choose how long Do-Not-Disturb should be active");
        final CharSequence[] items = new CharSequence[]{"15 minutes", "1 hour", "4 hours", "12 hours", "1 Day"};
        builder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                createAndSendToast("DND activated for " + items[0] + ".");
                                setEndOfDND(System.currentTimeMillis() + 15L * 60L * 1000L);
                                break;
                            case 1:
                                createAndSendToast("DND activated for " + items[1] + ".");
                                setEndOfDND(System.currentTimeMillis() + 60L * 60L * 1000L);
                                break;
                            case 2:
                                createAndSendToast("DND activated for " + items[2] + ".");
                                setEndOfDND(System.currentTimeMillis() + 4L * 60L * 60L * 1000L);
                                break;
                            case 3:
                                createAndSendToast("DND activated for " + items[3] + ".");
                                setEndOfDND(System.currentTimeMillis() + 12L * 60L * 60L * 1000L);
                                break;
                            case 4:
                                createAndSendToast("DND activated for " + items[4] + ".");
                                setEndOfDND(System.currentTimeMillis() + 24L * 60L * 60L * 1000L);
                                break;
                        }
                    }
                });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();

    }

    public void setEndOfDND(Long endTime){
        Aware.setSetting(getContentResolver(), Settings.AWARE_END_OF_DND, endTime);
    }

    public long getEndOfDND(){
        String endOfDNDString = Aware.getSetting(getContentResolver(), Settings.AWARE_END_OF_DND);
        if(endOfDNDString != null) {
            try {
                return Long.parseLong(endOfDNDString);
            } catch (NumberFormatException e) {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

    public void setUseLocation(Boolean value){
        Aware.setSetting(getContentResolver(), Settings.AWARE_USE_LOCATION, value.toString());
    }

    public boolean getUseLocation(){
        String useLocationString = Aware.getSetting(getContentResolver(), Settings.AWARE_USE_LOCATION);
        if(useLocationString != null) {
            try {
                return Boolean.parseBoolean(useLocationString);
            } catch (NumberFormatException e) {
                return true;
            }
        } else {
            return true;
        }
    }

}