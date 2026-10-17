package com.example.earthquakeinfo;

import java.util.Collections;
import java.util.List;

/*
    Describes everything the main screen needs to show at one moment
    ViewModel creates a new state, Activity only draws it
 */
public class EarthquakeUiState {

    public enum Status{
        LOADING,    // data is being downloaded - show the progress indicator
        SUCCESS,    // data arrived - show the list
        EMPTY,      // request succeeded, but there is no earthquakes for these settings
        NO_INTERNET,// the device is offline
        ERROR       // the server did not answer or sent invalid data
    }

    private final Status status;
    private final List<Earthquake> earthquakes;

    public EarthquakeUiState(Status status, List<Earthquake> earthquakes) {
        this.status = status;
        this.earthquakes = earthquakes;
    }

    public Status getStatus() {
        return status;
    }

    public List<Earthquake> getEarthquakes() {
        return earthquakes;
    }

    public static EarthquakeUiState loading(){
        return new EarthquakeUiState(Status.LOADING, Collections.emptyList());
    }

    public static EarthquakeUiState success(List<Earthquake> earthquakes){
        return new EarthquakeUiState(Status.SUCCESS,Collections.unmodifiableList(earthquakes));
    }

    public static EarthquakeUiState empty(){
        return new EarthquakeUiState(Status.EMPTY, Collections.emptyList());
    }

    public static EarthquakeUiState noInternet(){
        return new EarthquakeUiState(Status.NO_INTERNET,Collections.emptyList());
    }

    public static EarthquakeUiState error(){
        return new EarthquakeUiState(Status.ERROR, Collections.emptyList());
    }

}
