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

import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;


public class Connection {
    private final URL url;
    private final Context context;

    public Connection(String url, Context context) throws Exception {
        this.url = new URL(url);
        this.context = context;
    }

    public Response run(String comprehension) {
        String expr = "{ \"expr\": \"" + comprehension + "\", \"limit\": 10 }";
//        try {
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//
//            conn.connect();
//
//            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
//            out.write(expr, 0, expr.length());
//            out.flush();
//            out.close();
//
//            InputStream in = conn.getInputStream();
//            ByteArrayOutputStream buf = new ByteArrayOutputStream();
//            byte[] chunk = new byte[4096];
//            int read = 0;
//            while ((read = in.read(chunk)) > 0) {
//                buf.write(chunk, 0, read);
//            }
//            in.close();
//
//            String str = buf.toString();
//            //System.out.println("GOT JSON: "+str);
//            return new Response(JSONValue.parse(str));
//        } catch (Exception e) {
//            System.err.printf("failed to execute: %s\n", expr);
//            e.printStackTrace();
//        }


        // Instantiate the custom HttpClient
        DefaultHttpClient client = new MingleIOHttpClient(context);
        HttpPost post = new HttpPost("https://data.mingle.io");

        // Build the JSON object to pass parameters
        JSONObject jsonObj = new JSONObject();
        System.err.println(expr);
        try {
            jsonObj.put("expr", comprehension);
            jsonObj.put("limit", 10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create the POST object and add the parameters
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        entity.setContentType("application/json");
        post.setEntity(entity);

// Execute the GET call and obtain the response
        try{
        HttpResponse getResponse = client.execute(post);
        HttpEntity responseEntity = getResponse.getEntity();

            if (responseEntity != null) {
                String retSrc = EntityUtils.toString(responseEntity);
                System.err.println(retSrc);
                // parsing JSON
                return new Response( new JSONObject(retSrc)); //Convert String to JSON Object
            }

        } catch (Exception e) {
            System.err.printf("failed to execute: %s\n", expr);
            e.printStackTrace();
        }
        return null;
    }
}
