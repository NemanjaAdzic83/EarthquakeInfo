package com.example.earthquakeinfo;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;
/*
    custom loader that fetches a list of Earthquake objects with AsyncTask
 */
public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

    // Query URL
    private String mUrl;


    // Constructor (construct a new loader)
    public EarthquakeLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    // this work on background thread and returns a list of earthquakes
    @Override
    public List<Earthquake> loadInBackground() {

        if (mUrl == null){
            return null;
        }


        // perform the network request , parse the response and extract a list of earthquakes
        List<Earthquake> earthquakes = QueryUtils.fetchEarthquakeData(mUrl);
        return earthquakes;
    }
}
