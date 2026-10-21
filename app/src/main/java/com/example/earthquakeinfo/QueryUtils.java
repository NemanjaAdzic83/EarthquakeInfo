package com.example.earthquakeinfo;

import androidx.annotation.VisibleForTesting;

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
import java.util.Optional;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    // Networks timeouts in milliseconds
    private static final int READ_TIMEOUT_MS = 20000;
    private static final int CONNECT_TIMEOUT_MS = 15000;


    /*
        Private constructor is created that prevent creating instances of this class, all variables and method are static and
        that means that they could be called directly without creating objects of class
     */
    private QueryUtils(){

    }

    /*
        Downloads the earthquake data from the given URL and returns a list of earthquakes.
        Must be called on a background thread.
        Throws IOException if there is no answer from the server or the answer is invalid,
        so the caller can show an error message instead of an empty list.
     */
    public static List<Earthquake> fetchEarthquakeData(String requestUrl) throws IOException {
        URL url = createUrl(requestUrl);
        String jsonResponse = makeHttpRequest(url);
        return extractEarthquakesFromJson(jsonResponse);
    }



    /*
        Returns new URL object from the given string URL
     */
    private static URL createUrl(String stringUrl) throws IOException {
        try {
            return new URL(stringUrl);
        } catch (MalformedURLException e) {
            throw new IOException("Invalid URL: " + stringUrl, e);
        }
    }

    /*
        Make http request to the given URL and return a String as the response
     */
    private static String makeHttpRequest(URL url) throws IOException {

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
                throw new IOException("Error response code: " + responseCode);
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
    // Returns an empty list if the JSON is empty, throws IOException if it is invalid
    @VisibleForTesting
    static List<Earthquake> extractEarthquakesFromJson(String earthquakeJson) throws IOException {
        List<Earthquake> earthquakes = new ArrayList<>();

        if (earthquakeJson == null || earthquakeJson.isEmpty()){
            return earthquakes;
        }

        try {
            JSONObject root = new JSONObject(earthquakeJson);
            JSONArray features = root.getJSONArray("features");

            for (int i = 0; i < features.length(); i++){
                JSONObject feature = features.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");

                if (properties.isNull("mag")){
                    continue;
                }
                double magnitude = properties.getDouble("mag");
                String place = properties.isNull("place") ? "" : properties.getString("place");
                long time = properties.getLong("time");
                String url = properties.optString("url");
                boolean tsunami = properties.optInt("tsunami",0) == 1;

                // coordinates: longitude, latitude, depth
                // GeoJSON puts LONGITUDE first, than LATITUDE
                double longitude = Double.NaN;
                double latitude = Double.NaN;
                double depth = Double.NaN;

                JSONObject geometry = feature.optJSONObject("geometry");
                if (geometry != null){
                    JSONArray coordinates = geometry.optJSONArray("coordinates");
                    if (coordinates != null && coordinates.length() >= 2){
                        longitude = coordinates.optDouble(0,Double.NaN);
                        latitude = coordinates.optDouble(1,Double.NaN);
                        depth = coordinates.optDouble(2,Double.NaN);
                    }
                }

                earthquakes.add(new Earthquake(magnitude, place, time, url,latitude,longitude,depth,tsunami));
            }
        }catch (JSONException e){
            throw new IOException("Problem parsing the earthquake JSON results", e);
        }
        return earthquakes;
    }


}