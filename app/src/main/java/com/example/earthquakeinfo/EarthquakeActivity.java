package com.example.earthquakeinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.os.Bundle;
import android.preference.PreferenceManager;


import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements LoaderCallbacks<List<Earthquake>>,
        SharedPreferences.OnSharedPreferenceChangeListener{



    // textview that is displayed when the list is empty
    private TextView emptyStateTV;

    // adapter for the list of earthquakes
    private EarthquakeAdapter adapter;

    // constant value for the earthquake loader ID
    private static final int EARTHQUKE_LOADER_ID =  1;

    // URL for earthquake data from USGS
    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&minmagnitude=4.0";
    // https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2014-01-01&endtime=2014-01-02
    //
    // "https://earthquake.usgs.gov/fdsnws/event/1/query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);


        // find a reference to the list in layout
        ListView earthquakeListView = findViewById(R.id.list);

        emptyStateTV = findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(emptyStateTV);

        // create an adapter that takes an empty list of earthquake as input
       adapter = new EarthquakeAdapter(this,new ArrayList<Earthquake>());

       // set the adapter in listview
        earthquakeListView.setAdapter(adapter);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);


        // set an item click listener on the listview, which sends an intent to the web browser
        // to open a website with  more information about selected earthquake
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //find the current earthquake that was clicked on
                Earthquake currentEarthquake = adapter.getItem(position);

                //convert the string url into a Uri object
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                //create a new intent to view the earthquake uri
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW,earthquakeUri);

                //send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });


        // get a reference to the ConnectivityManager to check state of network connectivity

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            // get the reference to the LoadManager , in order to interact
            LoaderManager loaderManager =  getLoaderManager();

            /*
                initialize the loader. pass in the int ID constant
             */

            loaderManager.initLoader(EARTHQUKE_LOADER_ID, null, this);

        }else {
            // hide loading indicator so error message will be visible
            View loadingIndicator =  findViewById(R.id.lloading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTV.setText(R.string.no_internet_connection);
        }




//        EarthquakeAsyncTask task = new EarthquakeAsyncTask();
//        task.execute(USGS_REQUEST_URL);
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPreferences.getString(
                getString(R.string.setting_min_magnitude_key),
                getString(R.string.setting_min_magnitude_default));

        String orderBy = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder builder = baseUri.buildUpon();

        builder.appendQueryParameter("format","geojson");
        builder.appendQueryParameter("limit","25");
        builder.appendQueryParameter("minmag",minMagnitude);
        builder.appendQueryParameter("orderby",orderBy);


        return new EarthquakeLoader(this,builder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {


//        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//        if (networkInfo == null){
//            emptyStateTV.setText(R.string.no_internet_connection);
//        }else if (networkInfo.isConnected()){
//            emptyStateTV.setText(R.string.no_earthquakes);
//        }

        // hide loading indicator because data has been loaded
        View loadingIndicator =  findViewById(R.id.lloading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // set empty state txt to display that no quake if found
        emptyStateTV.setText(R.string.no_earthquakes);


        // clear adapter of previous earthquake data
        // adapter.clear();

        //// if there  is a valid list of earthquakes then add them to the adapter. This will trigger list to update
        if (earthquakes != null && !earthquakes.isEmpty()){
            //comment this to show no earthquakes message
            adapter.addAll(earthquakes);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {

        adapter.clear();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.setting_min_magnitude_key)) || s.equals(getString(R.string.settings_order_by_key))){
            adapter.clear();

            emptyStateTV.setVisibility(View.GONE);

            View loadingIndicator = findViewById(R.id.lloading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            getLoaderManager().restartLoader(EARTHQUKE_LOADER_ID, null,this);
        }

    }




}
