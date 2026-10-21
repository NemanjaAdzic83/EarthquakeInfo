package com.example.earthquakeinfo;


import android.content.Intent;
import android.graphics.Color;
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

        adapter = new EarthquakeAdapter(this, this::showDetails);
        binding.list.setAdapter(adapter);

        //Pull down to refresh , always download fresh data
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.color_surface);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        // Try again button below the error message
        binding.retryButton.setOnClickListener( v -> viewModel.refresh());

        // show the earthquakes whenever the ViewModel delivers a new list
        viewModel = new ViewModelProvider(this).get(EarthquakeViewModel.class);
        viewModel.getUiState().observe(this, this::render);
    }

    private void showDetails(Earthquake earthquake) {
        if (getSupportFragmentManager().findFragmentByTag(EarthquakeDetailsBottomSheet.TAG) == null){
            EarthquakeDetailsBottomSheet.newInstance(earthquake)
                    .show(getSupportFragmentManager(),EarthquakeDetailsBottomSheet.TAG);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Called on first start and when returning from Settings:
        // downloads data only if there is none yet or the settings have changed
        viewModel.loadIfNeeded();
    }

    /* Draws the screen for the given state
       This is the  only place that change the visibility of the list, message and indicators
     */
    private void render(EarthquakeUiState state){
        adapter.submitList(state.getEarthquakes());

        boolean loading = state.getStatus() == EarthquakeUiState.Status.LOADING;

        // The pull-to-refresh spinner disappears as soon as loading is finished
        if (!loading){
            binding.swipeRefresh.setRefreshing(false);
        }

        boolean showCenterIndicator = loading && !binding.swipeRefresh.isRefreshing() && state.getEarthquakes().isEmpty();
        binding.loadingIndicator.setVisibility(showCenterIndicator ? View.VISIBLE : View.GONE);



        switch (state.getStatus()){
            case EMPTY -> showMessage(R.string.no_earthquakes,false);
            case NO_INTERNET -> showMessage(R.string.no_internet_connection,true);
            case ERROR -> showMessage(R.string.error_loading,true);
            default -> binding.messageContainer.setVisibility(View.GONE);
        }
    }


    // Shows a message in the middle of the screen instead of the list, with the "Try button" when it makes sense to retry
    private void showMessage(@StringRes int messageResId, boolean showRetry) {
        binding.emptyView.setText(messageResId);
        binding.retryButton.setVisibility(showRetry ? View.VISIBLE : View.GONE);
        binding.messageContainer.setVisibility(View.VISIBLE);
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