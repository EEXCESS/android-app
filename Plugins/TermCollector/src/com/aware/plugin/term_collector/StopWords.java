package com.aware.plugin.term_collector;

import android.content.Context;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by wmb on 08.01.14.
 */
public class StopWords {
    private HashSet<String> stopwords;

    public StopWords(Context context){
        //GERMAN STOPWORDS by  Marco Götze, Steffen Geyer: http://solariz.de/649/deutsche-stopwords.htm;
        this.stopwords = new HashSet<String>();

        InputStream buildinginfo = context.getResources().openRawResource(R.raw.stopwords);
        DataInputStream myDIS = new DataInputStream(buildinginfo);
        String myLine;

        //now loop through and check if we have input, if so append it to list
        try{
            while((myLine=myDIS.readLine())!=null) stopwords.add(myLine);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public String[] filteredArray(String[] arrayToFilter) {
    ArrayList<String> resultList = new ArrayList<String>();
        for(String token : arrayToFilter ){
            if(!stopwords.contains(token.toLowerCase())){
                resultList.add(token);
            }
        }
    String[] result = new String[resultList.size()];
    return(resultList.toArray(result));
    }

    public boolean isStopWord(String token){
        return stopwords.contains(token.toLowerCase());
    }
}
