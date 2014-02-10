package com.aware.plugin.automatic_query.querymanagement;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wmb on 10.02.14.
 */
public class QueryManager {
   private ArrayList<QueryObject> queryList;
    private WhatManager whatManager;
    private WhereManager whereManager;

    public QueryManager(){
        queryList = new ArrayList<QueryObject>();

        Log.d(this.getClass().toString(), "@registering WhatManagerPlugins");
        whatManager = new WhatManager();
        whatManager.registerPlugin("content://com.aware.provider.plugin.ui_content/plugin_ui_content", 5, 5*60*1000);
        whatManager.registerPlugin("content://com.aware.provider.plugin.clipboard_catcher/plugin_clipboard_catcher", 5, 5*60*1000);
        whatManager.registerPlugin("content://com.aware.provider.plugin.osmpoi_resolver/plugin_osmpoi_resolver", 5, 5*60*1000);
        whatManager.registerPlugin("content://com.aware.provider.plugin.notification_catcher/plugin_notification_catcher", 5, 5*60*1000);
        whatManager.registerPlugin("content://com.aware.provider.plugin.sms_receiver/plugin_sms_receiver", 5, 5*60*1000);

        Log.d(this.getClass().toString(), "@registering WhereManagerPlugins");
        whereManager = new WhereManager();
        whereManager.registerPlugin("content://com.aware.provider.plugin.term_collector/plugin_term_collector_geodata", 7, 5*60*1000);
        whereManager.registerPlugin("content://com.aware.provider.plugin.geoname_resolver/plugin_geoname_resolver", 7, 15*60*1000);
    }

    public void addWhatObject(WhatObject toAdd){
        Log.d(this.getClass().toString(), "@addWhatObject");
        whatManager.add(toAdd);
    }

    public void addWhereObject(WhereObject toAdd){
        Log.d(this.getClass().toString(), "@addWhereObject");
        whereManager.add(toAdd);
    }

    private void cleanUp(){
        Log.d(this.getClass().toString(), "@cleanUp");
        cleanupQueryList();
        whatManager.cleanUp();
        whereManager.cleanUp();
    }


    private void cleanupQueryList(){

    }

    public QueryObject getNextQueryObject(){
        Log.d(this.getClass().toString(), "@getNextQueryObject");
        cleanUp();
        Log.d(this.getClass().toString(), whatManager.toString());
        Log.d(this.getClass().toString(), whereManager.toString());

        List<WhatObject> whatObjects = whatManager.getWhatObjects();
        List<WhereObject> whereObjects= whereManager.getWhereObjects();

        WhatObject whatObject;
        WhereObject whereObject;

        if(whatObjects.isEmpty()) {
            whatObject = new WhatObject(0, "", "");
        } else {
            whatObject = whatObjects.get(0);
        }

        if(whereObjects.isEmpty()) {
            whereObject = new WhereObject(0, "", "");
        } else {
            whereObject = whereObjects.get(0);
        }

        return new QueryObject(whatObject, whereObject);

    }
}
