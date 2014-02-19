package com.aware.plugin.automatic_query.situations;

import java.util.Map;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

/**
 * Created by wmb on 10.01.14.
 */
public class DarknessSituation extends Situation {
    public boolean assess(Map<String, Object> contextMap) {
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
}
