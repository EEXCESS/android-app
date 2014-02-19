package com.aware.plugin.automatic_query.situations;

import java.util.Map;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;

/**
 * Created by wmb on 10.01.14.
 */
public class MinimumTimeSinceLastSuccessfulQuerySituation extends Situation {


    public boolean assess(Map<String, Object> contextMap) {
        if (validateContext(contextMap)) {
            Long lastSuccessfulQuery = (Long) contextMap.get(ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY);

            // returns true, when last successful query was more then 15 seconds ago
            return lastSuccessfulQuery + ContextophelesConstants.SITUATION_MANAGER_MINIMU_TIME_BETWEEN_QUERIES > System.currentTimeMillis();
        } else {
            return false;
        }
    }

    private boolean validateContext(Map<String, Object> contextMap) {
        boolean result = true;
        result &= validateSingleContextInformation(contextMap, ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY, Long.class.toString());
        return result;
    }
}
