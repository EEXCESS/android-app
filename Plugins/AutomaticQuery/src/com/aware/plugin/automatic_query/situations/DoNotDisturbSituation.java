package com.aware.plugin.automatic_query.situations;

import java.util.Map;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

/**
 * Created by wmb on 10.01.14.
 */
public class DoNotDisturbSituation extends Situation {


    public boolean assess(Map<String, Object> contextMap) {
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
}
