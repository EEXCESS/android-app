package com.aware.plugin.automatic_query.querymanagement;

/**
 * Created by wmb on 10.02.14.
 */
public class WhereObject extends TermObject implements Comparable<TermObject> {
    private WhereObject() {
    }

    public WhereObject(long timestamp, String source, String value) {
        this.timestamp = timestamp;
        this.source = source;
        this.value = value;
        this.importance = computeImportanceFromValue(value);
    }

    public String toString() {
        return "Whereobject: " + value;
    }
}
