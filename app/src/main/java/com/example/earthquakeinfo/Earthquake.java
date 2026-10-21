package com.example.earthquakeinfo;

// A class that contains information related to a single earthquake (POJO)

public class Earthquake {


    // magnitude of the earthquake
    private final double magnitude;

    // location of the earthquake
    private final String location;

    // time of the earthquake
    private final long time;

    // url of the earthquake (to find more details of the earthquake)
    private final String url;

    // position of the epicenter in degrees
    private final double latitude;
    private final double longitude;

    // depth below the surface
    private final double depth;

    // true if USGS issued a tsunami flag for this earthquake
    private final boolean tsunami;

    // constructor
    public Earthquake(double magnitude, String location, long time, String url,double latitude,
                      double longitude, double depth, boolean tsunami) {
        this.magnitude = magnitude;
        this.location = location;
        this.time = time;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
        this.tsunami = tsunami;
    }


    // getters (setters not needed)

    public double getMagnitude() {
        return magnitude;
    }

    public String getLocation() {
        return location;
    }

    public long getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDepth() {
        return depth;
    }

    public boolean isTsunami() {
        return tsunami;
    }

    public boolean hasCoordinates(){
        return !Double.isNaN(latitude) && !Double.isNaN(longitude);
    }

    public boolean hasDepth(){
        return !Double.isNaN(depth);
    }

}
