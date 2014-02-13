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
    private HashMap<String, ArrayList<WhatObject>> objectListMap;

    public TermManager(){
        maxStorageMap = new HashMap<String, Integer>();
        noveltyWearOffTimeMap = new HashMap<String, Long>();
        objectListMap = new HashMap<String, ArrayList<WhatObject>>();
    }

    public void registerPlugin(String pluginName, int maxStorage, long noveltyWearOffTime){
        objectListMap.put(pluginName, new ArrayList<WhatObject>());
        maxStorageMap.put(pluginName, maxStorage);
        noveltyWearOffTimeMap.put(pluginName, noveltyWearOffTime);
    }

    public void add(WhatObject toAdd){
        cleanUp();
        String key = toAdd.getSource();

        List<WhatObject> addList = objectListMap.get(key);

        if(!addList.contains(toAdd)){
        // List is full
        while(addList.size() >= maxStorageMap.get(key)){
            // Delete the first and oldest entry
            addList.remove(0);
        };

        // add the new object
            addList.add(toAdd);
        }
    }

    public void remove(WhatObject toRemove){
        objectListMap.get(toRemove.getSource()).remove(toRemove);
    }

    public void cleanUp(){
        Long time = System.currentTimeMillis();

        for(String key: objectListMap.keySet()){
            // get Wearofftime for this plugin
            Long wearOffTime = noveltyWearOffTimeMap.get(key);

            // this collects the objects to remove
            List<WhatObject> objectsToRemove = new ArrayList<WhatObject>();

            // get all objects which are outdated
            for(WhatObject whatObject: objectListMap.get(key)){
                if(time - whatObject.getTimestamp() > wearOffTime) {
                    objectsToRemove.add(whatObject);
                }
            }

            // remove them
            objectListMap.get(key).removeAll(objectsToRemove);
        }
    }

    public List<WhatObject> getWhatObjects(){
        List<WhatObject> whatObjects = new ArrayList<WhatObject>();

        for(String key: objectListMap.keySet()){
            whatObjects.addAll(objectListMap.get(key));
        }

        return whatObjects;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Whatmanager:\n");
        for(String key: objectListMap.keySet()){
            sb.append(objectListMap.get(key).toString()  + "\n");
        }

        return sb.toString();
    }


}
