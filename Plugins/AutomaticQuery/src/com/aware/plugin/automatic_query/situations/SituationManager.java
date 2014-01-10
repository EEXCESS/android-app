package com.aware.plugin.automatic_query.situations;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by wmb on 10.01.14.
 */
public class SituationManager {

    // Situations that prohibit the run of a query
    private ArrayList<Situation> blockerSituations;

    // Situations that trigger the run of a query
    private ArrayList<Situation> triggerSituations;

    private HashMap<String, Object> contextMap;

    public SituationManager(){
       this.blockerSituations = new ArrayList<Situation>();
       blockerSituations.add(new DarknessSituation());

       this.triggerSituations = new ArrayList<Situation>();
       this.contextMap = new HashMap<String, Object>();
    }

    public boolean allowsQuery(){

        if (assessTriggerSituations()) {
            // a Trigger Situation explicitly triggered
            return true;
        } else {
            // return the opposite of the blocker state
            return !assesBlockerSituations();
        }
    }

    public void putContextValue(String contextIdentifier, Object value) {
        contextMap.put(contextIdentifier, value);
    }

    private boolean assesBlockerSituations() {

        for(Situation blocker : blockerSituations ){
            if (blocker.assess(contextMap)) {
                // one blocker returned true
                return true;
            }
        }

        // no blocker returned true
        return false;
    }

    private boolean assessTriggerSituations() {
        for(Situation blocker : triggerSituations ){
            if (blocker.assess(contextMap)) {
                // one Trigger returned true
                return true;
            }
        }

        // no Trigger returned true
        return false;
    }
}
