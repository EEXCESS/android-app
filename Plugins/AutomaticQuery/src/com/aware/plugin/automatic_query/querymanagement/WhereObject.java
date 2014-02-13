package com.aware.plugin.automatic_query.querymanagement;

/**
 * Created by wmb on 10.02.14.
 */
public class WhereObject extends TermObject implements Comparable<TermObject> {
    private WhereObject(){}

    public WhereObject(long timestamp, String source, String value) {
        this.timestamp = timestamp;
        this.source = source;
        this.value = value;
        double importance = value.length() * 0.01f;
        if(value.contains(" ")){
            importance += 0.05f;
        }
        this.importance = importance;
    }

    public String toString(){
        return "Whereobject: " + value;
    }
}
