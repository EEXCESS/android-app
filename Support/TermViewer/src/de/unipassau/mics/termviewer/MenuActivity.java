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

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

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

        final HashMap<String, ActivityDescriptor> activityDescriptions = new HashMap<String, ActivityDescriptor>();

        activityDescriptions.put("ClipboardCatcher", new ActivityDescriptor("ClipboardCatcher", ContextophelesConstants.CLIPBOARD_CATCHER_CONTENT_URI.toString(), ContextophelesConstants.CLIPBOARD_CATCHER_FIELD_CLIPBOARDCONTENT));
        activityDescriptions.put("TermCollectorTerms", new ActivityDescriptor("TermCollectorTerms", ContextophelesConstants.TERM_COLLECTOR_TERM_CONTENT_URI.toString(), ContextophelesConstants.TERM_COLLECTOR_CACHE_FIELD_TERM_CONTENT));
        activityDescriptions.put("TermCollectorGeodata", new ActivityDescriptor("TermCollectorGeodata", ContextophelesConstants.TERM_COLLECTOR_GEODATA_CONTENT_URI.toString(), ContextophelesConstants.TERM_COLLECTOR_GEODATA_FIELD_TERM_CONTENT));
        activityDescriptions.put("GeoCollector", new ActivityDescriptor("GeoCollector", ContextophelesConstants.GEO_COLLECTOR_CONTENT_URI.toString(), ContextophelesConstants.GEO_COLLECTOR_FIELD_TERM_CONTENT));
        activityDescriptions.put("GeonameResolver", new ActivityDescriptor("GeonameResolver", ContextophelesConstants.GEONAME_RESOLVER_CONTENT_URI.toString(), ContextophelesConstants.GEONAME_RESOLVER_FIELD_NAME));
        activityDescriptions.put("OSMPoiResolver", new ActivityDescriptor("OSMPoiResolver", ContextophelesConstants.OSMPOI_RESOLVER_CONTENT_URI.toString(), ContextophelesConstants.OSMPOI_RESOLVER_FIELD_NAME));
        activityDescriptions.put("ImageReceiver", new ActivityDescriptor("ImageReceiver", ContextophelesConstants.IMAGE_RECEIVER_CONTENT_URI.toString(), ContextophelesConstants.IMAGE_RECEIVER_FIELD_DISPLAY_NAME));
        activityDescriptions.put("SMSReceiver", new ActivityDescriptor("SMSReceiver", ContextophelesConstants.SMS_RECEIVER_CONTENT_URI.toString(), ContextophelesConstants.SMS_RECEIVER_FIELD_SMSContent));
        activityDescriptions.put("UIContent", new ActivityDescriptor("UIContent", ContextophelesConstants.UI_CONTENT_CONTENT_URI.toString(), ContextophelesConstants.UI_CONTENT_FIELD_TEXT));


        final String[] activities = new String[activityDescriptions.keySet().size()];
        activityDescriptions.keySet().toArray(activities);

        mTermList.setAdapter(new ArrayAdapter<String>(this, R.layout.menu_activity_entry, activities));

        mTermList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
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
