package com.aware.plugin.automatic_query.situations;

import com.aware.plugin.automatic_query.Settings;

import java.util.Map;

/**
 * Created by wmb on 10.01.14.
 */
public class MinimumTimeSinceLastSuccessfulQuerySituation extends Situation {



    public boolean assess(Map<String, Object> contextMap) {
        if(validateContext(contextMap)) {
            Long lastSuccessfulQuery = (Long) contextMap.get(Settings.AWARE_LAST_SUCCESSFUL_QUERY);

            // returns true, when last successful query was more then 15 seconds ago
            return lastSuccessfulQuery + 15000 > System.currentTimeMillis();
        } else {
            return false;
        }
    }

   private boolean validateContext(Map<String, Object> contextMap) {
       boolean result = true;
       result &= validateSingleContextInformation(contextMap, Settings.AWARE_LAST_SUCCESSFUL_QUERY, Long.class.toString());
       return result;
   }
}
