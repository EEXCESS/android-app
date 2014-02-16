package com.aware.plugin.automatic_query.querymanagement;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by wmb on 10.02.14.
 */
public class QueryManager {
    private ConcurrentSkipListSet<QueryObject> queryList;
    private TermManager whatManager;
    private TermManager whereManager;

    public QueryManager() {
        queryList = new ConcurrentSkipListSet<QueryObject>(new QueryObjectImportanceComparator());

        Log.d(this.getClass().toString(), "@registering WhatManagerPlugins");
        whatManager = new TermManager("WhatManager");
        whatManager.registerPlugin("content://com.aware.provider.plugin.ui_content/plugin_ui_content", 5, 5 * 60 * 1000);
        whatManager.registerPlugin("content://com.aware.provider.plugin.clipboard_catcher/plugin_clipboard_catcher", 5, 5 * 60 * 1000);
        whatManager.registerPlugin("content://com.aware.provider.plugin.osmpoi_resolver/plugin_osmpoi_resolver", 5, 5 * 60 * 1000);
        whatManager.registerPlugin("content://com.aware.provider.plugin.notification_catcher/plugin_notification_catcher", 5, 5 * 60 * 1000);
        whatManager.registerPlugin("content://com.aware.provider.plugin.sms_receiver/plugin_sms_receiver", 5, 5 * 60 * 1000);

        Log.d(this.getClass().toString(), "@registering WhereManagerPlugins");
        whereManager = new TermManager("WhereManager");
        whereManager.registerPlugin("content://com.aware.provider.plugin.term_collector/plugin_term_collector_geodata", 7, 5 * 60 * 1000);
        whereManager.registerPlugin("content://com.aware.provider.plugin.geoname_resolver/plugin_geoname_resolver", 7, 15 * 60 * 1000);
    }

    public void addWhatObject(WhatObject toAdd) {
        Log.d(this.getClass().toString(), "@addWhatObject: " + toAdd.getValue());
        whatManager.add(toAdd);
        createQuerysFromList(toAdd, whereManager.getTermObjects());
    }

    public void addWhereObject(WhereObject toAdd) {
        Log.d(this.getClass().toString(), "@addWhereObject: " + toAdd.getValue());
        whereManager.add(toAdd);
        createQuerysFromList(toAdd, whatManager.getTermObjects());
    }

    private void cleanUp() {
        Log.d(this.getClass().toString(), "@cleanUp");
        cleanupQueryList();
        whatManager.cleanUp();
        whereManager.cleanUp();
    }


    private void cleanupQueryList() {
        Long time = System.currentTimeMillis();

        int wearOffTime = 60000;
        // this collects the objects to remove
        List<QueryObject> objectsToRemove = new ArrayList<QueryObject>();

        // get all objects which are outdated
        for (QueryObject queryObject : queryList) {
            if (time - queryObject.getTimestamp() > wearOffTime) {
                objectsToRemove.add(queryObject);
            }
        }
        Log.d(this.getClass().toString(), "Removing Objects: " + objectsToRemove);

        // remove them
        for(QueryObject objectToRemove: objectsToRemove){
            queryList.remove(objectToRemove);
        }
    }

    public QueryObject getNextQueryObject() {
        Log.d(this.getClass().toString(), "@getNextQueryObject");
        cleanUp();

        Log.d(this.getClass().toString(), "QueryObjects:" + queryList);
        Log.d(this.getClass().toString(), whatManager.toString());
        Log.d(this.getClass().toString(), whereManager.toString());

        if (!queryList.isEmpty()) {
            // get and remove highest Object
            QueryObject result = queryList.pollFirst();
            return result;
        } else {
            return null;
        }
    }

    // Assumes to get a WhereObject and a List of WhatObjects OR the other way round!
    private void createQuerysFromList(TermObject singleObject, List<TermObject> objectList) {
        if (singleObject.getClass().toString().equals(WhatObject.class.toString())) {
            queryList.add(new QueryObject((WhatObject) singleObject, new WhereObject(0, "", "")));
            for (TermObject whereObject : objectList) {
                queryList.add(new QueryObject((WhatObject) singleObject, (WhereObject) whereObject));
            }
        } else {
            queryList.add(new QueryObject(new WhatObject(0, "", ""), (WhereObject) singleObject));
            for (TermObject whatObject : objectList) {
                queryList.add(new QueryObject((WhatObject) whatObject, (WhereObject) singleObject));
            }
        }
    }

    private static class QueryObjectImportanceComparator implements Comparator<QueryObject> {
        public int compare(QueryObject qo1, QueryObject qo2) {
            double imp1 = qo1.getImportance();
            double imp2 = qo2.getImportance();

            if (imp1 > imp2) return -1;
            if (imp1 < imp2) return 1;
            return 0;
        }
    }
}
