package io.mingle.v1;
/* Copyright 2013 Cezar Lotrean

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import android.util.Log;

public class Osmpois {

    private Connection connection;
    private static String Database = "osmpois";
    private static String ResultFormat = "{name: o.name, type: o.type, distance: dist({1}, {2}, o.lat, o.lon) }";

    public Osmpois(Connection connection) {
        this.connection = connection;
    }

    /**
     * There are 0.621371 kilometers per mile.
     *
     * @param lat
     * @param lng
     * @param maxDistanceKm
     * @return geonames data for all place within a radius of maxDistanceKm
     */
    public Response getPoisNearby(float lat, float lng, float maxDistanceKm){

		System.out.println("[ " + ResultFormat + " | o <~ " + Database +", dist({1}, {2}, o.lat, o.lon) < {3}]"
            .replace("{1}",String.valueOf(lat))
            .replace("{2}",String.valueOf(lng))
            .replace("{3}",String.valueOf(maxDistanceKm)));
    	return connection.run(
        		"[ o | o <~ " + Database +", dist({1}, {2}, o.lat, o.lon) < {3}]"
                    .replace("{1}",String.valueOf(lat))
                    .replace("{2}",String.valueOf(lng))
                    .replace("{3}",String.valueOf(maxDistanceKm)));
    }


    /**
     * See http://www.geonames.org/export/codes.html for a full list of featureCodes
     *
     * @param lat
     * @param lng
     * @param maxDistanceKm
     * @param type
     * @return
     */
    public Response getPoisNearbyOfType(float lat, float lng, float maxDistanceKm, String type){
        return connection.run(
                "[ o | " + ResultFormat + " <- " + Database +", dist({1}, {2}, o.lat, o.lon) < {3} && o.type == `{4}` ]"
                        .replace("{1}",String.valueOf(lat))
                        .replace("{2}",String.valueOf(lng))
                        .replace("{3}",String.valueOf(maxDistanceKm))
                        .replace("{4}",String.valueOf(type))
        );
    }
        
    
    /**
     * See http://www.geonames.org/export/codes.html for a full list of featureCodes
     *
     * @param lat
     * @param lng
     * @param maxDistanceKm
     * @param types
     * @return
     */
    public Response getPoisNearbyOfTypes(float lat, float lng, float maxDistanceKm, String... types){
        return connection.run(
                "[ " + ResultFormat + " | o <- " + Database +", dist({1}, {2}, o.lat, o.lon) < {3} && o.featureCode == `{4}` ]"
                        .replace("{1}",String.valueOf(lat))
                        .replace("{2}",String.valueOf(lng))
                        .replace("{3}",String.valueOf(maxDistanceKm))
                        .replace("{4}",String.valueOf(types))
        );
    }

    public Response getPoisNearbyOfRegex(float lat, float lng, float maxDistanceKm, String regex){
        return connection.run(
                "[ " + ResultFormat + " | o <- " + Database +", dist({1}, {2}, o.lat, o.lon) < {3} && o.type =~ `{4}` ]"
                        .replace("{1}",String.valueOf(lat))
                        .replace("{2}",String.valueOf(lng))
                        .replace("{3}",String.valueOf(maxDistanceKm))
                        .replace("{4}",String.valueOf(regex))
        );
    }
    
    public Response getPoisNearbyOfRegexes(float lat, float lng, float maxDistanceKm, String... regexes){
        System.out.println("getPoisNearbyOfRegexes called with lat:" + lat + " and lng: " + lng);
        StringBuilder sb = new StringBuilder();
        sb.append("o.type =~ `"+ regexes[0] +"`");
        
        if(regexes.length > 1){
        	for (int i = 1; i < regexes.length; i++) {
				sb.append("|| o.type =~ `"+ regexes[1] +"`");
			
			}
        }

        String query = "[ " + ResultFormat + " | o <- " + Database +", dist({1}, {2}, o.lat, o.lon) < {3} && ( {4} ) ]"
                .replace("{1}",String.valueOf(lat))
                .replace("{2}",String.valueOf(lng))
                .replace("{3}",String.valueOf(maxDistanceKm))
                .replace("{4}",sb.toString());
    	System.out.println(query);
        return connection.run(query);
    }
}


