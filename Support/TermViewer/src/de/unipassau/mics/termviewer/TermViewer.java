/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unipassau.mics.termviewer;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.ListView;


public final class TermViewer extends Activity
{

    public static final String TAG = "TermViewer";

    private String name = null;
    private String uri = null;
    private String field = null;

    private ListView mTermList;

    private TimeStampAdapter  adapter;


    private TermViewerContentObserver termViewerContentObserver;
    /**
     * Thread manager
     */
    private static HandlerThread threads = null;

    /**
     * Called when the activity is first created. Responsible for initializing the UI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(TAG, "Activity State: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.term_viewer);

        threads = new HandlerThread(TAG);
        threads.start();


        name = this.getIntent().getStringExtra("name");
        uri = this.getIntent().getStringExtra("uri");
        field = this.getIntent().getStringExtra("field");

        getActionBar().setTitle("AWARE: " + name);
        // Set the observers, that run in independent threads, for
        // responsiveness
        termViewerContentObserver= new TermViewerContentObserver(new Handler(
                threads.getLooper()));

        getContentResolver().registerContentObserver(Uri.parse(uri), true, termViewerContentObserver);

        Log.d(TAG, "termViewerContentObserver registered");

        mTermList = (ListView) findViewById(R.id.termList);


        // Populate the contact list
        populateTermList();
    }

    @Override
    public void onDestroy() {


        super.onDestroy();

        getContentResolver().unregisterContentObserver(termViewerContentObserver);

    }

    /**
     * Populate the contact list based on account currently selected in the account spinner.
     */
    private void populateTermList() {
        // Build adapter with contact entries

                Cursor cursor = getTerms();

                adapter = new TimeStampAdapter(getApplicationContext(), cursor, field);

                mTermList.setAdapter(adapter);

    }

    /**
     * Obtains the contact list for the currently selected account.
     *
     * @return A cursor for for accessing the contact list.
     */
    private Cursor getTerms()
    {
        // Run query
        Uri uri = Uri.parse(this.uri);
        String[] projection = new String[] {
        		"_id",
                field,
                "timestamp"
        };
        String selection = "1=1";
        String[] selectionArgs = null;
        String sortOrder = "timestamp DESC LIMIT 20";

        return getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public class TermViewerContentObserver extends ContentObserver {
        public TermViewerContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.wtf(TAG, "@TermViewerContentObserver: Refreshing ListView");


            new Thread(new Runnable(){

                public void run()
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            populateTermList();
                        }
                    });

                }
            }).start();
        }

    }
}


