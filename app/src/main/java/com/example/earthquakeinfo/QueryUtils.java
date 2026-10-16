package com.example.earthquakeinfo;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    // tag for the LOG messages
    public static final String LOG_TAG = "QueryUtils";

    // Networks timeouts in milliseconds
    private static final int READ_TIMEOUT_MS = 10000;
    private static final int CONNECT_TIMEOUT_MS = 15000;


/*
    Private constructor is created that prevent creating instances of this class, all variables and method are static and
    that means that they could be called directly without creating objects of class
 */
    private QueryUtils(){

    }

    /*

     */
    public static List<Earthquake> fetchEarthquakeData(String requestUrl){

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        }catch (IOException e){
            Log.e(LOG_TAG, "Problem making the HTTP request",e);
        }
        return extractEarthquakesFromJson(jsonResponse);
    }



    /*
        Returns new URL object from the given string URL
     */
    private static URL createUrl(String stringUrl){
        try {
            return new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG,"Invalid URL: " + stringUrl,e);
            return null;
        }
    }

    /*
        Make http request to the given URL and return a String as the response
     */
    private static String makeHttpRequest(URL url) throws IOException {

        //if the url is null, then return early
        if (url == null){
           return "";
        }

        HttpURLConnection urlConnection = null;

        try {
            // settings http request
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIMEOUT_MS);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            urlConnection.setRequestMethod("GET");
            //everything before connection was setting up the HTTP request
            urlConnection.connect(); // established the HTTP connection with server

            //everything after connection ( .connect() ) is about receiving and response

            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK){
                Log.e(LOG_TAG,"Error response code: " + responseCode);
                return "";
            }
            try (InputStream inputStream = urlConnection.getInputStream()){
                return readFromStream(inputStream);
            }
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
        }
    }

    /*
        Convert InputStream into a String  which contains the whole JSON response from the server
     */
    private static String readFromStream(InputStream inputStream) throws IOException {

        // use StringBuilder because it is mutable and gradually build response string
        StringBuilder output = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null){
            output.append(line);
        }
        return output.toString();

        }

        // Parses the JSON response from USGS and returns a list of Earthquake object
    // Returns an empty list if the JSON is empty or invalid
    private static List<Earthquake> extractEarthquakesFromJson(String earthquakeJson) {
        List<Earthquake> earthquakes = new ArrayList<>();

        if (TextUtils.isEmpty(earthquakeJson)){
            return earthquakes;
        }

        try {
            JSONObject root = new JSONObject(earthquakeJson);
            JSONArray features = root.getJSONArray("features");

            for (int i = 0; i < features.length(); i++){
                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");

                if (properties.isNull("mag")){
                    continue;
                }
                double magnitude = properties.getDouble("mag");
                String place = properties.isNull("place") ? "" : properties.getString("place");
                long time = properties.getLong("time");
                String url = properties.optString("url");

                earthquakes.add(new Earthquake(magnitude, place, time, url));
            }
        }catch (JSONException e){
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results",e);
        }
        return earthquakes;
    }


    }





