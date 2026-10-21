package com.example.earthquakeinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EarthquakeRepository {

    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    // How many earthquakes to download
    private static final String RESULT_LIMIT = "25";

    private final Context context;

    public EarthquakeRepository(Context context){
        this.context = context.getApplicationContext();
    }

    // Minimum magnitude chosen by the user(slider in settings or chips on the main screen)
    public int getMinMagnitude(){
        return preferences().getInt(
                context.getString(R.string.setting_min_magnitude_key),
                context.getResources().getInteger(R.integer.setting_main_magnitude_default));
    }

    // Save a new minimum magnitude - the same value the Setting slider shows
    public void setMinMagnitude(int minMagnitude){
        preferences().edit().putInt(context.getString(R.string.setting_min_magnitude_key),minMagnitude).apply();
    }
    private SharedPreferences preferences(){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getRequestUrl(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int minMagnitude = getMinMagnitude();


        String orderBy = preferences.getString(
                context.getString(R.string.settings_order_by_key),
                context.getString(R.string.settings_order_by_default));

        orderBy = orderBy.toLowerCase(Locale.ROOT);

        return Uri.parse(USGS_REQUEST_URL).buildUpon()
                .appendQueryParameter("format","geojson")
                .appendQueryParameter("limit",RESULT_LIMIT)
                .appendQueryParameter("minmagnitude", String.valueOf(minMagnitude))
                .appendQueryParameter("orderby", orderBy)
                .build()
                .toString();

    }



    public List<Earthquake> fetchEarthquakes(String requestUrl) throws IOException{
        return QueryUtils.fetchEarthquakeData(requestUrl);
    }

    // Returns true if device has a working internet connection
    public boolean isOnline(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null){
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);


    }



}
