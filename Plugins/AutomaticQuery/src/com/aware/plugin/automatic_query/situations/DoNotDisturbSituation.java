package com.aware.plugin.automatic_query.situations;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.Map;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

/**
 * Created by wmb on 10.01.14.
 */
public class DoNotDisturbSituation extends Situation {

    public DoNotDisturbSituation(){
        this.TAG = "DoNotDisturbSituation";
        this.PREFIX = "SITUATION_DND";
    }

    public boolean assess(Map<String, Object> contextMap, Context context) {
        if (validateContext(contextMap)) {
            Long endtime = (Long) contextMap.get(ContextophelesConstants.SETTINGS_AQ_END_OF_DND);

            // returns true, when endtime is not yet reached
            return endtime > System.currentTimeMillis();
        } else {
            return false;
        }
    }

    private boolean validateContext(Map<String, Object> contextMap) {
        boolean result = true;
        result &= validateSingleContextInformation(contextMap, ContextophelesConstants.SETTINGS_AQ_END_OF_DND, Long.class.toString());
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
