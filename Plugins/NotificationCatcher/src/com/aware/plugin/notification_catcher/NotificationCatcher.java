package com.aware.plugin.notification_catcher;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.notification_catcher.NotificationCatcher_Provider.Hashed_Contacts;
import com.aware.plugin.notification_catcher.NotificationCatcher_Provider.Notifications;
import com.aware.utils.Encrypter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Service to process notifications for the NotificationCatcher
 * @author Christian Koehler
 * @email: ckoehler@andrew.cmu.edu
 *
 */

public class NotificationCatcher extends AccessibilityService {		

	final String TAG = "NotificationCatcher";
    private boolean isInit = false;
	
	@Override
    public void onCreate() {
		if(Aware.DEBUG) Log.e(TAG,"Service Created");
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		
		Log.e(TAG,"Event Received");

		
	    if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
	        	        
	    	Log.e(TAG,"Notification Intercepted");
	    	
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
                                Log.e(TAG, "Type: " + type);
                                Log.e(TAG, "viewID: " + viewId);
                                Log.e(TAG, "value: " + value.toString());
		                            text.put(viewId, value.toString());
		                    }
		                }

		                
		                ContentValues rowData = new ContentValues();
                        rowData.put(Notifications.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
                        rowData.put(Notifications.TIMESTAMP, System.currentTimeMillis());
                        rowData.put(Notifications.APP_NAME, (String)event.getPackageName());
                        rowData.put(Notifications.CONTACT_ID, getContactID(text.get(16908358)));
                        
                        // These values are a hack, please contact me if they break
                        String notificationTitle = text.get(16908310);
                        rowData.put(Notifications.TITLE, notificationTitle);
                        String notificationText = text.get(16908358);
                        rowData.put(Notifications.TEXT, notificationText);
                        
                        getContentResolver().insert(Notifications.CONTENT_URI, rowData);
                        
                    	String output = "DeviceID: " + Aware_Preferences.DEVICE_ID + "\n";
                        output = output + "Timestamp: " + System.currentTimeMillis() + "\n";
                        output = output + "App Name: " + (String)event.getPackageName() + "\n";
                        output = output + "Title: " + text.get(16908310) + "\n";
                        output = output + "Text: " + text.get(16908358);
                    	
                    	Log.e(TAG,output);
                                                                                                                                                
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }		        		        
	        }
	    }
	}
	
	private int getContactID(String notification_text) {
		
		int contact_id = -1;
		
		Vector<String> contact_names = new Vector<String>(0,1);
		Vector<String> contact_names_alt = new Vector<String>(0,1);
		
		Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		 
        if (null != c) {        	
        	try {
        		if (c.moveToFirst()) {
        			
        			do {
        				
        				contact_names.add(c.getString(c.getColumnIndex("display_name")));
        				contact_names_alt.add(c.getString(c.getColumnIndex("display_name_alt")));

        			} while (c.moveToNext());
        		}
			} finally {
				c.close(); // always close cursors!
			}
		}
		
        String contact_contained = "";
        String contact_alt_contained = "";
        
        for(int i=0;i<contact_names.size();i++) {
        	if( notification_text.contains(contact_names.get(i)))
        		contact_contained = contact_names.get(i);
        		
        	if( notification_text.contains(contact_names_alt.get(i)))
        		contact_alt_contained = contact_names_alt.get(i);
        }
        
        if(!contact_contained.equals("") || !contact_alt_contained.equals("")) {
        	String[] selection_args = new String[2];
        	selection_args[0] = Encrypter.hashSHA1(contact_contained);
        	selection_args[1] = Encrypter.hashSHA1(contact_alt_contained);
        	String [] return_args = {"_id"};
        	
        	Cursor contacts_hash = getContentResolver().query(Hashed_Contacts.CONTENT_URI, return_args, "contact_hash=? OR contact_alt_hash=?", selection_args, null);
        	
        	if( contacts_hash != null) {
        		try {
        			if(contacts_hash.moveToFirst()) {
        				do {
        					contact_id = contacts_hash.getInt(contacts_hash.getColumnIndex("_id"));
        				} while(contacts_hash.moveToNext());
        			}
        		} finally {
        			contacts_hash.close();
        		}
        	}
        	
        	if( contact_id == -1) {
        		ContentValues rowData = new ContentValues();
        		rowData.put(Hashed_Contacts.CONTACT_HASH, selection_args[0]);
        		rowData.put(Hashed_Contacts.CONTACT_ALT_HASH, selection_args[1]);
        		rowData.put(Hashed_Contacts.TIMESTAMP, System.currentTimeMillis());
        		
        		Uri tmpUri = getContentResolver().insert(Hashed_Contacts.CONTENT_URI, rowData);
        		Log.e(TAG,tmpUri.toString());
        	}
        }
        
		return contact_id;
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