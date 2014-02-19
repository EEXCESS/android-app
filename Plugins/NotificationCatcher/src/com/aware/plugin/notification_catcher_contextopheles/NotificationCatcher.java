package com.aware.plugin.notification_catcher_contextopheles;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.ContentValues;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.notification_catcher_contextopheles.NotificationCatcher_Provider.Notifications;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

public class NotificationCatcher extends AccessibilityService {		

	final String TAG = ContextophelesConstants.TAG_NOTIFICATION_CATCHER;
    private boolean isInit = false;
	
	@Override
    public void onCreate() {
		if(Aware.DEBUG) Log.e(TAG,"Service Created");
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
	    if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
	        List<CharSequence> textList = event.getText();
	        
	        String outputText = "";
	        for(int i=0;i < textList.size();i++){
	        	outputText = outputText + ";" + textList.get(i);
            }

            Log.e(TAG, "Notification Text: " + outputText);

	        Notification notification = (Notification) event.getParcelableData();
	        
	        if(notification != null) {
	        
		        RemoteViews views = notification.contentView;
		        Class secretClass = views.getClass();
	
		        try {
		            Map<Integer, String> text = new HashMap<Integer, String>();
	
		            Field outerFields[] = secretClass.getDeclaredFields();
		            for (int i = 0; i < outerFields.length; i++) {

		                if (!outerFields[i].getName().equals("mActions")) continue;
	
		                outerFields[i].setAccessible(true);
	
		                ArrayList<Object> actions = (ArrayList<Object>) outerFields[i].get(views);
		                for (Object action : actions) {

		                    Field innerFields[] = action.getClass().getDeclaredFields();

                            // Additionaly, we need to get the declared fields of the suoerclass, to also get viewId
                            Field innerFieldsParent[] = action.getClass().getSuperclass().getDeclaredFields();

		                    Object value = null;
		                    Integer type = null;
		                    Integer viewId = null;

		                     for (Field field : innerFieldsParent) {
		                        field.setAccessible(true);
		                        if (field.getName().equals("value")) {
		                            value = field.get(action);
		                        } else if (field.getName().equals("type")) {
		                            type = field.getInt(action);
		                        } else if (field.getName().equals("viewId")) {
		                            viewId = field.getInt(action);
		                        }
		                    }

                            for (Field field : innerFields) {
                                field.setAccessible(true);
                                if (field.getName().equals("value")) {
                                    value = field.get(action);
                                } else if (field.getName().equals("type")) {
                                    type = field.getInt(action);
                                } else if (field.getName().equals("viewId")) {
                                    viewId = field.getInt(action);
                                }
                            }



		                    if (type != null && (type == 9 || type == 10)) {
		                            text.put(viewId, value.toString());
		                    }
		                }

		                
		                ContentValues rowData = new ContentValues();
                        rowData.put(Notifications.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
                        rowData.put(Notifications.TIMESTAMP, System.currentTimeMillis());
                        rowData.put(Notifications.APP_NAME, (String)event.getPackageName());

                        // These values are a hack, please contact me if they break
                        String notificationTitle = text.get(16908310);
                        rowData.put(Notifications.TITLE, notificationTitle);
                        String notificationText = text.get(16908358);
                        rowData.put(Notifications.TEXT, notificationText);
                        
                        getContentResolver().insert(Notifications.CONTENT_URI, rowData);
                        Log.d(TAG, "Saving " + rowData.toString());
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }		        		        
	        }
	    }
	}

    @Override
	protected void onServiceConnected() {
		
		if (isInit) {
	        return;
	    }
	    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
	    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
	    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
	    setServiceInfo(info);
	    isInit = true;
	    
	}
	
	@Override
	public void onInterrupt() {
	    isInit = false;
	}


}