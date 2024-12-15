package com.jeffrey.fypweatherapp.weather;

import android.app.Application;

public class WeatherApplication extends Application {


    public static final boolean DEBUG = true;
    public static final boolean USE_SAMPLE_DATA = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
