package com.aware.plugin.automatic_query.situations;

import android.util.Log;

import java.util.Map;

/**
 * Created by wmb on 10.01.14.
 */
public abstract class Situation {

    private final String TAG = "Situation";

    public abstract boolean assess(Map <String, Object> contextMap);

    // Checks, wether the given map contains a not-null object for the given identifier with the given class
    protected boolean validateSingleContextInformation (Map <String, Object> contextMap, String contextIdentifier, String expectedClass) {
       Log.wtf(TAG, "Validating Contextinformation " + contextIdentifier);

        Object obj = contextMap.get(contextIdentifier);

        if (obj != null) {
          Log.wtf(TAG, "Object is not null");
          if(obj.getClass().toString().equals(expectedClass)) {
              Log.wtf(TAG, "Object is of expected class " + expectedClass);
              return true;
          } else {
              Log.wtf(TAG, "Object is not of expected class " + expectedClass + " but of class " + obj.getClass().toString());
              return false;
          }
        } else {
            Log.wtf(TAG, "Object is null");
            return false;
        }
    };
}
