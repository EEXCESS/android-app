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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Geonames {

    private Connection connection;

    public Geonames(Connection connection) {
        this.connection = connection;
    }



    public Response getCity(String cityName){

        return connection.run(
                "[ g | g <- geonames, g.featureClass == `{1}` && g.name == \"{2}\" ]"
                        .replace("{1}",String.valueOf("P"))
                        .replace("{2}",String.valueOf(cityName))
        );
    }

    /**
     * There are 0.621371 kilometers per mile.
     *
     * @param lat
     * @param lng
     * @param maxDistanceKm
     * @return geonames data for all place within a radius of maxDistanceKm
     */
    public Response getPlacesNearby(float lat, float lng, float maxDistanceKm){
        return connection.run(
                "[ g | g <- geonames, dist({1}, {2}, g.lat, g.lon) < {3} ]"
                    .replace("{1}",String.valueOf(lat))
                    .replace("{2}",String.valueOf(lng))
                    .replace("{3}",String.valueOf(maxDistanceKm)));
    }

    /**
     * See http://www.geonames.org/export/codes.html for a full list of featureClass
     *
     * @param lat
     * @param lng
     * @param maxDistanceKm
     * @param featureClass
     * @return
     */
    public Response getPlacesNearbyOfClass(float lat, float lng, float maxDistanceKm, String featureClass){
        return connection.run(
                "[ g | g <- geonames, dist({1}, {2}, g.lat, g.lon) < {3} && g.featureClass == `{4}` ]"
                        .replace("{1}",String.valueOf(lat))
                        .replace("{2}", String.valueOf(lng))
                        .replace("{3}", String.valueOf(maxDistanceKm))
                        .replace("{4}", String.valueOf(featureClass))
        );
    }

    /**
     * See http://www.geonames.org/export/codes.html for a full list of featureCodes
     *
     * @param lat
     * @param lng
     * @param maxDistanceKm
     * @param featureCode
     * @return
     */
    public Response getPlacesNearbyOfType(float lat, float lng, float maxDistanceKm, String featureCode){
        return connection.run(
                "[ g | g <- geonames, dist({1}, {2}, g.lat, g.lon) < {3} && g.featureCode == `{4}` ]"
                        .replace("{1}",String.valueOf(lat))
                        .replace("{2}",String.valueOf(lng))
                        .replace("{3}",String.valueOf(maxDistanceKm))
                        .replace("{4}",String.valueOf(featureCode))
        );
    }

    /**
     *
     * @param lat
     * @param lng
     * @param maxDistanceKm
     * @param regex
     * @return
     */
    public Response getPlacesNearbyRegex(float lat, float lng, float maxDistanceKm, String regex){
        return connection.run(
                "[ g | g <- geonames, g.name =~ {4}, dist({1}, {2}, g.lat, g.lon) < {3}]"
                        .replace("{1}",String.valueOf(lat))
                        .replace("{2}",String.valueOf(lng))
                        .replace("{3}",String.valueOf(maxDistanceKm))
                        .replace("{4}",String.valueOf(regex))
        );
    }

    /**
     * Returns the nearest postal codes within maxDistanceKm
     * @param lat
     * @param lng
     * @param maxDistanceKm
     * @return
     */
    public Response getPostalCodesNearby(float lat, float lng, float maxDistanceKm){
        return connection.run(
                "[ p | p <- postalcodes, dist({1}, {2}, p.lat, p.lon) < {3} ]"
                        .replace("{1}", String.valueOf(lat))
                        .replace("{2}",String.valueOf(lng))
                        .replace("{3}",String.valueOf(maxDistanceKm))
        );
    }

    /**
     * Returns all matching records of featureClass == P (Populated Place)
     * @param regexCityName
     * @return
     */
    public Response getCityRegex(String regexCityName){
        return connection.run(
                "[ g | g <- geonames, g.featureClass == `{1}` && g.altnames =~ `{2}` ]"
                        .replace("{1}",String.valueOf("P"))
                        .replace("{2}",String.valueOf(regexCityName))
        );
    }

    /**
     * Returns the matching record from the countryinfo data given a 2 or 3 character ISO code
     * @param countryCode
     * @return
     */
    public Response getCountryInfo(String countryCode){
        return connection.run(
                "[ c | c <- countryinfo, c.iso == `{1}` || c.iso3 == `{1}` ]"
                        .replace("{1}",String.valueOf(countryCode))
        );
    }

    /**
     * Returns all time zones given a 2 or 3 character ISO code
     * @param countryCodeIso2
     * @return
     */
    public Response getTimeZoneForCountry(String countryCodeIso2){
        return connection.run(
                "[ tz | tz <- timezones, tz.countryCode == `{1}` ]"
                        .replace("{1}",String.valueOf(countryCodeIso2))
        );
    }

    public float getDistanceInMeters(float lat1, float lng1, float lat2, float lng2){ return 0f;}


    public boolean existsPopulatedPlaceWithName(String name){
        Response response = getCity(name);
        if(response != null) {
            return response.size() > 0;
        } else {
            Log.wtf("GEONAMES", "Response was NULL!");
            return false;
        }

    }

    public Set<String> areCities(List<String> names){

        StringBuilder sb = new StringBuilder();
        sb.append("g.name == `"+ names.get(0) +"`");

        if(names.size() > 1){
            for (int i = 1; i < names.size(); i++) {
                sb.append(" || g.name == `"+ names.get(i) +"`");
            }
        }

        String query = "[ g.name | g <~ geonames, g.population > 1000 && g.featureClass == \"P\" && ( {1} ) ]"
                .replace("{1}", sb.toString());
        System.out.println("Query " + query);

        Response response =  connection.run(query);


        Set<String> result = new HashSet<String>();

        try {

            //System.out.println("Response " + response);
            JSONArray jsonArray = new JSONObject(response.toJSONString()).getJSONArray("result");


        for(int i=0; i < jsonArray.length(); i++) {
            System.out.println("Adding " + jsonArray.get(i).toString());
            result.add(jsonArray.get(i).toString());
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /* V1.1
    public float[] getDistanceInMeters(float lat, float lng, String[] placeIds){
    public io.mingle.v1.Response getTimeZone(float lat, float lng){ return null;}
    public io.mingle.v1.Response getTimeZoneRegex(String regex){ return null;}
    */
}


