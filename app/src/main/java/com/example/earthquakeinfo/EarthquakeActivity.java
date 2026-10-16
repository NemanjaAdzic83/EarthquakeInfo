package com.example.earthquakeinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Locale;

public class EarthquakeActivity extends AppCompatActivity {

    // URL for earthquake data from USGS (query parameters are added in buildRequestUrl)
    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    // textview that is displayed when the list is empty
    private TextView emptyStateTV;

    // loading indicator shown while data is being downloaded
    private View loadingIndicator;

    // adapter for the list of earthquakes
    private EarthquakeAdapter adapter;

    // loads earthquakes in the background and keeps them across screen rotation
    private EarthquakeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // find a reference to the list in layout
        ListView earthquakeListView = findViewById(R.id.list);

        // Push the toolbar below the status bar and keep the last item above the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            earthquakeListView.setPadding(bars.left, 0, bars.right, bars.bottom);
            return windowInsets;
        });

        emptyStateTV = findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(emptyStateTV);

        loadingIndicator = findViewById(R.id.lloading_indicator);

        // create an adapter that takes an empty list of earthquakes as input
        adapter = new EarthquakeAdapter(this, new ArrayList<>());
        earthquakeListView.setAdapter(adapter);

        // open the USGS web page with more details when an earthquake is clicked
        earthquakeListView.setOnItemClickListener((parent, view, position, id) -> {
            Earthquake currentEarthquake = adapter.getItem(position);
            if (currentEarthquake != null) {
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());
                startActivity(new Intent(Intent.ACTION_VIEW, earthquakeUri));
            }
        });

        // show the earthquakes whenever the ViewModel delivers a new list
        viewModel = new ViewModelProvider(this).get(EarthquakeViewModel.class);
        viewModel.getEarthquakes().observe(this, earthquakes -> {
            loadingIndicator.setVisibility(View.GONE);
            emptyStateTV.setText(R.string.no_earthquakes);

            adapter.clear();
            if (earthquakes != null && !earthquakes.isEmpty()) {
                adapter.addAll(earthquakes);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Called on first start and when returning from Settings:
        // downloads data only if there is none yet or the settings have changed
        loadEarthquakes();
    }

    private void loadEarthquakes() {
        if (!isOnline()) {
            if (adapter.isEmpty()) {
                loadingIndicator.setVisibility(View.GONE);
                emptyStateTV.setText(R.string.no_internet_connection);
            }
            // try again next time the screen is shown
            viewModel.reset();
            return;
        }

        if (viewModel.load(buildRequestUrl())) {
            // a new request was started: clear the old list and show the loading indicator
            adapter.clear();
            emptyStateTV.setText("");
            loadingIndicator.setVisibility(View.VISIBLE);
        }
    }

    // Builds the USGS query URL from the user's settings
    private String buildRequestUrl() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String minMagnitude = sharedPreferences.getString(
                getString(R.string.setting_min_magnitude_key),
                getString(R.string.setting_min_magnitude_default));
        if (TextUtils.isEmpty(minMagnitude)) {
            minMagnitude = getString(R.string.setting_min_magnitude_default);
        }

        String orderBy = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        // older versions of the app could have saved "Magnitude" instead of "magnitude"
        orderBy = orderBy.toLowerCase(Locale.ROOT);

        return Uri.parse(USGS_REQUEST_URL).buildUpon()
                .appendQueryParameter("format", "geojson")
                .appendQueryParameter("limit", "25")
                .appendQueryParameter("minmagnitude", minMagnitude)
                .appendQueryParameter("orderby", orderBy)
                .build()
                .toString();
    }

    // Returns true if the device has a working internet connection
    private boolean isOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}