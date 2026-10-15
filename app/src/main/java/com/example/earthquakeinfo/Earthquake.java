package com.example.earthquakeinfo;

// A class that contains information related to a single earthquake (POJO)

public class Earthquake {


    // magnitude of the earthquake
    private double magnitude;

    // location of the earthquake
    private String location;

    // time of the earthquake
    private long time;

    // url of the earthquake (to find more details of the earthquake)
    private String url;


    // constructor
    public Earthquake(double magnitude, String location, long time, String url) {
        this.magnitude = magnitude;
        this.location = location;
        this.time = time;
        this.url = url;
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

}
