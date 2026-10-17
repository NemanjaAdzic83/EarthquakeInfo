package com.example.earthquakeinfo;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.earthquakeinfo.databinding.ActivityEarthquakeBinding;


public class EarthquakeActivity extends AppCompatActivity {


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

        // show the earthquakes whenever the ViewModel delivers a new list
        viewModel = new ViewModelProvider(this).get(EarthquakeViewModel.class);
        viewModel.getUiState().observe(this, this::render);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Called on first start and when returning from Settings:
        // downloads data only if there is none yet or the settings have changed
        viewModel.loadIfNeeded();
    }

    private void render(EarthquakeUiState state){
        adapter.submitList(state.getEarthquakes());

        binding.loadingIndicator.setVisibility(state.getStatus() == EarthquakeUiState.Status.LOADING ? View.VISIBLE : View.GONE);

        switch (state.getStatus()){
            case EMPTY -> showMessage(R.string.no_earthquakes);
            case NO_INTERNET -> showMessage(R.string.no_internet_connection);
            case ERROR -> showMessage(R.string.error_loading);
            default -> binding.emptyView.setVisibility(View.GONE);
        }
    }


    // Shows a message in the middle of the screen instead of the list
    private void showMessage(@StringRes int messageResId) {
        binding.emptyView.setText(messageResId);
        binding.emptyView.setVisibility(View.VISIBLE);
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