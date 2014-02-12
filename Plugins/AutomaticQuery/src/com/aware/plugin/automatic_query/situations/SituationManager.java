package com.aware.plugin.automatic_query.situations;

import android.content.Context;

import com.aware.Aware;
import com.aware.plugin.automatic_query.Settings;

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
    private Context context;

    private SituationManager(){};

    public SituationManager(Context context){
        this.context = context;
       this.blockerSituations = new ArrayList<Situation>();
       //blockerSituations.add(new DarknessSituation());
        blockerSituations.add(new DoNotDisturbSituation());
        blockerSituations.add(new MinimumTimeSinceLastSuccessfulQuerySituation());

       this.triggerSituations = new ArrayList<Situation>();
       this.contextMap = new HashMap<String, Object>();
    }

    public boolean allowsQuery(){

        // fill contextMap with values from Settings
        fillContextMap();

        if (assessTriggerSituations()) {
            // a Trigger Situation explicitly triggered
            System.out.println("A Trigger has fired");
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

    private void fillContextMap(){
        contextMap.put(Settings.AWARE_END_OF_DND, new Long(getEndOfDND()));
        contextMap.put(Settings.AWARE_LAST_SUCCESSFUL_QUERY, new Long(getTimeOfLastSuccessfulQuery()));
    }


    private long getEndOfDND(){
        String endOfDNDString = Aware.getSetting(context.getContentResolver(), Settings.AWARE_END_OF_DND);
        if(endOfDNDString != null) {
            try {
                return Long.parseLong(endOfDNDString);
            } catch (NumberFormatException e) {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

    private long getTimeOfLastSuccessfulQuery(){
        String timeOfLastSuccessfulQueryString = Aware.getSetting(context.getContentResolver(), Settings.AWARE_LAST_SUCCESSFUL_QUERY);
        if(timeOfLastSuccessfulQueryString != null) {
            try {
                return Long.parseLong(timeOfLastSuccessfulQueryString);
            } catch (NumberFormatException e) {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

}
