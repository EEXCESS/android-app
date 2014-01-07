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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public final class TermViewer extends Activity
{

    public static final String TAG = "TermViewer";

    private ListView mTermList;
    

    /**
     * Called when the activity is first created. Responsible for initializing the UI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(TAG, "Activity State: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.term_viewer);
 
        mTermList = (ListView) findViewById(R.id.termList);
       

        // Populate the contact list
        populateTermList();
    }

    /**
     * Populate the contact list based on account currently selected in the account spinner.
     */
    private void populateTermList() {
        // Build adapter with contact entries
        Cursor cursor = getTerms();
        String[] fields = new String[] {
        		"term_content"
        		};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.term_entry, cursor,
                fields, new int[] {R.id.termEntryText});
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
        Uri uri = Uri.parse("content://com.aware.provider.plugin.term_collector/plugin_term_collector");
        String[] projection = new String[] {
        		"_id",
                "term_content",
                "timestamp"
        };
        String selection = "1=1";
        String[] selectionArgs = null;
        String sortOrder = "timestamp DESC LIMIT 20";

        return getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

}
