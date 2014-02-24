package com.aware.plugin.automatic_query.europeana;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aware.plugin.automatic_query.R;
import com.google.gson.Gson;

import java.util.ArrayList;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;
import eu.europeana.api.client.EuropeanaApi2Item;
import eu.europeana.api.client.EuropeanaApi2Results;

public class DisplayResultsActivity extends Activity {

    private final static String TAG = ContextophelesConstants.TAG_AUTOMATIC_QUERY + " DisplayResultsActivity";
    boolean flag_loading = false;
    boolean flag_all_loaded = false;
    private ListView m_listview;
    private GridView m_gridview;
    private String what = "";
    private String where = "";

    int lastViewedPosition = 0;
    int topOffset = 0;


    private TextView loadingView = null;

    private ArrayList<EuropeanaApi2Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.wtf(TAG, "@OnCreate");

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        setContentView(R.layout.main);

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                m_gridview = (GridView) findViewById(R.id.id_grid_view);
                break;
            default:
                m_listview = (ListView) findViewById(R.id.id_list_view);
        }


        AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

                View v;

                switch (screenSize) {
                    case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                    case Configuration.SCREENLAYOUT_SIZE_LARGE:

                       //get scroll position
                      lastViewedPosition = m_gridview.getFirstVisiblePosition();
                      //get offset
                      v = m_listview.getChildAt(0);
                      topOffset = (v == null) ? 0 : v.getTop();


                        break;
                    default:
                        //get scroll position
                       lastViewedPosition = m_listview.getFirstVisiblePosition();
                       //get offset
                       v = m_listview.getChildAt(0);
                       topOffset = (v == null) ? 0 : v.getTop();


                }

                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    if (flag_all_loaded == false && flag_loading == false) {
                        flag_loading = true;

                        switch (screenSize) {
                            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                                break;
                            default:
                                m_listview.addFooterView(loadingView);
                        }

                        loadMoreItems(totalItemCount);
                    }
                }
            }
        };


        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                m_gridview.setOnScrollListener(scrollListener);
                break;
            default:
                m_listview.setOnScrollListener(scrollListener);
        }



        Intent myIntent = getIntent();
        if (myIntent != null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.wtf(TAG, "@OnNewIntent");

        loadingView = new TextView(this);
        loadingView.setText("Loading ...");
        loadingView.setGravity(Gravity.CENTER);

        flag_all_loaded = false;
        flag_loading = false;

        items = new ArrayList<EuropeanaApi2Item>();

        what = intent.getStringExtra("what");
        where = intent.getStringExtra("where");

        ArrayList<String> resultList = intent.getStringArrayListExtra("results_list");

        if (
                resultList != null) {

            // set Time, the user has clicked the result.
            // used to temporarily disable the UIContent Plugin
            CommonSettings.setLastTimeUserClickedResultItem(getContentResolver(), System.currentTimeMillis());

            checkIfAllItemsAreLoaded(resultList.size(), intent.getLongExtra("totalNumberOfResults", 0));

            Resources res = getResources();
            String toastText = res.getQuantityString(R.plurals.toastText, (int) intent.getLongExtra("totalNumberOfResults", 0), (int) intent.getLongExtra("totalNumberOfResults", 0));

            if (!what.equals("")) {
                toastText += " " + res.getString(R.string.result_for) + " " + what;
            }

            if (!where.equals("")) {
                toastText += " " + res.getString(R.string.result_in) + " " + where;
            }

            toastText += ".";

            createAndSendToast(toastText);

            Log.d(TAG, toastText);

            items = new ArrayList<EuropeanaApi2Item>();

            Gson gson = new Gson();
            int i = 0;

            for (String json : resultList) {
                items.add(gson.fromJson(json, EuropeanaApi2Item.class));
                i++;
            }

            EuropeanaApi2ResultAdapter adapter = new EuropeanaApi2ResultAdapter(this, R.layout.row, items.toArray(new EuropeanaApi2Item[items.size()]));


            AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> a, View
                        v, int position, long id) {

                    EuropeanaApi2Item item = (EuropeanaApi2Item) a.getItemAtPosition(position);

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getObjectURL()));
                    startActivity(browserIntent);
                }
            };


            int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

            switch (screenSize) {
                case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                case Configuration.SCREENLAYOUT_SIZE_LARGE:
                    m_gridview.setAdapter(adapter);
                    m_gridview.setOnItemClickListener(onClickListener);
                    break;
                default:
                    m_listview.setAdapter(adapter);
                    m_listview.setOnItemClickListener(onClickListener);
            }


        }

    }

    public void createAndSendToast(String text) {
        Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_LONG).show();
    }

    public void showDNDDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //TODO: Extract Strings and Times
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
                                CommonSettings.setEndOfDoNotDisturb(getContentResolver(), System.currentTimeMillis() + 15L * 60L * 1000L);
                                break;
                            case 1:
                                createAndSendToast("DND activated for " + items[1] + ".");
                                CommonSettings.setEndOfDoNotDisturb(getContentResolver(), System.currentTimeMillis() + 60L * 60L * 1000L);
                                break;
                            case 2:
                                createAndSendToast("DND activated for " + items[2] + ".");
                                CommonSettings.setEndOfDoNotDisturb(getContentResolver(), System.currentTimeMillis() + 4L * 60L * 60L * 1000L);
                                break;
                            case 3:
                                createAndSendToast("DND activated for " + items[3] + ".");
                                CommonSettings.setEndOfDoNotDisturb(getContentResolver(), System.currentTimeMillis() + 12L * 60L * 60L * 1000L);
                                break;
                            case 4:
                                createAndSendToast("DND activated for " + items[4] + ".");
                                CommonSettings.setEndOfDoNotDisturb(getContentResolver(), System.currentTimeMillis() + 24L * 60L * 60L * 1000L);
                                break;
                        }
                    }
                });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();

    }

    public void showPrefilledQuery() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Modify Query");
        alert.setMessage("Modify the query to better fit your needs.");

        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);


        final EditText whatField = (EditText) textEntryView.findViewById(R.id.what_edit);
        whatField.setText(what);

        final EditText whereField = (EditText) textEntryView.findViewById(R.id.where_edit);
        whereField.setText(where);

        alert.setView(textEntryView);

        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String where = whereField.getText().toString();
                String what = whatField.getText().toString();

                // Do something with value!
                new ExecuteSearchTask(getApplication()).execute(new String[]{"0", where, what});
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    private void loadMoreItems(int offset) {
        Log.d(TAG, "@LoadMoreItems with offset " + offset);

        new ExecuteSearchTask(this).execute(new String[]{new Integer(offset).toString(), where, what});

    }

    public void postResultsFromQuery(EuropeanaApi2Results results, String where, String what) {
        Log.d(TAG, "loaded more results for where " + where + " what " + what);
        if (items == null) {
            items = new ArrayList<EuropeanaApi2Item>();
        }

        if (results.getAllItems() != null) {
            items.addAll(results.getAllItems());
        }


        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                m_gridview.setAdapter(new EuropeanaApi2ResultAdapter(this, R.layout.row, items.toArray(new EuropeanaApi2Item[items.size()])));
                //m_gridview.setSelectionFromTop(lastViewedPosition, topOffset);
                break;
            default:
                m_listview.setAdapter(new EuropeanaApi2ResultAdapter(this, R.layout.row, items.toArray(new EuropeanaApi2Item[items.size()])));

                m_listview.removeFooterView(loadingView);
                m_listview.setSelectionFromTop(lastViewedPosition, topOffset);

        }




        checkIfAllItemsAreLoaded(items.size(), results.getTotalResults());

        flag_loading = false;
    }

    private void checkIfAllItemsAreLoaded(int numberOfLoadedItems, long numberOfItemsTotal) {
        if (numberOfLoadedItems >= numberOfItemsTotal) {
            flag_all_loaded = true;
        }

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
        boolean deactivateVisible = System.currentTimeMillis() < CommonSettings.getEndOfDoNotDisturb(getContentResolver());
        menu.findItem(R.id.action_activate_dnd).setVisible(!deactivateVisible);
        menu.findItem(R.id.action_deactivate_dnd).setVisible(deactivateVisible);

        //show the right button for enabling or disabling Location
        boolean useLocationEnabled = CommonSettings.getQueryUseOfLocation(getContentResolver());
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
                showPrefilledQuery();
                return true;
            case R.id.action_activate_dnd:
                showDNDDialog();
                return true;
            case R.id.action_deactivate_dnd:
                CommonSettings.setEndOfDoNotDisturb(getContentResolver(), System.currentTimeMillis());
                createAndSendToast("Do not disturb disabled.");
                return true;
            case R.id.action_disable_location:
                CommonSettings.setQueryUseOfLocation(getContentResolver(), false);
                createAndSendToast("Use of location disabled.");
                return true;
            case R.id.action_enable_location:
                CommonSettings.setQueryUseOfLocation(getContentResolver(), true);
                createAndSendToast("Use of location enabled.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}