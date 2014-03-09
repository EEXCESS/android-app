package com.aware.plugin.automatic_query.situations;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.Map;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

/**
 * Created by wmb on 10.01.14.
 */
public class ActivitySituation extends Situation {
    final String BLOCK_IN_VEHICLE_SETTING = "BLOCK_IN_VEHICLE_SETTING";
    final String BLOCK_ON_BICYCLE_SETTING = "BLOCK_ON_BICYCLE_SETTING";
    final String BLOCK_ON_FOOT_SETTING = "BLOCK_ON_FOOT_SETTING";
    final String BLOCK_STILL_SETTING = "BLOCK_STILL_SETTING";
    final String BLOCK_TILTING_SETTING = "BLOCK_TILTING_SETTING";
    final String BLOCK_UNKNOWN_SETTING = "BLOCK_UNKNOWN_SETTING";

    final String MINIMAL_CONFIDENCE_SETTING = "MINIMAL_CONFIDENCE_SETTING";

    final boolean BLOCK_IN_VEHICLE_SETTING_DEFAULT = true;
    final boolean BLOCK_ON_BICYCLE_SETTING_DEFAULT = true;
    final boolean BLOCK_ON_FOOT_SETTING_DEFAULT = false;
    final boolean BLOCK_STILL_SETTING_DEFAULT = false;
    final boolean BLOCK_TILTING_SETTING_DEFAULT = false;
    final boolean BLOCK_UNKNOWN_SETTING_DEFAULT = false;

    final int MINIMAL_CONFIDENCE_SETTING_DEFAULT = 0;


    public ActivitySituation(){
        this.TAG = "ActivitySituation";
        this.PREFIX = "SITUATION_ACTIVITY";
    }

    public boolean assess(Map<String, Object> contextMap, Context context) {
        if (validateContext(contextMap)) {
            int activityType = (Integer) contextMap.get(ContextophelesConstants.INFO_AQ_ACTIVITY_TYPE);
            int activityConfidence = (Integer) contextMap.get(ContextophelesConstants.INFO_AQ_ACTIVITY_CONFIDENCE);

            // TODO: Finish implementation by including Google Play Services, I had an error with gradle
            // returns true, if any of the activated activities fires
            boolean result = false;

            switch(activityType){


            }
           // if(Aware.getSetting())


            return result;
        } else {
            return false;
        }
    }

    private boolean validateContext(Map<String, Object> contextMap) {
        boolean result = true;
        result &= validateSingleContextInformation(contextMap, ContextophelesConstants.INFO_AQ_ACTIVITY_TYPE, Integer.class.toString());
        result &= validateSingleContextInformation(contextMap, ContextophelesConstants.INFO_AQ_ACTIVITY_CONFIDENCE, Integer.class.toString());
        return result;
    }

    public LinearLayout getSettingsLayout(Context context){
        LinearLayout result = super.getSettingsLayout(context);

        return result;
    }

    public void initSettings(Context context){
        super.initSettings(context);
    }

    public void resetSettings(Context context) {
        super.resetSettings(context);
    }

}
