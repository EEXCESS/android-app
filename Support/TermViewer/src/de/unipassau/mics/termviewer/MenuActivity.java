package de.unipassau.mics.termviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;

/**
 * Created by wmb on 23.01.14.
 */
public class MenuActivity extends Activity {

    public static final String TAG = "TermViewer MenuActivity";

    private ListView mTermList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(TAG, "Activity State: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        mTermList = (ListView) findViewById(R.id.menuEntryList);

        final String[] activities = new String[] {
                "ClipboardCatcher",
                "TermCollectorTerms",
                "TermCollectorGeodata",
               // "AutomaticQuery",
                "GeoCollector",
                "GeonameResolver",
                "OSMPoiResolver",
                "ImageReceiver",
                "SMSReceiver",
                "UIContent"
        };


        final HashMap<String, ActivityDescriptor> activityDescriptions = new HashMap<String, ActivityDescriptor>();

        activityDescriptions.put("ClipboardCatcher", new ActivityDescriptor("ClipboardCatcher", "content://com.aware.provider.plugin.clipboard_catcher/plugin_clipboard_catcher", "CLIPBOARDCONTENT"));
        activityDescriptions.put("TermCollectorTerms", new ActivityDescriptor("TermCollectorTerms", "content://com.aware.provider.plugin.term_collector/plugin_term_collector_terms","term_content"));
        activityDescriptions.put("TermCollectorGeodata", new ActivityDescriptor("TermCollectorGeodata", "content://com.aware.provider.plugin.term_collector/plugin_term_collector_geodata", "term_content"));
        activityDescriptions.put("GeoCollector", new ActivityDescriptor("GeoCollector", "content://com.aware.provider.plugin.geo_collector/plugin_geo_collector_terms", "term_content"));
        activityDescriptions.put("GeonameResolver", new ActivityDescriptor("GeonameResolver", "content://com.aware.provider.plugin.geoname_resolver/plugin_geoname_resolver", "name"));
        activityDescriptions.put("OSMPoiResolver", new ActivityDescriptor("OSMPoiResolver", "content://com.aware.provider.plugin.osmpoi_resolver/plugin_osmpoi_resolver", "name"));
        activityDescriptions.put("ImageReceiver", new ActivityDescriptor("ImageReceiver", "content://com.aware.provider.plugin.image_receiver/plugin_image_receiver", "_display_name"));
        activityDescriptions.put("SMSReceiver", new ActivityDescriptor("SMSReceiver", "content://com.aware.provider.plugin.sms_receiver/plugin_sms_receiver", "SMSCONTENT"));
        activityDescriptions.put("UIContent", new ActivityDescriptor("UIContent", "content://com.aware.provider.plugin.ui_content/plugin_ui_content", "content_text"));

        mTermList.setAdapter(new ArrayAdapter<String>(this, R.layout.menu_activity_entry, activities));

        mTermList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                Log.d(TAG,"Item clicked: " +  activities[arg2] );
                ActivityDescriptor desc = activityDescriptions.get(activities[arg2]);

                String name = desc.getName();
                String uri = desc.getContentUri();
                String field = desc.getField();

                Log.d(TAG,"Putting Values into intent:");
                Log.d(TAG,"name:" + name);
                Log.d(TAG,"uri:" + uri);
                Log.d(TAG,"field:" + field);


                Intent i = new Intent(getApplicationContext(), TermViewer.class);
                i.putExtra("name", name);
                i.putExtra("uri", uri);
                i.putExtra("field", field);

                startActivity(i);

            }

        });

    }


}
