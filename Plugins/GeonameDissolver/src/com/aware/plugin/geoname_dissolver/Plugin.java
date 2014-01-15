package com.aware.plugin.geoname_dissolver;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;

import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.geoname_dissolver.GeonameDissolver_Provider.GeonameDissolver;
import com.aware.providers.Locations_Provider;
import com.aware.utils.Aware_Plugin;

import io.mingle.v1.Mingle;
import io.mingle.v1.Response;

public class Plugin extends Aware_Plugin {

	private static final String TAG = "GeonameDissolver Plugin";
	public static final String ACTION_AWARE_GEONAMEDISSOLVER = "ACTION_AWARE_GEONAMEDISSOLVER";
	
	private static long previousTimestamp = 0L;

    private static Location previousDissolvedLocation = null;

    //minimal Distance to dissolve again
    private static double minimalDistanceBetweenCoordinates = 200.0;
	
	public static Uri locationContentUri;
	private static LocationObserver locationObs = null;

	
	/**
	 * Thread manager
	 */
	private static HandlerThread threads = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "Plugin Created");
		super.onCreate();

		// Share the context back to the framework and other applications
		CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer() {
			@Override
			public void onContext() {
				Log.d(TAG, "Putting extra context into intent");
				Intent notification = new Intent(ACTION_AWARE_GEONAMEDISSOLVER);
				sendBroadcast(notification);
			}
		};

		DATABASE_TABLES = GeonameDissolver_Provider.DATABASE_TABLES;
		TABLES_FIELDS = GeonameDissolver_Provider.TABLES_FIELDS;
		CONTEXT_URIS = new Uri[] { GeonameDissolver.CONTENT_URI };

		threads = new HandlerThread(TAG);
		threads.start();

		// Set the observers, that run in independent threads, for
		// responsiveness

        locationContentUri= Uri
                .parse("content://com.aware.provider.locations/locations");
        locationObs = new LocationObserver(new Handler(
                threads.getLooper()));
        getContentResolver().registerContentObserver(
                locationContentUri, true, locationObs);

        Log.d(TAG, "notificationCatcherObs registered");

		
		Log.d(TAG, "Plugin Started");
	}

	@Override
	public void onDestroy() {

		Log.d(TAG, "Plugin is destroyed");

		super.onDestroy();
		
		getContentResolver().unregisterContentObserver(locationObs);
		

	}

	protected void saveData(Response res) {
		Log.wtf(TAG, "@saveData(Response res)");
		
		if (res != null) {
            long timestamp = System.currentTimeMillis();


            Log.wtf(TAG, "found " + res.size() + " results");

            for (int i = 0; i < res.size(); i++) {

                Log.wtf(TAG, "i: " + i);
			ContentValues rowData = new ContentValues();

            rowData.put(GeonameDissolver.DEVICE_ID, Aware.getSetting(
					getContentResolver(), Aware_Preferences.DEVICE_ID));


            // add i to produce slightly different timestamps
            rowData.put(GeonameDissolver.TIMESTAMP, timestamp + i);

			rowData.put(GeonameDissolver.NAME, res.get("result").get(i).get("name").toString());
			rowData.put(GeonameDissolver.TYPE, res.get("result").get(i).get("type").toString());
            //rowData.put(GeonameDissolver.DISTANCE, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)));

                Log.wtf(TAG, res.get("result").get(i).get("name").toString());

				Log.d(TAG, "Saving " + rowData.toString());
				getContentResolver().insert(GeonameDissolver.CONTENT_URI, rowData);

            }
		}

	}

	public class LocationObserver extends ContentObserver {
		public LocationObserver(Handler handler) {
			super(handler);
		}

        private Response PrintResponse(String info, Response resp) throws Exception{
//            Log.wtf(TAG, "Printing response for " + info);
//            Log.wtf(TAG, "GOT HEAD: \t"+resp.get("head"));
//            Log.wtf(TAG, "GOT BODY: \t"+resp.get("body"));
//            Log.wtf(TAG, "GOT TOTAL: \t"+resp.get("total"));
//            Log.wtf(TAG, "GOT TIME: \t"+resp.get("time"));
//            Log.wtf(TAG, "GOT FOUND: \t"+resp.get("found"));

            Log.wtf(TAG, info + " found " + resp.size() + " items.");

            for (int i = 0; i < resp.size(); i++) {
                Log.wtf(TAG, resp.get("result").get(i).get("name").toString());
             }



            return resp;
        }

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange GeonameDissolver");

			Cursor cursor = getContentResolver().query(
					locationContentUri, null, null, null,
                    Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");


            if (cursor != null && cursor.moveToFirst()) {

                // get lat and lon
                double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
                double lon = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));

                Log.wtf(TAG, "Lat:" + lat + " Lon:" + lon);

                Location currentLocation = new Location("");
                currentLocation.setLatitude(lat);
                currentLocation.setLatitude(lon);

                if(shouldTryToDissolve(currentLocation)){

                  // dissolve them with mingle
                    Mingle mingle;
                    Response resPOIs = null;
                    Response resGeo = null;
                    try {
                        mingle = new Mingle();

                        resPOIs = mingle.osmpois().getPoisNearbyOfRegexes((float)lat, (float)lon, 1f, "^POI.*");

                       //TODO!
                        resGeo = mingle.geonames().getPlacesNearbyOfClass((float) lat, (float) lon, 1f, "P");

                        PrintResponse("getPoisNearbyOfRegexes", resPOIs);

                        // response was not empty
                        if(resPOIs.size() > 0) {
                            previousDissolvedLocation = currentLocation;
                            // save data
                            saveData(resPOIs);
                        } else {
                            Log.wtf(TAG, "Mingle Query did not yield any Results");
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                    }
                }
            }

            if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

    private boolean shouldTryToDissolve(Location currentLocation){
      if (previousDissolvedLocation != null) {
            // we have dissolved successfully in the past
            float distBetweenLocs = previousDissolvedLocation.distanceTo(currentLocation);
            Log.wtf(TAG, "Distance between locs " + distBetweenLocs);
            return distBetweenLocs > minimalDistanceBetweenCoordinates;
        } else {
            // We haven't dissolved yet
            Log.wtf(TAG, "Should try to dissolve, as it has not resolved yet.");
            return true;
        }
    }

}
