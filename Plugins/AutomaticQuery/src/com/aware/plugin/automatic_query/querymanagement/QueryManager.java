package com.aware.plugin.automatic_query.querymanagement;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;

/**
 * Created by wmb on 10.02.14.
 */
public class QueryManager {
    private ConcurrentSkipListSet<QueryObject> queryList;
    private TermManager whatManager;
    private TermManager whereManager;
    private Context context;

    public QueryManager(Context context) {
        this.context = context;
        queryList = new ConcurrentSkipListSet<QueryObject>(new QueryObjectImportanceComparator());

        Log.d(this.getClass().toString(), "@registering WhatManagerPlugins");
        whatManager = new TermManager("WhatManager");
        whatManager.registerPlugin(ContextophelesConstants.UI_CONTENT_CONTENT_URI.toString(), ContextophelesConstants.UI_CONTENT_MAX_STORAGE, ContextophelesConstants.UI_CONTENT_WEAROFF_TIME);
        whatManager.registerPlugin(ContextophelesConstants.CLIPBOARD_CATCHER_CONTENT_URI.toString(), ContextophelesConstants.CLIPBOARD_CATCHER_MAX_STORAGE, ContextophelesConstants.CLIPBOARD_CATCHER_WEAROFF_TIME);
        whatManager.registerPlugin(ContextophelesConstants.OSMPOI_RESOLVER_CONTENT_URI.toString(), ContextophelesConstants.OSMPOI_RESOLVER_MAX_STORAGE, ContextophelesConstants.OSMPOI_RESOLVER_WEAROFF_TIME);
        whatManager.registerPlugin(ContextophelesConstants.NOTIFICATION_CATCHER_CONTENT_URI.toString(), ContextophelesConstants.NOTIFICATION_CATCHER_MAX_STORAGE, ContextophelesConstants.NOTIFICATION_CATCHER_WEAROFF_TIME);
        whatManager.registerPlugin(ContextophelesConstants.SMS_RECEIVER_CONTENT_URI.toString(), ContextophelesConstants.SMS_RECEIVER_MAX_STORAGE, ContextophelesConstants.SMS_RECEIVER_WEAROFF_TIME);
        whatManager.registerPlugin(ContextophelesConstants.GEONAME_RESOLVER_CONTENT_URI.toString(), ContextophelesConstants.GEONAME_RESOLVER_MAX_STORAGE, ContextophelesConstants.GEONAME_RESOLVER_WEAROFF_TIME);

        Log.d(this.getClass().toString(), "@registering WhereManagerPlugins");
        whereManager = new TermManager("WhereManager");
        whereManager.registerPlugin(ContextophelesConstants.TERM_COLLECTOR_GEODATA_CONTENT_URI.toString(), ContextophelesConstants.TERM_COLLECTOR_GEODATA_MAX_STORAGE, ContextophelesConstants.TERM_COLLECTOR_GEODATA_WEAROFF_TIME);
        whereManager.registerPlugin(ContextophelesConstants.GEONAME_RESOLVER_CONTENT_URI.toString(), ContextophelesConstants.GEONAME_RESOLVER_MAX_STORAGE, ContextophelesConstants.GEONAME_RESOLVER_WEAROFF_TIME);
        whereManager.registerPlugin(ContextophelesConstants.UI_CONTENT_CONTENT_URI.toString(), ContextophelesConstants.UI_CONTENT_MAX_STORAGE, ContextophelesConstants.UI_CONTENT_WEAROFF_TIME);
        whereManager.registerPlugin(ContextophelesConstants.CLIPBOARD_CATCHER_CONTENT_URI.toString(), ContextophelesConstants.CLIPBOARD_CATCHER_MAX_STORAGE, ContextophelesConstants.CLIPBOARD_CATCHER_WEAROFF_TIME);
        whereManager.registerPlugin(ContextophelesConstants.OSMPOI_RESOLVER_CONTENT_URI.toString(), ContextophelesConstants.OSMPOI_RESOLVER_MAX_STORAGE, ContextophelesConstants.OSMPOI_RESOLVER_WEAROFF_TIME);
        whereManager.registerPlugin(ContextophelesConstants.NOTIFICATION_CATCHER_CONTENT_URI.toString(), ContextophelesConstants.NOTIFICATION_CATCHER_MAX_STORAGE, ContextophelesConstants.NOTIFICATION_CATCHER_WEAROFF_TIME);
        whereManager.registerPlugin(ContextophelesConstants.SMS_RECEIVER_CONTENT_URI.toString(), ContextophelesConstants.SMS_RECEIVER_MAX_STORAGE, ContextophelesConstants.SMS_RECEIVER_WEAROFF_TIME);
        whereManager.registerPlugin(ContextophelesConstants.GEONAME_RESOLVER_CONTENT_URI.toString(), ContextophelesConstants.GEONAME_RESOLVER_MAX_STORAGE, ContextophelesConstants.GEONAME_RESOLVER_WEAROFF_TIME);
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

        int wearOffTime = CommonSettings.getQueryListWearOffTime(context.getContentResolver());
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
        for (QueryObject objectToRemove : objectsToRemove) {
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
