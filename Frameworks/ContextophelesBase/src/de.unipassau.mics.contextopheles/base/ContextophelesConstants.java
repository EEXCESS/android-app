package de.unipassau.mics.contextopheles.base;

import android.net.Uri;

public class ContextophelesConstants {
    // Tags
    public static final String TAG_AUTOMATIC_QUERY = "AutomaticQuery";
    public static final String TAG_CLIPBOARD_CATCHER = "ClipboardCatcher";
    public static final String TAG_GEONAME_RESOLVER = "GeonameResolver";
    public static final String TAG_OSMPOI_RESOLVER = "OSMPoiResolver";
    public static final String TAG_IMAGE_RECEIVER = "ImageReceiver";
    public static final String TAG_NOTIFICATION_CATCHER = "NotificationCatcher";
    public static final String TAG_SMS_RECEIVER = "SMSReceiver";
    public static final String TAG_UI_CONTENT = "UIContent";
    public static final String TAG_GEO_COLLECTOR = "GeoCollector";
    public static final String TAG_TERM_COLLECTOR = "TermCollector";


    // Settings Fields, AQ = Automatic Query, GR = GeonameResolver, OR = OSMPOIResolver
    public static final String SETTINGS_AQ_END_OF_DND = "AQ_END_OF_DND";
    public static final String SETTINGS_AQ_USE_LOCATION = "AQ_USE_LOCATION";
    public static final String SETTINGS_AQ_NOTIFICATION_VIBRATE = "AQ_QUERY_NOTIFICATION_VIBRATE";
    public static final String SETTINGS_AQ_NOTIFICATION_SOUND = "AQ_QUERY_NOTIFICATION_SOUND";
    public static final String SETTINGS_AQ_MINIMUM_NUMBER_OF_RESULTS_TO_DISPLAY_NOTIFICATION = "AQ_MINIMUM_NUMBER_OF_RESULTS_TO_DISPLAY_NOTIFICATION";
    public static final String SETTINGS_AQ_QUERYLIST_WEAROFF_TIME = "SETTINGS_AQ_QUERYLIST_WEAROFF_TIME";
    public static final String SETTINGS_GR_DISTANCE = "SETTINGS_GR_DISTANCE";
    public static final String SETTINGS_OR_DISTANCE = "SETTINGS_OR_DISTANCE";
    public static final String SETTINGS_GR_DISTANCE_SEEKBAR_PROGRESS = "SETTINGS_GR_DISTANCE_SEEKBAR_PROGRESS";
    public static final String SETTINGS_OR_DISTANCE_SEEKBAR_PROGRESS = "SETTINGS_OR_DISTANCE_SEEKBAR_PROGRESS";
    public static final String SETTINGS_GR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS = "SETTINGS_GR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS";
    public static final String SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS = "SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS";
    public static final String SETTINGS_UI_MINIMAL_CONTENT_LENGTH = "SETTINGS_UI_MINIMAL_CONTENT_LENGTH";
    public static final String SETTINGS_TC_MINIMAL_TOKEN_LENGTH = "SETTINGS_TC_MINIMAL_TOKEN_LENGTH";
    public static final String SETTINGS_USE_FAKE_LOCATION = "SETTINGS_USE_FAKE_LOCATION";
    public static final String SETTINGS_FAKE_LONGITUDE = "SETTINGS_FAKE_LONGITUDE";
    public static final String SETTINGS_FAKE_LATITUDE = "SETTINGS_FAKE_LATITUDE";
    public static final String SETTINGS_TC_APPLY_STOPWORDS = "SETTINGS_TC_APPLY_STOPWORDS";

    // Default Values
    public static final boolean SETTINGS_AQ_NOTIFICATION_VIBRATE_DEFAULT = true;
    public static final boolean SETTINGS_AQ_NOTIFICATION_SOUND_DEFAULT = true;
    public static final boolean SETTINGS_AQ_USE_LOCATION_DEFAULT = true;
    public static final int SETTINGS_AQ_MINIMUM_NUMBER_OF_RESULTS_TO_DISPLAY_NOTIFICATION_DEFAULT = 5;
    public static final long INFO_AQ_LAST_SUCCESSFUL_QUERY_DEFAULT = 0;
    public static final long SETTINGS_AQ_END_OF_DND_DEFAULT = 0;
    public static final int SETTINGS_AQ_QUERYLIST_WEAROFF_TIME_DEFAULT = 120000;
    public static final float SETTINGS_GR_DISTANCE_DEFAULT = 1f;
    public static final float SETTINGS_OR_DISTANCE_DEFAULT = 1f;
    public static final int SETTINGS_GR_DISTANCE_SEEKBAR_PROGRESS_DEFAULT = 1;
    public static final int SETTINGS_OR_DISTANCE_SEEKBAR_PROGRESS_DEFAULT = 1;
    public static final int SETTINGS_GR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS_DEFAULT = 200;
    public static final int SETTINGS_OR_MINIMAL_DISTANCE_BETWEEN_GEOPOSITIONS_DEFAULT = 200;
    public static final int  SETTINGS_UI_MINIMAL_CONTENT_LENGTH_DEFAULT = 20;
    public static final int  SETTINGS_TC_MINIMAL_TOKEN_LENGTH_DEFAULT = 3;
    public static final boolean SETTINGS_TC_APPLY_STOPWORDS_DEFAULT = true;
    public static final boolean SETTINGS_USE_FAKE_LOCATION_DEFAULT = false;
    public static final double SETTINGS_FAKE_LATITUDE_DEFAULT = 0.0;
    public static final double SETTINGS_FAKE_LONGITUDE_DEFAULT = 0.0;


    // Fields for Values Saved via Settings
    public static final String INFO_AQ_LAST_SUCCESSFUL_QUERY = "AWARE_LAST_SUCCESSFUL_QUERY";
    public static final String INFO_AQ_LAST_TIME_USER_CLICKED_RESULTITEM = "AWARE_LAST_TIME_USER_CLICKED_RESULTITEM";

    // Automatic Query Defaults
    public static final long AQ_PLUGIN_TIME_TO_WAIT_BETWEEN_RUNS = 1000;
    public static final long AQ_PLUGIN_MAX_NUMBER_OF_EMPTY_RUNS_BEFORE_SLEEP = 150;
    public static final int  AQ_PLUGIN_MAX_NUMBER_OF_NOTIFICATIONS_AT_ONCE = 5;

    //Clipboard Catcher
    public static final String CLIPBOARD_CATCHER_PLUGIN_NAME = "plugin.clipboard_catcher";
    public static final String CLIPBOARD_CATCHER_AUTHORITY = "com.aware.provider." + CLIPBOARD_CATCHER_PLUGIN_NAME;
    public static final String CLIPBOARD_CATCHER_MAIN_TABLE = "plugin_clipboard_catcher";

    public static final Uri CLIPBOARD_CATCHER_CONTENT_URI = Uri.parse("content://" + CLIPBOARD_CATCHER_AUTHORITY + "/" + CLIPBOARD_CATCHER_MAIN_TABLE); //this needs to match the table name
    public static final String CLIPBOARD_CATCHER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware." + CLIPBOARD_CATCHER_PLUGIN_NAME;
    public static final String CLIPBOARD_CATCHER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware." + CLIPBOARD_CATCHER_PLUGIN_NAME;

    public static final String CLIPBOARD_CATCHER_FIELD_ID = "_id";
    public static final String CLIPBOARD_CATCHER_FIELD_TIMESTAMP = "timestamp";
    public static final String CLIPBOARD_CATCHER_FIELD_DEVICE_ID = "device_id";
    public static final String CLIPBOARD_CATCHER_FIELD_CLIPBOARDCONTENT = "CLIPBOARDCONTENT";

    public static final int CLIPBOARD_CATCHER_MAX_STORAGE = 5;
    public static final long CLIPBOARD_CATCHER_WEAROFF_TIME = 5 * 60 * 1000;


    // Geoname Resolver
    public static final String GEONAME_RESOLVER_PLUGIN_NAME = "plugin.geoname_resolver";
    public static final String GEONAME_RESOLVER_AUTHORITY = "com.aware.provider." + GEONAME_RESOLVER_PLUGIN_NAME;
    public static final String GEONAME_RESOLVER_MAIN_TABLE = "plugin_geoname_resolver";

    public static final Uri GEONAME_RESOLVER_CONTENT_URI = Uri.parse("content://" + GEONAME_RESOLVER_AUTHORITY + "/" + GEONAME_RESOLVER_MAIN_TABLE); //this needs to match the table name
    public static final String GEONAME_RESOLVER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware." + GEONAME_RESOLVER_PLUGIN_NAME;
    public static final String GEONAME_RESOLVER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware." + GEONAME_RESOLVER_PLUGIN_NAME;

    public static final String GEONAME_RESOLVER_FIELD_ID = "_id";
    public static final String GEONAME_RESOLVER_FIELD_TIMESTAMP = "timestamp";
    public static final String GEONAME_RESOLVER_FIELD_DEVICE_ID = "device_id";
    public static final String GEONAME_RESOLVER_FIELD_NAME = "name";

    public static final int  GEONAME_RESOLVER_MAX_STORAGE = 5;
    public static final long GEONAME_RESOLVER_WEAROFF_TIME = 5 * 60 * 1000;

    // OSM Poi Resolver
    public static final String OSMPOI_RESOLVER_PLUGIN_NAME = "plugin.osmpoi_resolver";
    public static final String OSMPOI_RESOLVER_AUTHORITY = "com.aware.provider." + OSMPOI_RESOLVER_PLUGIN_NAME;
    public static final String OSMPOI_RESOLVER_MAIN_TABLE = "plugin_osmpoi_resolver";

    public static final Uri OSMPOI_RESOLVER_CONTENT_URI = Uri.parse("content://" + OSMPOI_RESOLVER_AUTHORITY + "/" + OSMPOI_RESOLVER_MAIN_TABLE); //this needs to match the table name
    public static final String OSMPOI_RESOLVER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware." + OSMPOI_RESOLVER_PLUGIN_NAME;
    public static final String OSMPOI_RESOLVER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware." + OSMPOI_RESOLVER_PLUGIN_NAME;

    public static final String OSMPOI_RESOLVER_FIELD_ID = "_id";
    public static final String OSMPOI_RESOLVER_FIELD_TIMESTAMP = "timestamp";
    public static final String OSMPOI_RESOLVER_FIELD_DEVICE_ID = "device_id";
    public static final String OSMPOI_RESOLVER_FIELD_NAME = "name";
    public static final String OSMPOI_RESOLVER_FIELD_TYPE = "type";

    public static final int OSMPOI_RESOLVER_MAX_STORAGE = 5;
    public static final long OSMPOI_RESOLVER_WEAROFF_TIME = 5 * 60 * 1000;

    // Image Receiver
    public static final String IMAGE_RECEIVER_PLUGIN_NAME = "plugin.image_receiver";
    public static final String IMAGE_RECEIVER_AUTHORITY = "com.aware.provider." + IMAGE_RECEIVER_PLUGIN_NAME;
    public static final String IMAGE_RECEIVER_MAIN_TABLE = "plugin_image_receiver";

    public static final Uri IMAGE_RECEIVER_CONTENT_URI = Uri.parse("content://" + IMAGE_RECEIVER_AUTHORITY + "/" + IMAGE_RECEIVER_MAIN_TABLE); //this needs to match the table name
    public static final String IMAGE_RECEIVER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware." + IMAGE_RECEIVER_PLUGIN_NAME;
    public static final String IMAGE_RECEIVER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware." + IMAGE_RECEIVER_PLUGIN_NAME;

    public static final String IMAGE_RECEIVER_FIELD_ID = "_id";
    public static final String IMAGE_RECEIVER_FIELD_TIMESTAMP = "timestamp";
    public static final String IMAGE_RECEIVER_FIELD_DEVICE_ID = "device_id";
    public static final String IMAGE_RECEIVER_FIELD_DATA = "_data";
    public static final String IMAGE_RECEIVER_FIELD_DISPLAY_NAME = "_display_name";
    public static final String IMAGE_RECEIVER_FIELD_SIZE = "_size";
    public static final String IMAGE_RECEIVER_FIELD_BUCKET_DISPLAY_NAME = "bucket_display_name";
    public static final String IMAGE_RECEIVER_FIELD_BUCKET_ID = "bucket_id";
    public static final String IMAGE_RECEIVER_FIELD_DATE_TAKEN = "date_taken";
    public static final String IMAGE_RECEIVER_FIELD_DATE_ADDED = "date_added";
    public static final String IMAGE_RECEIVER_FIELD_DATE_MODIFIED = "date_modified";
    public static final String IMAGE_RECEIVER_FIELD_DESCRIPTION = "description";
    public static final String IMAGE_RECEIVER_FIELD_HEIGHT = "height";
    public static final String IMAGE_RECEIVER_FIELD_ISPRIVATE = "isprivate";
    public static final String IMAGE_RECEIVER_FIELD_LATITUDE = "latitude";
    public static final String IMAGE_RECEIVER_FIELD_LONGITUDE = "longitude";
    public static final String IMAGE_RECEIVER_FIELD_MIME_TYPE = "mime_type";
    public static final String IMAGE_RECEIVER_FIELD_MINI_THUMB_MAGIC = "mini_thumb_magic";
    public static final String IMAGE_RECEIVER_FIELD_ORIENTATION = "orientation";
    public static final String IMAGE_RECEIVER_FIELD_PICASA_ID = "picasa_id";
    public static final String IMAGE_RECEIVER_FIELD_TITLE = "title";
    public static final String IMAGE_RECEIVER_FIELD_WIDTH = "width";

    // Notification Catcher
    public static final String NOTIFICATION_CATCHER_PLUGIN_NAME = "plugin.notification_catcher_contextopheles";
    public static final String NOTIFICATION_CATCHER_AUTHORITY = "com.aware.provider." + NOTIFICATION_CATCHER_PLUGIN_NAME;
    public static final String NOTIFICATION_CATCHER_MAIN_TABLE = "notification_catcher_contextopheles";

    public static final Uri NOTIFICATION_CATCHER_CONTENT_URI = Uri.parse("content://" + NOTIFICATION_CATCHER_AUTHORITY + "/" + NOTIFICATION_CATCHER_MAIN_TABLE); //this needs to match the table name
    public static final String NOTIFICATION_CATCHER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware." + NOTIFICATION_CATCHER_PLUGIN_NAME;
    public static final String NOTIFICATION_CATCHER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware." + NOTIFICATION_CATCHER_PLUGIN_NAME;

    public static final String NOTIFICATION_CATCHER_FIELD_ID = "_id";
    public static final String NOTIFICATION_CATCHER_FIELD_TIMESTAMP = "timestamp";
    public static final String NOTIFICATION_CATCHER_FIELD_DEVICE_ID = "device_id";
    public static final String NOTIFICATION_CATCHER_FIELD_TITLE = "title";
    public static final String NOTIFICATION_CATCHER_FIELD_TEXT = "content_text";
    public static final String NOTIFICATION_CATCHER_FIELD_APP_NAME = "app_name";

    public static final int NOTIFICATION_CATCHER_MAX_STORAGE = 5;
    public static final long NOTIFICATION_CATCHER_WEAROFF_TIME = 5 * 60 * 1000;

    // SMS Receiver
    public static final String SMS_RECEIVER_PLUGIN_NAME = "plugin.sms_receiver";
    public static final String SMS_RECEIVER_AUTHORITY = "com.aware.provider." + SMS_RECEIVER_PLUGIN_NAME;
    public static final String SMS_RECEIVER_MAIN_TABLE = "plugin_sms_receiver";

    public static final Uri SMS_RECEIVER_CONTENT_URI = Uri.parse("content://" + SMS_RECEIVER_AUTHORITY + "/" + SMS_RECEIVER_MAIN_TABLE);
    public static final String SMS_RECEIVER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware." + SMS_RECEIVER_PLUGIN_NAME;
    public static final String SMS_RECEIVER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware." + SMS_RECEIVER_PLUGIN_NAME;

    public static final String SMS_RECEIVER_FIELD_ID = "_id";
    public static final String SMS_RECEIVER_FIELD_TIMESTAMP = "timestamp";
    public static final String SMS_RECEIVER_FIELD_DEVICE_ID = "device_id";

    public static final String SMS_RECEIVER_FIELD_SMSContent = "SMSCONTENT";

    public static final int SMS_RECEIVER_MAX_STORAGE = 5;
    public static final long SMS_RECEIVER_WEAROFF_TIME = 5 * 60 * 1000;


    // UIContent
    public static final String UI_CONTENT_PLUGIN_NAME = "plugin.ui_content";
    public static final String UI_CONTENT_AUTHORITY = "com.aware.provider." + UI_CONTENT_PLUGIN_NAME;
    public static final String UI_CONTENT_MAIN_TABLE = "plugin_ui_content";

    public static final Uri     UI_CONTENT_CONTENT_URI = Uri.parse("content://" + UI_CONTENT_AUTHORITY + "/" + UI_CONTENT_MAIN_TABLE);
    public static final String  UI_CONTENT_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware." + UI_CONTENT_PLUGIN_NAME;
    public static final String  UI_CONTENT_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware." + UI_CONTENT_PLUGIN_NAME;

    public static final String UI_CONTENT_FIELD_ID =         "_id";
    public static final String UI_CONTENT_FIELD_TIMESTAMP =   "timestamp";
    public static final String UI_CONTENT_FIELD_DEVICE_ID =   "device_id";
    public static final String UI_CONTENT_FIELD_SOURCE_APP =  "source_app";
    public static final String UI_CONTENT_FIELD_TEXT =        "content_text";

    public static final int     UI_CONTENT_MAX_STORAGE = 5;
    public static final long    UI_CONTENT_WEAROFF_TIME = 5 * 60 * 1000;


    // Term Collector Common

    public static final String TERM_COLLECTOR_PLUGIN_NAME = "plugin.term_collector";
    public static final String TERM_COLLECTOR_AUTHORITY = "com.aware.provider." + TERM_COLLECTOR_PLUGIN_NAME;

    public static final String  TERM_COLLECTOR_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.term_collector";
    public static final String  TERM_COLLECTOR_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.term_collector";

    public static final String TERM_COLLECTOR_FIELD_ID =         "_id";
    public static final String TERM_COLLECTOR_FIELD_TIMESTAMP =   "timestamp";
    public static final String TERM_COLLECTOR_FIELD_DEVICE_ID =   "device_id";

    //Term Collector Term
    public static final String TERM_COLLECTOR_TERM_TABLE    = "plugin_term_collector_terms";
    public static final Uri    TERM_COLLECTOR_TERM_CONTENT_URI = Uri.parse("content://" + TERM_COLLECTOR_AUTHORITY + "/" + TERM_COLLECTOR_TERM_TABLE);
    public static final String TERM_COLLECTOR_TERM_FIELD_TERM_CONTENT = "term_content";
    public static final String TERM_COLLECTOR_TERM_FIELD_TERM_SOURCE = "term_source";


    //Term Collector GeoData
    public static final String TERM_COLLECTOR_GEODATA_TABLE = "plugin_term_collector_geodata";
    public static final Uri    TERM_COLLECTOR_GEODATA_CONTENT_URI = Uri.parse("content://" + TERM_COLLECTOR_AUTHORITY + "/" + TERM_COLLECTOR_GEODATA_TABLE);
    public static final String TERM_COLLECTOR_GEODATA_FIELD_TERM_CONTENT = "term_content";
    public static final String TERM_COLLECTOR_GEODATA_FIELD_TERM_SOURCE = "term_source";

    public static final int  TERM_COLLECTOR_GEODATA_MAX_STORAGE = 7;
    public static final long TERM_COLLECTOR_GEODATA_WEAROFF_TIME = 5 * 60 * 1000;

    //Term Collector Cache
    public static final String TERM_COLLECTOR_CACHE_TABLE   = "plugin_term_collector_geodata_cache";
    public static final Uri    TERM_COLLECTOR_CACHE_CONTENT_URI = Uri.parse("content://" + TERM_COLLECTOR_AUTHORITY + "/" + TERM_COLLECTOR_CACHE_TABLE);
    public static final String TERM_COLLECTOR_CACHE_FIELD_TERM_CONTENT = "term_content";
    public static final String TERM_COLLECTOR_CACHE_FIELD_IS_CITE = "is_city";

    // Light Sensor
    public static final String LIGHT_URI = "content://com.aware.provider.light/light";
    public static final String LIGHT_FIELD_TIMESTAMP = "timestamp";
    public static final String LIGHT_FIELD_DOUBLE_LUX = "double_light_lux";

    // Location
    public static final String LOCATION_URI = "content://com.aware.provider.locations/locations";

    //SituationManager Fields
    public static final String SITUATION_MANAGER_LIGHT = "light";

    //ActivitySituation
    public static final String INFO_AQ_ACTIVITY_TYPE = "INFO_AQ_ACTIVITY_TYPE";
    public static final String INFO_AQ_ACTIVITY_CONFIDENCE = "INFO_AQ_ACTIVITY_CONFIDENCE";
}