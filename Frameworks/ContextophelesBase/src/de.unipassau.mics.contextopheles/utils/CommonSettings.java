package de.unipassau.mics.contextopheles.utils;


import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.aware.Aware;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

public class CommonSettings {
    private static final String TAG = "CommonSettings";

    public static boolean getBooleanFromAwareSettings(ContentResolver resolver,  String settingsIdentifier, boolean defaultValue) {
        String booleanString = Aware.getSetting(resolver, settingsIdentifier);
        if (booleanString != null && !booleanString.equals("")) {
            try {
                return Boolean.parseBoolean(booleanString);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static int getIntegerFromAwareSettings(ContentResolver resolver,  String settingsIdentifier, int defaultValue) {
        String integerString = Aware.getSetting(resolver, settingsIdentifier);
        if (integerString != null) {
            try {
                return Integer.parseInt(integerString);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static long getLongFromAwareSettings(ContentResolver resolver,  String settingsIdentifier, long defaultValue) {
        String longString = Aware.getSetting(resolver, settingsIdentifier);
        if (longString != null) {
            try {
                return Long.parseLong(longString);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static float getFloatFromAwareSettings(ContentResolver resolver,  String settingsIdentifier, float defaultValue) {
        String floatString = Aware.getSetting(resolver, settingsIdentifier);
        if (floatString  != null) {
            try {
                return Float.parseFloat(floatString);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static double getDoubleFromAwareSettings(ContentResolver resolver,  String settingsIdentifier, double defaultValue) {
        String doubleString = Aware.getSetting(resolver, settingsIdentifier);
        if (doubleString != null) {
            try {
                return Double.parseDouble(doubleString);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static boolean getNotificationUsesVibration(ContentResolver resolver) {

        return getBooleanFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_AQ_NOTIFICATION_VIBRATE, ContextophelesConstants.SETTINGS_AQ_NOTIFICATION_VIBRATE_DEFAULT);
    }

    public static void setNotificationUsesVibration(ContentResolver resolver, Boolean value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_AQ_NOTIFICATION_VIBRATE, value.toString());
    }

    public static boolean getNotificationUsesSound(ContentResolver resolver) {
        return  getBooleanFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_AQ_NOTIFICATION_SOUND, ContextophelesConstants.SETTINGS_AQ_NOTIFICATION_SOUND_DEFAULT);
    }

    public static void setNotificationUsesSound(ContentResolver resolver, Boolean value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_AQ_NOTIFICATION_SOUND, value.toString());
    }

    public static int getMinimumNumberOfResultsToDisplayNotification(ContentResolver resolver) {
        return getIntegerFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_AQ_MINIMUM_NUMBER_OF_RESULTS_TO_DISPLAY_NOTIFICATION, ContextophelesConstants.SETTINGS_AQ_MINIMUM_NUMBER_OF_RESULTS_TO_DISPLAY_NOTIFICATION_DEFAULT);
    }

    public static void setMinimumNumberOfResultsToDisplayNotification(ContentResolver resolver, Integer value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_AQ_MINIMUM_NUMBER_OF_RESULTS_TO_DISPLAY_NOTIFICATION, value.toString());
    }


    public static long getTimeOfLastSuccessfulQuery(ContentResolver resolver) {
        return getLongFromAwareSettings(resolver, ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY, ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY_DEFAULT);
    }

    public static void setTimeOfLastSuccessfulQuery(ContentResolver resolver, Long value) {
        Aware.setSetting(resolver, ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY, value.toString());
    }


    public static long getLastTimeUserClickedResultItem(ContentResolver resolver) {
        return getLongFromAwareSettings(resolver, ContextophelesConstants.INFO_AQ_LAST_TIME_USER_CLICKED_RESULTITEM, ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY_DEFAULT);
    }

    public static void setLastTimeUserClickedResultItem(ContentResolver resolver, Long value) {
        Aware.setSetting(resolver, ContextophelesConstants.INFO_AQ_LAST_TIME_USER_CLICKED_RESULTITEM, value.toString());
    }


    public static long getEndOfDoNotDisturb(ContentResolver resolver) {
        return getLongFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_AQ_END_OF_DND, ContextophelesConstants.SETTINGS_AQ_END_OF_DND_DEFAULT);
    }

    public static void setEndOfDoNotDisturb(ContentResolver resolver, Long value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_AQ_END_OF_DND, value.toString());
    }

    public static boolean getQueryUseOfLocation(ContentResolver resolver) {
        return getBooleanFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_AQ_USE_LOCATION, ContextophelesConstants.SETTINGS_AQ_USE_LOCATION_DEFAULT);
    }

    public static void setQueryUseOfLocation(ContentResolver resolver, Boolean value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_AQ_USE_LOCATION, value.toString());
    }

    public static float getGeonameDistance(ContentResolver resolver) {
        return getFloatFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_GR_DISTANCE, ContextophelesConstants.SETTINGS_GR_DISTANCE_DEFAULT);
    }

    public static void setGeonameDistance(ContentResolver resolver, Float value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_GR_DISTANCE, value.toString());
    }

    public static void setGeonameDistanceFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_GR_DISTANCE, value);
    }

    public static int getGeonameDistanceSeekBarProgress(ContentResolver resolver) {
        return getIntegerFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_GR_DISTANCE_SEEKBAR_PROGRESS, ContextophelesConstants.SETTINGS_GR_DISTANCE_SEEKBAR_PROGRESS_DEFAULT);
    }

    public static void setGeonameDistanceSeekBarProgress(ContentResolver resolver, Integer value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_GR_DISTANCE_SEEKBAR_PROGRESS, value.toString());
    }

    public static float getOSMPoiDistance(ContentResolver resolver) {
        return getFloatFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_OR_DISTANCE, ContextophelesConstants.SETTINGS_OR_DISTANCE_DEFAULT);
    }

    public static void setOSMPoiDistance(ContentResolver resolver, Float value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_OR_DISTANCE, value.toString());
    }

    public static void setOSMPoiDistanceFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_OR_DISTANCE, value);
    }

    public static int getOSMPoiDistanceSeekBarProgress(ContentResolver resolver) {
        return getIntegerFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_OR_DISTANCE_SEEKBAR_PROGRESS, ContextophelesConstants.SETTINGS_OR_DISTANCE_SEEKBAR_PROGRESS_DEFAULT);
    }

    public static void setOSMPoiDistanceSeekBarProgress(ContentResolver resolver, Integer value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_OR_DISTANCE_SEEKBAR_PROGRESS, value.toString());
    }

    public static int getGeonameMinimalDistanceBetweenPositions(ContentResolver resolver) {
        return getIntegerFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_GR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS, ContextophelesConstants.SETTINGS_GR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS_DEFAULT);
    }

    public static void setGeonameMinimalDistanceBetweenPositions(ContentResolver resolver, Integer value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_GR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS, value.toString());
    }

    public static void setGeonameMinimalDistanceBetweenPositionsFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_GR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS, value);
    }

    public static int getOSMPoiMinimalDistanceBetweenPositions(ContentResolver resolver) {
        return getIntegerFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS, ContextophelesConstants.SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS_DEFAULT);
    }

    public static void setOSMPoiMinimalDistanceBetweenPositions(ContentResolver resolver, Integer value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS, value.toString());
    }

    public static void setOSMPoiMinimalDistanceBetweenPositionsFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS, value);
    }

    public static boolean getUseFakeLocation(ContentResolver resolver) {
        return getBooleanFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_USE_FAKE_LOCATION, ContextophelesConstants.SETTINGS_USE_FAKE_LOCATION_DEFAULT);
    }

    public static void setUseFakeLocation(ContentResolver resolver, Boolean value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_USE_FAKE_LOCATION, value.toString());
    }

    public static void setFakeLongitude(ContentResolver resolver, Double value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_FAKE_LONGITUDE, value.toString());
    }


    public static void setFakeLongitudeFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_FAKE_LONGITUDE, value);
    }

    public static double getFakeLongitude(ContentResolver resolver) {
        return getDoubleFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_FAKE_LONGITUDE, ContextophelesConstants.SETTINGS_FAKE_LONGITUDE_DEFAULT);
    }

    public static void setFakeLatitude(ContentResolver resolver, Double value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_FAKE_LATITUDE, value.toString());
    }

    public static void setFakeLatitudeFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_FAKE_LATITUDE, value);
    }

    public static double getFakeLatitude(ContentResolver resolver) {
        return getDoubleFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_FAKE_LATITUDE, ContextophelesConstants.SETTINGS_FAKE_LATITUDE_DEFAULT);
    }

    public static int getMinimalUIContentLength(ContentResolver resolver) {
        return getIntegerFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_UI_MINIMAL_CONTENT_LENGTH, ContextophelesConstants.SETTINGS_UI_MINIMAL_CONTENT_LENGTH_DEFAULT);
    }

    public static void setMinimalUIContentLength(ContentResolver resolver, Integer value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_UI_MINIMAL_CONTENT_LENGTH, value.toString());
    }

    public static void setMinimalUIContentLengthFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_UI_MINIMAL_CONTENT_LENGTH, value);
    }

    public static int getQueryListWearOffTime(ContentResolver resolver) {
        return getIntegerFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_AQ_QUERYLIST_WEAROFF_TIME, ContextophelesConstants.SETTINGS_AQ_QUERYLIST_WEAROFF_TIME_DEFAULT);
    }

    public static void setQueryListWearOffTime(ContentResolver resolver, Integer value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_AQ_QUERYLIST_WEAROFF_TIME, value.toString());
    }

    public static void setQueryListWearOffTimeFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_AQ_QUERYLIST_WEAROFF_TIME, value);
    }


    public static int getMinimalTermCollectorTokenLength(ContentResolver resolver) {
        return getIntegerFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_TC_MINIMAL_TOKEN_LENGTH, ContextophelesConstants.SETTINGS_TC_MINIMAL_TOKEN_LENGTH_DEFAULT);
    }

    public static void setMinimalTermCollectorTokenLength(ContentResolver resolver, Integer value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_TC_MINIMAL_TOKEN_LENGTH, value.toString());
    }

    public static void setMinimalTermCollectorTokenLengthFromString(ContentResolver resolver, String value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_TC_MINIMAL_TOKEN_LENGTH, value);
    }


    public static boolean getTermCollectorApplyStopwords(ContentResolver resolver) {
        return getBooleanFromAwareSettings(resolver, ContextophelesConstants.SETTINGS_TC_APPLY_STOPWORDS, ContextophelesConstants.SETTINGS_TC_APPLY_STOPWORDS_DEFAULT);
    }

    public static void setTermCollectorApplyStopwords(ContentResolver resolver, Boolean value) {
        Aware.setSetting(resolver, ContextophelesConstants.SETTINGS_TC_APPLY_STOPWORDS, value.toString());
    }





    public static int getCountForUri(ContentResolver resolver, Uri uri) {
        Cursor countCursor = resolver.query(uri,
                new String[] {"count(*) AS count"},
                null,
                null,
                null);

        try{
            countCursor.moveToFirst();
            return countCursor.getInt(0);
        } catch (NullPointerException e) {
            return 0;
        }

    }

    public static void cleanDataForUri(ContentResolver resolver, Uri uri){
        Log.d(TAG, "Trying to delete all Data.");
        resolver.delete(uri, " 1 = 1 ", null);
        Log.d(TAG, "Deletion done.");
    }

}