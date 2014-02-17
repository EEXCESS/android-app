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

/**
 * Service to process notifications for the UIContent
 *
 * @author Christian Koehler
 * @email: ckoehler@andrew.cmu.edu
 */

public class UIContent extends AccessibilityService {

    final String TAG = "UIContent";
    private boolean isInit = false;

    @Override
    public void onCreate() {
        if (Aware.DEBUG) Log.e(TAG, "Service Created");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.e(TAG, "Event Received:" + event.getEventType());
        switch (event.getEventType()) {
            case (AccessibilityEvent.TYPE_VIEW_CLICKED):
                //Log.d(TAG, "TYPE_VIEW_CLICKED");
                break;
            case (AccessibilityEvent.TYPE_VIEW_LONG_CLICKED):
                //Log.d(TAG, "TYPE_VIEW_LONG_CLICKED");
                break;
            case (AccessibilityEvent.TYPE_VIEW_SELECTED):
                // Log.d(TAG, "TYPE_VIEW_SELECTED");
                // Log.d(TAG, "Source: " + event.getSource());

                break;
            case (AccessibilityEvent.TYPE_VIEW_FOCUSED):
                //Log.d(TAG, "TYPE_VIEW_FOCUSED");
                //  getUIContentFromNode(event.getSource());
                break;
            case (AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED):
                //Log.d(TAG, "TYPE_VIEW_TEXT_CHANGED");
                break;
            case (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED):
                //Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED");
                //getUIContentFromNode(event.getSource());
                break;
            case (AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED):
                //Log.d(TAG, "TYPE_NOTIFICATION_STATE_CHANGED");
                break;
            case (AccessibilityEvent.TYPE_VIEW_HOVER_ENTER):
                //Log.d(TAG, "TYPE_VIEW_HOVER_ENTER");
                break;
            case (AccessibilityEvent.TYPE_VIEW_HOVER_EXIT):
                //Log.d(TAG, "TYPE_VIEW_HOVER_EXIT");
                break;
            case (AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START):
                //Log.d(TAG, "TYPE_TOUCH_EXPLORATION_GESTURE_START");
                break;
            case (AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END):
                //Log.d(TAG, "TYPE_TOUCH_EXPLORATION_GESTURE_END");
                break;
            case (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED):
                Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED");

                // Only process this, if at least 15 seconds have gone by, since the user clicked on a result in the Display Activity
                // This should prevent use of content on the europeana page most of the time
                if ((getLastTimeUserClickedResult() + 15000) < System.currentTimeMillis()) {
                    getUIContentFromNode(event.getSource());
                } else {
                    Log.d(TAG, "Ignoring UIContent, as Europeana Results have been clicked recently." + getLastTimeUserClickedResult() + 15000 + " / " + System.currentTimeMillis());
                }
                break;
            case (AccessibilityEvent.TYPE_VIEW_SCROLLED):
                // Log.d(TAG, "TYPE_VIEW_SCROLLED");
                //  getUIContentFromNode(event.getSource());
                break;
            case (AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED):
                //only called in textfields
                //Log.d(TAG, "TYPE_VIEW_TEXT_SELECTION_CHANGED: " + event.getText());
                break;
            case (AccessibilityEvent.TYPE_ANNOUNCEMENT):
                //Log.d(TAG, "TYPE_ANNOUNCEMENT");
                break;
            case (AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY):
                //Log.d(TAG, "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY");
                break;
            case (AccessibilityEvent.TYPE_GESTURE_DETECTION_START):
                //Log.d(TAG, "TYPE_GESTURE_DETECTION_START");
                break;
            case (AccessibilityEvent.TYPE_GESTURE_DETECTION_END):
                //Log.d(TAG, "TYPE_GESTURE_DETECTION_END");
                break;
            case (AccessibilityEvent.TYPE_TOUCH_INTERACTION_START):
                //Log.d(TAG, "TYPE_TOUCH_INTERACTION_START");
                break;
            case (AccessibilityEvent.TYPE_TOUCH_INTERACTION_END):
                //Log.d(TAG, "TYPE_TOUCH_INTERACTION_END");
                break;
            default:
                break;
        }
    }

    private void getUIContentFromNode(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        int level = 0;

        while (node.getParent() != null) {
            node = node.getParent();
            level++;
        }

        //Log.d(TAG, "Root-Child-Count: " +  node.getChildCount());

        LinkedList<AccessibilityNodeInfo> nodesQueue = new LinkedList<AccessibilityNodeInfo>();

        nodesQueue.add(node);
        String sourceApp = node.getPackageName().toString();
        HashSet<String> nodeTexts = new HashSet<String>();

        while (!nodesQueue.isEmpty()) {
            AccessibilityNodeInfo top = nodesQueue.poll();
            if (top != null) {
                for (int i = 0; top != null && i < top.getChildCount(); i++) {
                    // if the child has children, add the child, otherwise process to keep memory footprint low
                    AccessibilityNodeInfo child = top.getChild(i);
                    if (top != null && child != null) {
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

        boolean isEuropeana = false;

        for (String nodeText : nodeTexts) {
            if (nodeText.toLowerCase().contains("europeana")) {
                isEuropeana = false;
            }
        }

        // Filter, if some of the text contains europeana
        if (!isEuropeana) {
            for (String nodeText : nodeTexts) {
                // Filter out short Descriptions to improve quality of results (Button Descriptions)
                // Also, filter Description of Links
                if (nodeText.length() > 20 && !nodeText.endsWith("Link")) {
                    saveData(sourceApp, nodeText);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        isInit = false;
    }


    private void saveData(String sourceApp, String text) {
        // get information if text was already saved
        Cursor c = getContentResolver().query(UIContents.CONTENT_URI, null, UIContents.TEXT + " = " + DatabaseUtils.sqlEscapeString(text), null, "timestamp" + " DESC LIMIT 1000");
        // UIContents.TEXT + " = " + DatabaseUtils.sqlEscapeString(text), null, "timestamp" + " DESC LIMIT 1000"

        if (c.getCount() == 0) {
            Log.d(TAG, "Saving " + text + " from app " + sourceApp);
            ContentValues rowData = new ContentValues();
            rowData.put(UIContents.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
            rowData.put(UIContents.TIMESTAMP, System.currentTimeMillis());
            rowData.put(UIContents.SOURCE_APP, sourceApp);
            rowData.put(UIContents.TEXT, text);

            getContentResolver().insert(UIContents.CONTENT_URI, rowData);
        } else {
            Log.d(TAG, "Skipping saving of " + text.substring(0, 19) + "... from app " + sourceApp + " as it is already included");
        }

        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    public long getLastTimeUserClickedResult() {
        String lastTimeString = Aware.getSetting(getContentResolver(), "AWARE_LAST_TIME_USER_CLICKED_RESULTITEM");
        if (lastTimeString != null) {
            try {
                return Long.parseLong(lastTimeString);
            } catch (NumberFormatException e) {
                return 0L;
            }
        } else {
            return 0L;
        }
    }
}