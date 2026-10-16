package com.example.earthquakeinfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Loads the list of earthqukes on a background thread and keeps it  while the activity is recreated
public class EarthquakeViewModel extends ViewModel {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Earthquake>> earthquakes = new MutableLiveData<>();

    // URL of the last request , so the same data is not downloaded twice
    private volatile String lastUrl;

    public LiveData<List<Earthquake>> getEarthquakes(){
        return earthquakes;
    }

    public boolean load(String url){
        if (url.equals(lastUrl)){
            return false;
        }
        lastUrl = url;
        executor.execute(() -> {
            List<Earthquake> result = QueryUtils.fetchEarthquakeData(url);
            // Ignore the result if a newer request was started in the meantime
            if (url.equals(lastUrl)){
                earthquakes.postValue(result);
            }
        });
        return true;
    }

    // Forget the last URL so the next load() downloads the data again
    public void reset(){
        lastUrl = null;
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
