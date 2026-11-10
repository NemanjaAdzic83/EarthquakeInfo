package com.example.earthquakeinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class EarthquakeRepository {

    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    // How many earthquakes to download
    private static final String RESULT_LIMIT = "25";

    // Near me filter =: radius around the saved location
    private static final String NEAR_ME_RADIUS_KM = "500";

    private static final String KEY_NEAR_ME = "near_me";
    private static final String KEY_LATITUDE = "near_me_latitude";
    private static final String KEY_LONGITUDE = "near_me_longitude";

    private final Context context;
    private static final DateTimeFormatter USGS_TIME =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

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

    //Number of days back from now : 1, 7 and  30 days (chips on the main screen)
    public int getPeriodDays(){
        return preferences().getInt(
                context.getString(R.string.setting_period_key),
                context.getResources().getInteger(R.integer.setting_period_default));
    }

    public void setPeriodDays(int days){
        preferences().edit().putInt(context.getString(R.string.setting_period_key),days).apply();
    }

    @VisibleForTesting
    static String startTimeFor(int days, long nowMillis) {
        Instant start = Instant.ofEpochMilli(nowMillis)
                .minus(days, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.HOURS);
        return USGS_TIME.format(start);
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

         Uri.Builder builder = Uri.parse(USGS_REQUEST_URL).buildUpon()
                .appendQueryParameter("format","geojson")
                .appendQueryParameter("limit",RESULT_LIMIT)
                .appendQueryParameter("minmagnitude", String.valueOf(minMagnitude))
                .appendQueryParameter("orderby", orderBy)
                .appendQueryParameter("starttime",startTimeFor(getPeriodDays(),System.currentTimeMillis()));

         if (isNearMe()){
             SharedPreferences preferences1 = preferences;
             builder.appendQueryParameter("latitude",String.valueOf(preferences1.getFloat(KEY_LATITUDE,0f)))
                     .appendQueryParameter("longitude",String.valueOf(preferences1.getFloat(KEY_LONGITUDE,0f)))
                     .appendQueryParameter("maxradiuskm",NEAR_ME_RADIUS_KM);
         }


         return builder.build().toString();

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

    // True if the user turned on "ner me" and we already hava a location
    public boolean isNearMe(){
        SharedPreferences preferences = preferences();
        return preferences.getBoolean(KEY_NEAR_ME,false) && preferences.contains(KEY_LATITUDE);
    }

    public void setNearMe(boolean enabled){
        preferences().edit().putBoolean(KEY_NEAR_ME,enabled).apply();
    }

    // Saves the location to -10km and turns near me on
    public void serNearMeLocation(double latitude, double longitude){
        preferences().edit()
                .putBoolean(KEY_NEAR_ME,true)
                .putFloat(KEY_LATITUDE, (float) roundToTenth(latitude))
                .putFloat(KEY_LONGITUDE,(float) roundToTenth(longitude))
                .apply();
    }

    @VisibleForTesting
    static double roundToTenth(double value) {
        return Math.round(value * 10) / 10.0;
    }


}
