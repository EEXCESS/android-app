package com.aware.plugin.geoname_resolver;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.geoname_resolver.GeonameResolver_Provider.GeonameResolver;
import com.aware.providers.Locations_Provider;
import com.aware.utils.Aware_Plugin;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;
import io.mingle.v1.Mingle;
import io.mingle.v1.Response;

public class Plugin extends Aware_Plugin {

	private static final String TAG = ContextophelesConstants.TAG_GEONAME_RESOLVER + " Plugin";
	public static final String ACTION_AWARE_GEONAMERESOLVER = "ACTION_AWARE_GEONAMERESOLVER";
	
	private static long previousTimestamp = 0L;

    private static Location previousDissolvedLocation = null;

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
				Intent notification = new Intent(ACTION_AWARE_GEONAMERESOLVER);
				sendBroadcast(notification);
			}
		};

		DATABASE_TABLES = GeonameResolver_Provider.DATABASE_TABLES;
		TABLES_FIELDS = GeonameResolver_Provider.TABLES_FIELDS;
		CONTEXT_URIS = new Uri[] { GeonameResolver.CONTENT_URI };

		threads = new HandlerThread(TAG);
		threads.start();

		// Set the observers, that run in independent threads, for
		// responsiveness

        locationContentUri= Uri.parse(ContextophelesConstants.LOCATION_URI);
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

        getContentResolver().unregisterContentObserver(locationObs);

        super.onDestroy();
	}

	protected void saveData(Response res) {

		if (res != null) {
            long timestamp = System.currentTimeMillis();

            for (int i = 0; i < res.size(); i++) {

			ContentValues rowData = new ContentValues();

            rowData.put(GeonameResolver.DEVICE_ID, Aware.getSetting(
					getContentResolver(), Aware_Preferences.DEVICE_ID));


            // add i to produce slightly different timestamps
            rowData.put(GeonameResolver.TIMESTAMP, timestamp + i);

			rowData.put(GeonameResolver.NAME, res.get("result").get(i).get("name").toString());

				Log.d(TAG, "Saving " + rowData.toString());
				getContentResolver().insert(GeonameResolver.CONTENT_URI, rowData);

            }
		}

	}

	public class LocationObserver extends ContentObserver {
		public LocationObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d(TAG, "@onChange GeonameResolver");

			Cursor cursor = getContentResolver().query(
					locationContentUri, null, null, null,
                    Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");

            if (cursor != null && cursor.moveToFirst()) {

                // get lat and lon


                double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
                double lon = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));

                if(CommonSettings.getUseFakeLocation(getContentResolver())){
                    lat = CommonSettings.getFakeLatitude(getContentResolver());
                    lon = CommonSettings.getFakeLongitude(getContentResolver());
                }

                if (lat > 90) {lat = 90;}
                if (lat < -90) {lat = -90;}
                if (lon > 180) {lon = 180;}
                if (lon < -180) {lon = -180;}

                Log.d(TAG, "Lat:" + lat + " Lon:" + lon);

                Location currentLocation = new Location("");
                currentLocation.setLatitude(lat);
                currentLocation.setLongitude(lon);

                if(shouldTryToDissolve(currentLocation)){
                    dissolveLocation(currentLocation);
                }
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        private boolean shouldTryToDissolve(Location currentLocation){
            if (previousDissolvedLocation != null) {
                // we have dissolved successfully in the past
                float distBetweenLocs = previousDissolvedLocation.distanceTo(currentLocation);
                return distBetweenLocs > CommonSettings.getGeonameMinimalDistanceBetweenPositions(getContentResolver());
            } else {
                // We haven't dissolved yet
                Log.d(TAG, "Should try to dissolve, as it has not resolved yet.");
                return true;
            }
        }

        private void dissolveLocation(Location currentLocation) {
            // dissolve currentLocation with mingle
            Mingle mingle;
            Response resPlaces = null;

            try {
                mingle = new Mingle(getApplicationContext());
                resPlaces = mingle.geonames().getPlacesNearbyOfClass((float)currentLocation.getLatitude(), (float)currentLocation.getLongitude(), CommonSettings.getGeonameDistance(getContentResolver()), "P");

                // response was not empty
                if(resPlaces.size() > 0) {
                    previousDissolvedLocation = currentLocation;
                    // save data
                    saveData(resPlaces);
                } else {
                    Log.d(TAG, "Mingle Query did not yield any Results");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


	}

    }
}
