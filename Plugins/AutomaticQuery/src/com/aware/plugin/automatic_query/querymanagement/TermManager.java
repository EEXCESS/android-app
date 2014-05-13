package com.aware.plugin.automatic_query.querymanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wmb on 10.02.14.
 */
public class TermManager {
    private HashMap<String, Integer> maxStorageMap;
    private HashMap<String, Long> noveltyWearOffTimeMap;
    private HashMap<String, ArrayList<TermObject>> objectListMap;
    private String name;

    private TermManager() {
    }

    public TermManager(String name) {
        this.name = name;
        maxStorageMap = new HashMap<String, Integer>();
        noveltyWearOffTimeMap = new HashMap<String, Long>();
        objectListMap = new HashMap<String, ArrayList<TermObject>>();
    }

    public void registerPlugin(String pluginName, int maxStorage, long noveltyWearOffTime) {
        objectListMap.put(pluginName, new ArrayList<TermObject>());
        maxStorageMap.put(pluginName, maxStorage);
        noveltyWearOffTimeMap.put(pluginName, noveltyWearOffTime);
    }

    public void add(TermObject toAdd) {
        cleanUp();
        if(toAdd != null){
        String key = toAdd.getSource();

        List<TermObject> addList = objectListMap.get(key);

        if (!addList.contains(toAdd)) {
            // List is full
            while (addList.size() >= maxStorageMap.get(key)) {
                // Delete the first and oldest entry
                addList.remove(0);
            }

            // add the new object
            addList.add(toAdd);
        }
        }
    }

    public void remove(TermObject toRemove) {
        objectListMap.get(toRemove.getSource()).remove(toRemove);
    }

    public void cleanUp() {
        Long time = System.currentTimeMillis();

        for (String key : objectListMap.keySet()) {
            // get Wearofftime for this plugin
            Long wearOffTime = noveltyWearOffTimeMap.get(key);

            // this collects the objects to remove
            List<TermObject> objectsToRemove = new ArrayList<TermObject>();

            // get all objects which are outdated
            for (TermObject termObject : objectListMap.get(key)) {
                if (time - termObject.getTimestamp() > wearOffTime) {
                    objectsToRemove.add(termObject);
                }
            }

            // remove them
            objectListMap.get(key).removeAll(objectsToRemove);
        }
    }

    public List<TermObject> getTermObjects() {
        List<TermObject> termObjects = new ArrayList<TermObject>();

        for (String key : objectListMap.keySet()) {
            termObjects.addAll(objectListMap.get(key));
        }

        return termObjects;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name + ":\n");
        for (String key : objectListMap.keySet()) {
            sb.append(objectListMap.get(key).toString() + "\n");
        }

        return sb.toString();
    }


}
