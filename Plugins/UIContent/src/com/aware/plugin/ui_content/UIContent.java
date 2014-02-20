package com.aware.plugin.ui_content;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.ui_content.UIContent_Provider.UIContents;

import java.util.HashSet;
import java.util.LinkedList;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;

public class UIContent extends AccessibilityService {

    private final static String TAG = ContextophelesConstants.TAG_UI_CONTENT +" AccessibilityService";

    @Override
    public void onCreate() {
        if (Aware.DEBUG) Log.e(TAG, "Service Created");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.e(TAG, "Event Received:" + event.getEventType());
        switch (event.getEventType()) {
            case (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED):
                Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED");

                // Only process this, if at least 15 seconds have gone by, since the user clicked on a result in the Display Activity
                // This should prevent use of content on the europeana page most of the time
                if ((CommonSettings.getLastTimeUserClickedResultItem(getContentResolver()) + 15000) < System.currentTimeMillis()) {
                    getUIContentFromNode(event.getSource());
                } else {
                    Log.d(TAG, "Ignoring UIContent, as Europeana Results have been clicked recently.");
                }
                break;
            default:
                break;
        }
    }

    private void getUIContentFromNode(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }

        while (node.getParent() != null) {
            node = node.getParent();
        }

        LinkedList<AccessibilityNodeInfo> nodesQueue = new LinkedList<AccessibilityNodeInfo>();

        nodesQueue.add(node);
        String sourceApp = node.getPackageName().toString();
        HashSet<String> nodeTexts = new HashSet<String>();

        while (!nodesQueue.isEmpty()) {
            AccessibilityNodeInfo top = nodesQueue.poll();
            if (top != null) {
                for (int i = 0; i < top.getChildCount(); i++) {
                    // if the child has children, add the child, otherwise process to keep memory footprint low
                    AccessibilityNodeInfo child = top.getChild(i);
                    if (child != null) {
                        if (child.getChildCount() > 0) {
                            nodesQueue.add(child);
                        } else {
                            CharSequence desc = child.getContentDescription();
                            if (desc != null) {
                                nodeTexts.add(desc.toString());
                            }
                        }
                    }
                }
                CharSequence desc = top.getContentDescription();
                if (desc != null) {
                    nodeTexts.add(desc.toString());
                }
            }
        }


        // Check if any of the nodes contains the word europeana
        boolean isEuropeana = false;
        for (String nodeText : nodeTexts) {
            if (nodeText.toLowerCase().contains("europeana")) {
                isEuropeana = true;
                break;
            }
        }

        int  minimalLength = CommonSettings.getMinimalUIContentLength(getContentResolver());

        // Filter, if some of the text contains europeana
        if (!isEuropeana) {
            for (String nodeText : nodeTexts) {
                // Filter out short Descriptions to improve quality of results (Button Descriptions)
                // Also,
                if ((nodeText.length() > minimalLength) && !nodeText.endsWith("Link")) {
                    saveData(sourceApp, nodeText);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }


    private void saveData(String sourceApp, String text) {
        // get information about wether text was already saved
        Cursor c = getContentResolver().query(UIContents.CONTENT_URI, null, UIContents.TEXT + " = " + DatabaseUtils.sqlEscapeString(text), null, UIContents.TIMESTAMP + " DESC LIMIT 1000");

        if (c.getCount() == 0) {
            ContentValues rowData = new ContentValues();
            rowData.put(UIContents.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
            rowData.put(UIContents.TIMESTAMP, System.currentTimeMillis());
            rowData.put(UIContents.SOURCE_APP, sourceApp);
            rowData.put(UIContents.TEXT, text);

            Log.d(TAG, "Saving " + rowData.toString());

            getContentResolver().insert(UIContents.CONTENT_URI, rowData);
        } else {
            Log.d(TAG, "Skipping saving of " + text.substring(0, CommonSettings.getMinimalUIContentLength(getContentResolver()) - 1) + "... from app " + sourceApp + " as it is already included");
        }

        if (!c.isClosed()) {
            c.close();
        }
    }
}