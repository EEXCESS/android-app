package com.aware.plugin.automatic_query.querymanagement;

/**
 * Created by wmb on 10.02.14.
 */
public class QueryObject {
    private WhatObject what;
    private WhereObject where;
    private long timestamp;
    private double affinity;

    public QueryObject(WhatObject whatObject, WhereObject whereObject){
        this.what = whatObject;
        this.where = whereObject;
        this.timestamp = System.currentTimeMillis();
        this.affinity = computeAffinity(whatObject, whereObject);
    }

    //  the affinity does not change over time
    private double computeAffinity(WhatObject whatObject, WhereObject whereObject){
        // we start with an affinity of 0.5
        double localAffinity = 0.5;

        long deltaTimestamp = Math.abs(whatObject.getTimestamp() - whereObject.getTimestamp());
        // Best Affinity is for a Difference of 0, worst for a Difference of 300.000 (Five Minutes)
        // Values up to 150.000 count as bonus (max + 0.25), value above 150.000 as malus (max - 0.25). Values above 300.000 count as 300.000
        //Log.d(this.getClass().toString(), "deltaTimestamp" + deltaTimestamp);
        if (deltaTimestamp > 300000) {deltaTimestamp = 300000;}

        double modifier = Math.cos(((double) 300000) / Math.PI) * 0.25;
        localAffinity += modifier;


        // bonus if both objects have the same source. Thought to favor e.g. place and object from one SMS
        if(whatObject.getSource().equals(whereObject.getSource())){
            localAffinity += 0.15;
        }

        return localAffinity;
    }

    public double getAffinity(){
        return affinity;
    }

    public double getImportance(){
        return what.getImportance() + where.getImportance() + affinity;
    }

    public double getTimestamp(){
        return timestamp;
    }


    public WhatObject getWhatObject(){
        return what;
    }

    public WhereObject getWhereObject(){
        return where;
    }

    @Override
    public String toString() {
        return where.toString() + " " + what.toString() + " Importance: " + this.getImportance();
    }
}
