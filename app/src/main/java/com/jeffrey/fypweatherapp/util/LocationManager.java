package com.jeffrey.fypweatherapp.util;

public class LocationManager {

    private static LocationManager instance;
    private double latitude;
    private double longitude;

    private LocationManager() { }

    public static synchronized LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

