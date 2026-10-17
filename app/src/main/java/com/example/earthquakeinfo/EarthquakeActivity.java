package com.example.earthquakeinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.example.earthquakeinfo.databinding.ActivityEarthquakeBinding;

import java.net.URL;
import java.util.Collections;
import java.util.Locale;

public class EarthquakeActivity extends AppCompatActivity {

    // URL for earthquake data from USGS (query parameters are added in buildRequestUrl)
    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    // Generated from activity_earthquake.xml - gives direct access to all views of the screen
    private ActivityEarthquakeBinding binding;

    // adapter for the list of earthquakes
    private EarthquakeAdapter adapter;

    // loads earthquakes in the background and keeps them across screen rotation
    private EarthquakeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        super.onCreate(savedInstanceState);
        binding = ActivityEarthquakeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Push the toolbar below the status bar and keep the last item above the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            binding.list.setPadding(bars.left, 0, bars.right, bars.bottom);
            return windowInsets;
        });

        adapter = new EarthquakeAdapter(this, earthquake -> {
            Uri earthquakeUri = Uri.parse(earthquake.getUrl());
            startActivity(new Intent(Intent.ACTION_VIEW,earthquakeUri));
        });
        binding.list.setAdapter(adapter);

        // thin line between the rows
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.list_divider);
        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }
        binding.list.addItemDecoration(divider);

        // show the earthquakes whenever the ViewModel delivers a new list
        viewModel = new ViewModelProvider(this).get(EarthquakeViewModel.class);
        viewModel.getEarthquakes().observe(this, earthquakes -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            adapter.submitList(earthquakes);
            if (earthquakes == null || earthquakes.isEmpty()) {
                showMessage(R.string.no_earthquakes);
            } else {
                binding.emptyView.setVisibility(View.GONE);
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
            if (adapter.getItemCount() == 0) {
                binding.loadingIndicator.setVisibility(View.GONE);
                showMessage(R.string.no_internet_connection);
            }
            // try again next time the screen is shown
            viewModel.reset();
            return;
        }

        if (viewModel.load(buildRequestUrl())) {
            // a new request was started: clear the old list and show the loading indicator
            adapter.submitList(Collections.emptyList());
            binding.emptyView.setVisibility(View.GONE);
            binding.loadingIndicator.setVisibility(View.VISIBLE);
        }
    }

    // Shows a message in the middle of the screen instead of the list
    private void showMessage(@StringRes int messageResId) {
        binding.emptyView.setText(messageResId);
        binding.emptyView.setVisibility(View.VISIBLE);
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