package com.aware.plugin.automatic_query.querymanagement;

/**
 * Created by wmb on 10.02.14.
 */
public class TermObject implements Comparable<TermObject>{
    protected long timestamp;
    protected String source;
    protected String value;
    protected double importance;

    protected TermObject(){}

    public TermObject(long timestamp, String source, String value) {
        this.timestamp = timestamp;
        this.source = source;
        this.value = value;
        double importance = value.length() * 0.01f;
        if(value.contains(" ")){
            importance += 0.05f;
        }
        this.importance = importance;
    }

    public String getValue() {
        return value;
    }

    public String getSource() {
        return source;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getImportance(){
        return importance;
    }

    public double getNovelty(){
        return 0.0f;
    }

    public String toString(){
        return "Termobject: " + value;
    }

    public int compareTo(TermObject other){
        int last = this.value.compareTo(other.value);
        return last == 0 ? this.value.compareTo(other.value) : last;
    }

    public boolean equals(Object aThat){
        if ( this == aThat ) return true;
        if ( !(aThat instanceof TermObject) ) return false;
        return this.value.equals(((TermObject) aThat).getValue());
    }
}
