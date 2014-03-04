package com.aware.plugin.automatic_query.situations;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;


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

    private SituationManager() {
    }


    public SituationManager(Context context) {
        this.context = context;
        this.blockerSituations = new ArrayList<Situation>();
        //blockerSituations.add(new DarknessSituation());
        blockerSituations.add(new DoNotDisturbSituation());
        blockerSituations.add(new MinimumTimeSinceLastSuccessfulQuerySituation());

        this.triggerSituations = new ArrayList<Situation>();
        this.contextMap = new HashMap<String, Object>();
    }

    public boolean allowsQuery() {

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

        for (Situation blocker : blockerSituations) {
            if (blocker.isEnabled(context) && blocker.assess(contextMap, context)) {
                // one blocker returned true
                return true;
            }
        }

        // no blocker returned true
        return false;
    }

    private boolean assessTriggerSituations() {
        for (Situation trigger : triggerSituations) {
            if (trigger.isEnabled(context) && trigger.assess(contextMap, context)) {
                // one Trigger returned true
                return true;
            }
        }

        // no Trigger returned true
        return false;
    }

    private void fillContextMap() {
        contextMap.put(ContextophelesConstants.SETTINGS_AQ_END_OF_DND, CommonSettings.getEndOfDoNotDisturb(context.getContentResolver()));
        contextMap.put(ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY, CommonSettings.getTimeOfLastSuccessfulQuery(context.getContentResolver()));
    }

    public LinearLayout getSettingsLayout(){
        LinearLayout result = new LinearLayout(context);
        result.setOrientation(LinearLayout.VERTICAL);

        // Add Situation Settings
        ArrayList<Situation> situations = new ArrayList<Situation>();
        situations.addAll(blockerSituations);
        situations.addAll(triggerSituations);

        for(Situation situation: situations){
            result.addView(situation.getSettingsLayout(context));
        }

        return result;
    }

    public void initSituationSettings(){
        // Add Situation Settings
        ArrayList<Situation> situations = new ArrayList<Situation>();
        situations.addAll(blockerSituations);
        situations.addAll(triggerSituations);

        for(Situation situation: situations){
            situation.initSettings(context);
        }
    }

    public void resetSituationSettings(){
        // Add Situation Settings
        ArrayList<Situation> situations = new ArrayList<Situation>();
        situations.addAll(blockerSituations);
        situations.addAll(triggerSituations);

        for(Situation situation: situations){
            situation.resetSettings(context);
        }
    }

}
