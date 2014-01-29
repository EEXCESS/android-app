package com.aware.plugin.browser_history;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Browser;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.browser_history.Browser_History_Provider.Browser_History_Data;
import com.aware.utils.Aware_Plugin;

/**
 * Collects browser history, notifies when user visits new webpage
 * @author Sahil Thakkar <sahilgthakkar@gmail.com>
 *
 */
public class Plugin extends Aware_Plugin {

	private static Browser_Observer browserObs = null;
	
	public static final String ACTION_AWARE_BROWSER_HISTORY = "ACTION_AWARE_BROWSER_HISTORY";
	
	/**
	 * Thread manager
	 */
	private static HandlerThread threads = null;

	@Override
	public void onCreate() {
		super.onCreate();
		
		TAG = "Browser History";
        DEBUG = Aware.getSetting(getContentResolver(), Aware_Preferences.DEBUG_FLAG).equals("true");
        
		DATABASE_TABLES = Browser_History_Provider.DATABASE_TABLES;
		TABLES_FIELDS = Browser_History_Provider.TABLES_FIELDS;
		CONTEXT_URIS = new Uri[]{ Browser_History_Data.CONTENT_URI };

		threads = new HandlerThread(TAG);
		threads.start();

		browserObs = new Browser_Observer(new Handler(threads.getLooper()));
		getContentResolver().registerContentObserver(Browser.BOOKMARKS_URI,
				true, browserObs);


		Cursor lastBrowserHistory = getContentResolver().query(Browser_History_Data.CONTENT_URI, null, null, null, 
				Browser_History_Data.TIMESTAMP + " DESC");
		if(lastBrowserHistory.getCount() == 0)
			firstTimeBrowserHistory();

		if( lastBrowserHistory != null && ! lastBrowserHistory.isClosed()) lastBrowserHistory.close();
		
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context = new Intent(ACTION_AWARE_BROWSER_HISTORY);
                sendBroadcast(context);
            }
        };
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Make sure to clean-up what was activated when the plugin is not active anymore.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		//If the plugin is terminated, make sure to terminate the browser contentobserver
		getContentResolver().unregisterContentObserver(browserObs);

		//remove background threads
		threads.quit();
	}

	public int firstTimeBrowserHistory(){
		int browserItems = 0;
		String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL, 
				Browser.BookmarkColumns.DATE};
		String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
		Cursor mCur = getContentResolver().query(Browser.BOOKMARKS_URI, proj, sel, null, null);
		
		if( DEBUG ) Log.d(TAG, "Total First Time History Size : " + ((mCur==null)?0:mCur.getCount()));

		if (mCur.moveToFirst() && mCur.getCount() > 0) {
			while (mCur.isAfterLast() == false) {
				browserItems++;
				String title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
				String url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));
				String timeDate = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.DATE));

				ContentValues data = new ContentValues();
				data.put(Browser_History_Data.TIMESTAMP, System.currentTimeMillis());
				data.put(Browser_History_Data.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
				data.put(Browser_History_Data.BROWSER_TITLE, title);
				data.put(Browser_History_Data.BROWSER_URL, url);
				data.put(Browser_History_Data.BROWSER_VISITED_TIME, timeDate);

				getContentResolver().insert(Browser_History_Data.CONTENT_URI, data);
				
				if( DEBUG ) {
				    Log.d(TAG, "mCur counter : " +browserItems);
				    Log.d(TAG, data.toString());
				}
				mCur.moveToNext();
			}
		}
		if( mCur != null && ! mCur.isClosed()) mCur.close();
		return browserItems;
	}

	public class Browser_Observer extends ContentObserver {
		public Browser_Observer(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			Cursor tempCur = null;
			String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL, 
					Browser.BookmarkColumns.DATE};
			String where = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
			Cursor lastBrowserHistory = getContentResolver().query(Browser_History_Data.CONTENT_URI, null, null, null, 
					Browser_History_Data.TIMESTAMP + " DESC LIMIT 1");

			if( DEBUG ) Log.d("TAG","Last Browser count : " + lastBrowserHistory.getCount());

			try{
				if(lastBrowserHistory.getCount() < 1){
					firstTimeBrowserHistory();
				}
				else if(lastBrowserHistory.getCount() == 1 && lastBrowserHistory.moveToFirst()){

					String lastTimeDate = lastBrowserHistory.getString(lastBrowserHistory.getColumnIndex(Browser_History_Data.BROWSER_VISITED_TIME));
					String lastTitle = lastBrowserHistory.getString(lastBrowserHistory.getColumnIndex(Browser_History_Data.BROWSER_TITLE));
					String lastUrl = lastBrowserHistory.getString(lastBrowserHistory.getColumnIndex(Browser_History_Data.BROWSER_URL));

					if( DEBUG ) {
    					Log.d("TAG","----------------------------------");
    					Log.d("TAG","Last Browser time : " + lastTimeDate);
    					Log.d("TAG","Last Browser Title : " + lastTitle);
    					Log.d("TAG","Last Browser URL : " + lastUrl);
					}
					
					where = where + " AND " + Browser.BookmarkColumns.DATE + " >= " + lastTimeDate;
					tempCur = getContentResolver().query(Browser.BOOKMARKS_URI, proj, where , null, Browser.BookmarkColumns.CREATED + " DESC");

					if (tempCur.moveToFirst() && tempCur.getCount() > 0) {
						while (tempCur.isAfterLast() == false) {

							String timeDate = tempCur.getString(tempCur.getColumnIndex(Browser.BookmarkColumns.DATE));
							String title = tempCur.getString(tempCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
							String url = tempCur.getString(tempCur.getColumnIndex(Browser.BookmarkColumns.URL));
							
							if( DEBUG ) {
    							Log.d("TAG","----------------------------------");
    							Log.d("TAG","new Browser time : " + timeDate);
    							Log.d("TAG","new Browser Title : " + title);
    							Log.d("TAG","new Browser URL : " + url);
							}
							
							if(!lastTitle.equalsIgnoreCase(title)
									&& !lastUrl.equalsIgnoreCase(url))
							{
								ContentValues data = new ContentValues();
								data.put(Browser_History_Data.TIMESTAMP, System.currentTimeMillis());
								data.put(Browser_History_Data.DEVICE_ID, Aware.getSetting(getContentResolver(), Aware_Preferences.DEVICE_ID));
								data.put(Browser_History_Data.BROWSER_TITLE, title);
								data.put(Browser_History_Data.BROWSER_URL, url);
								data.put(Browser_History_Data.BROWSER_VISITED_TIME, timeDate);

								getContentResolver().insert(Browser_History_Data.CONTENT_URI, data);
								
								if( DEBUG ) Log.d(TAG, data.toString());
								
								CONTEXT_PRODUCER.onContext();
							}
							tempCur.moveToNext();
						}
					}
				}
			}
			catch (Exception e) {
				if( DEBUG ) Log.d("TAG","EXCEPTION : " + e.getMessage());
			}
			finally{
				if( tempCur != null && ! tempCur.isClosed()) tempCur.close();
				if( lastBrowserHistory != null && ! lastBrowserHistory.isClosed()) lastBrowserHistory.close();
			}
		}
	}
}