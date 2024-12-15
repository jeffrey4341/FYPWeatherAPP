package com.jeffrey.fypweatherapp.weather.api.entity;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.jeffrey.fypweatherapp.weather.api.ApiManager;

public class Weather implements Serializable {

    private static final long serialVersionUID = -821374811106598097L;

    @SerializedName("OpenWeatherJSON")
    @Expose
    public OpenWeatherJSON OpenWeatherJSON;

    @SerializedName("AirQualityResponse")
    @Expose
    public AirQualityResponse AirQualityResponse;

    public OpenWeatherJSON getOpenWeatherJSON() {
        if (OpenWeatherJSON == null) {
            Log.e("Weather", "OpenWeatherJSON is null.");
            throw new IllegalStateException("OpenWeatherJSON data is invalid or empty.");
        }

        if (OpenWeatherJSON.current == null) {
            Log.e("Weather", "Current weather data is missing.");
            throw new IllegalStateException("Current weather data is missing in OpenWeatherJSON.");
        }

        return OpenWeatherJSON;
    }

    public AirQualityResponse getAirQualityResponse() {
        if (AirQualityResponse == null) {
            Log.e("Weather", "AirQualityResponse is null.");
            throw new IllegalStateException("AirQualityResponse data is invalid or empty.");
        }

        if (AirQualityResponse.list == null || AirQualityResponse.list.isEmpty()) {
            Log.e("Weather", "AirQuality list is missing or empty.");
            throw new IllegalStateException("AirQuality list is missing or empty in AirQualityResponse.");
        }

        return AirQualityResponse;
    }


    @Override
    public String toString() {
        return "Weather{" +
                "OpenWeatherJSON=" + OpenWeatherJSON +
                '}';
    }

    // Helper method to find today's forecast index
    public int getTodayDailyForecastIndex() {
        OpenWeatherJSON w = getOpenWeatherJSON();
        for (int i = 0; i < w.daily.size(); i++) {
            if (ApiManager.isToday(String.valueOf(w.daily.get(i).dt))) {
                return i;
            }
        }
        return -1;
    }

    // Retrieve today's daily forecast
    public DailyForecast getTodayDailyForecast() {
        int todayIndex = getTodayDailyForecastIndex();
        if (todayIndex != -1) {
            return getOpenWeatherJSON().daily.get(todayIndex);
        }
        return null;
    }
}

