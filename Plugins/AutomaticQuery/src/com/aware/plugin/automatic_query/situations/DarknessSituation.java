package com.aware.plugin.automatic_query.situations;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.Map;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

/**
 * Created by wmb on 10.01.14.
 */
public class DarknessSituation extends Situation {

    public DarknessSituation(){
        this.TAG = "DarknessSituation";
        this.PREFIX = "SITUATION_DARKNESS";
    }

    public boolean assess(Map<String, Object> contextMap, Context context) {
        if (validateContext(contextMap)) {
            Double lux = (Double) contextMap.get(ContextophelesConstants.SITUATION_MANAGER_LIGHT);
            return lux < 1000.0;
        } else {
            return false;
        }
    }

    private boolean validateContext(Map<String, Object> contextMap) {
        boolean result = true;
        result &= validateSingleContextInformation(contextMap, ContextophelesConstants.SITUATION_MANAGER_LIGHT, Double.class.toString());
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
