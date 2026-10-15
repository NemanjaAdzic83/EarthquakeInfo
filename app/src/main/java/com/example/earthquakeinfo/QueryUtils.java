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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    // tag for the LOG messages
    public static final String LOG_TAG = EarthquakeActivity.class.getName();


/*
    Private constructor is created that prevent creating instances of this class, all variables and method are static and
    that means that they could be called directly without creating objects of class
 */
    private QueryUtils(){

    }

    /*

     */
    public static List<Earthquake> extractEarthquakeFromJson(String earthquakeJson){


        // if the JSON string empty return null
        if (TextUtils.isEmpty(earthquakeJson)){
            return null;
        }


        /*
            Creating an empty List that we can put all the earthquakes to
        */
        List<Earthquake> earthquakes = new ArrayList<>();

        // Parsing the JSON string

        // If there's a problem with the way the JSON is formatted, a JSONException exception object will be thrown
        // and that is reason why it's used TRY-CATCH block
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // create a json object from the response string
            JSONObject jsonObject = new JSONObject(earthquakeJson);


            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features (earthquakes).
            JSONArray jsonArray = jsonObject.getJSONArray("features");


            // with for loop we iterate through an array
            // For each earthquake in the earthquakeArray, create an Earthquake object
            for (int i = 0; i < jsonArray.length(); i++){
                // Get a single earthquake at position "i" within the list of earthquakes
                JSONObject currentEarthquake = jsonArray.getJSONObject(i);

                // For a given earthquake, extract the JSONObject associated with the
                // key called "properties", which represents a list of all properties
                // for that earthquake.
                JSONObject properties = currentEarthquake.getJSONObject("properties");

                // Extract the value for the key called "mag"
                double magnitude = properties.getDouble("mag");

                // Extract the value for the key called "place"
                String place = properties.getString("place");

                // Extract the value for the key called "time"
                long time = properties.getLong("time");


                // Extract the value for the key called "url"
                String  url = properties.getString("url");

                // Create a new Earthquake object with the magnitude, location, time and url from the JSON response.
                Earthquake earthquake = new Earthquake(magnitude, place, time, url);

                // Add the new earthquake to the list of earthquakes.
                earthquakes.add(earthquake);
            }


        }catch (JSONException e){
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils","Problem parsing the earthquake  JSON results",e );
        }

        // Return teh earthquakes list
        return earthquakes;
    }


    /*
        Returns new URL object from the given string URL
     */
    private static URL createUrl(String stringUrl){
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /*
        Make http request to the given URL and return a String as the response
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        //if the url is null, than return early
        if (url == null){
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            // settings http request
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");


            //everything before connection was setting up the HTTP request

            urlConnection.connect(); // established the HTTP connection with server

            //everything after connection ( .connect() ) is about receiving and response


            /*
                -check http response codes
                if the request was successful response code 200, than read the input stream and parse response
             */
            if (urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }else {
                Log.e(LOG_TAG,"Error response code: " + urlConnection.getResponseCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }

            if (inputStream != null){
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    /*
        convert inputstream into a String  which contains the whole JSON response from the server
     */
    private static String readFromStream(InputStream inputStream) throws IOException {

        // use StringBuilder because it is mutable and gradually build response string
        StringBuilder output = new StringBuilder();

        if (inputStream != null){
            // inputstream represents a stream of bytes(small chunks of data), inputstream allows our code to process one byte at the time
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            // buffered reader help us to read text from an Inputstream, we can read one line of the text at a time and then build a string with entire
            // contents of the inputstream
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();

            while (line != null){
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    public static List<Earthquake> fetchEarthquakeData(String requestUrl){
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        //Log.i(LOG_TAG, "TEST: fetchEarthquakeData() called ...");

        URL url = createUrl(requestUrl);

        String jsonResponse = null;

        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.i(LOG_TAG, "Problem making a HTTP request",e);
        }

        List<Earthquake> earthquakes = extractEarthquakeFromJson(jsonResponse);

        return earthquakes;
    }


}

