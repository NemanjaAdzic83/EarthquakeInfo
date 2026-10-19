package com.example.earthquakeinfo;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    Holds the state of the main screen and survives screen rotation.
    Decides WHEN to load data and turns the result into an EarthquakeUiState.
    The Activity only observes the state and draws it.
 */
public class EarthquakeViewModel extends AndroidViewModel {

    private static final String LOG_TAG = "EarthquakeViewModel";

    private final EarthquakeRepository repository;

    // One background thread for network requests
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<EarthquakeUiState> uiState = new MutableLiveData<>();

    // URL of the last request, used to detect changed settings and to ignore outdated results
    private volatile String lastUrl;

    // AndroidViewModel receives the Application, which the repository needs for settings and network
    public EarthquakeViewModel(@NonNull Application application) {
        super(application);
        repository = new EarthquakeRepository(application);
    }

    public LiveData<EarthquakeUiState> getUiState() {
        return uiState;
    }

    /*
        Called every time the screen becomes visible (onStart).
        Loads data only the first time, after the settings have changed,
        or if the previous attempt failed.
     */
    public void loadIfNeeded() {
        String url = repository.getRequestUrl();
        EarthquakeUiState current = uiState.getValue();

        boolean settingsChanged = !url.equals(lastUrl);
        boolean lastAttemptFailed = current == null
                || current.getStatus() == EarthquakeUiState.Status.NO_INTERNET
                || current.getStatus() == EarthquakeUiState.Status.ERROR;

        if (settingsChanged || lastAttemptFailed) {
            load(url);
        }
    }

    // Always downloads fresh data (used later for pull-to-refresh and the "Retry" button)
    public void refresh() {
        load(repository.getRequestUrl());
    }

    private void load(String url) {

        EarthquakeUiState current = uiState.getValue();
        List<Earthquake> previous = (url.equals(lastUrl) && current != null) ? current.getEarthquakes() : Collections.<Earthquake> emptyList();

        lastUrl = url;

        if (!repository.isOnline()) {
            uiState.setValue(EarthquakeUiState.noInternet());
            return;
        }

        uiState.setValue(EarthquakeUiState.loading(previous));

        executor.execute(() -> {
            EarthquakeUiState newState;
            try {
                List<Earthquake> earthquakes = repository.fetchEarthquakes(url);
                newState = earthquakes.isEmpty()
                        ? EarthquakeUiState.empty()
                        : EarthquakeUiState.success(earthquakes);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Loading earthquakes failed", e);
                newState = EarthquakeUiState.error();
            }

            // Ignore the result if a newer request was started in the meantime
            if (url.equals(lastUrl)) {
                uiState.postValue(newState);
            }
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}