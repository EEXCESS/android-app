package com.aware.plugin.automatic_query.querymanagement;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wmb on 10.02.14.
 */
public class WhereManager {
    private HashMap<String, Integer> maxStorageMap;
    private HashMap<String, Long> noveltyWearOffTimeMap;
    private HashMap<String, ArrayList<WhereObject>> objectListMap;

    public WhereManager(){
        maxStorageMap = new HashMap<String, Integer>();
        noveltyWearOffTimeMap = new HashMap<String, Long>();
        objectListMap = new HashMap<String, ArrayList<WhereObject>>();
    }

    public void registerPlugin(String pluginName, int maxStorage, long noveltyWearOffTime){
        objectListMap.put(pluginName, new ArrayList<WhereObject>());
        maxStorageMap.put(pluginName, maxStorage);
        noveltyWearOffTimeMap.put(pluginName, noveltyWearOffTime);
    }

    public void add(WhereObject toAdd){
        cleanUp();
        String key = toAdd.getSource();

        List<WhereObject> addList = objectListMap.get(key);

        if(!addList.contains(toAdd)){
            // List is full
            while(addList.size() >= maxStorageMap.get(key)){
                // Delete the first and oldest entry
                addList.remove(0);
            }

            // add the new object
            addList.add(toAdd);
        }
    }

    public void remove(WhereObject toRemove){
        objectListMap.get(toRemove.getSource()).remove(toRemove);
    }

    public void cleanUp(){
        Long time = System.currentTimeMillis();

        for(String key: objectListMap.keySet()){
                        // get Wearofftime for this plugin
                        Long wearOffTime = noveltyWearOffTimeMap.get(key);

                        // this collects the objects to remove
                        List<WhereObject> objectsToRemove = new ArrayList<WhereObject>();

                        // get all objects which are outdated
                        for(WhereObject whereObject: objectListMap.get(key)){
                            if(time - whereObject.getTimestamp() > wearOffTime) {
                                objectsToRemove.add(whereObject);
                            }
                        }
                Log.d(this.getClass().toString(), "Removing Objects: " + objectsToRemove);
                        // remove them
                        objectListMap.get(key).removeAll(objectsToRemove);
        }
    }

    public List<WhereObject> getWhereObjects(){
        List<WhereObject> whereObjects = new ArrayList<WhereObject>();

        for(String key: objectListMap.keySet()){
            whereObjects.addAll(objectListMap.get(key));
        }

        return whereObjects;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Wheremanager:\n");
        for(String key: objectListMap.keySet()){
            sb.append(objectListMap.get(key).toString()  + "\n");
        }

        return sb.toString();
    }
}
