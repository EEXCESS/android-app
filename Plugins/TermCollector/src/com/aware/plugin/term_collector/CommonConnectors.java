package com.aware.plugin.term_collector;

import android.content.Context;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wmb on 08.01.14.
 */
public class CommonConnectors {
    private List<String> commonConnectors;

    public CommonConnectors(Context context){
        this.commonConnectors = new ArrayList<String>();

        InputStream buildinginfo = context.getResources().openRawResource(R.raw.commonconnectors);
        DataInputStream myDIS = new DataInputStream(buildinginfo);
        String myLine;

       //now loop through and check if we have input, if so append it to list
        try{
            while((myLine=myDIS.readLine())!=null) commonConnectors.add(myLine);
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    public boolean isCommonConnector(String token){
        return commonConnectors.contains(token);
    }
}
