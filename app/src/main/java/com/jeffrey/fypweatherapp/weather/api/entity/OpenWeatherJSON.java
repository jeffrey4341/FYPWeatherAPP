package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class OpenWeatherJSON implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1206164576046726422L;
    @SerializedName("lat")
    @Expose
    public double lat;
    @SerializedName("lon")
    @Expose
    public double lon;
    @SerializedName("timezone")
    @Expose
    public String timezone;
    @SerializedName("timezone_offset")
    @Expose
    public int timezoneOffset;
    @SerializedName("current")
    @Expose
    public CurrentWeather current;
    @SerializedName("minutely")
    @Expose
    public List<Minutely> minutely;
    @SerializedName("hourly")
    @Expose
    public List<HourlyForecast> hourly;
    @SerializedName("daily")
    @Expose
    public List<DailyForecast> daily;
    @SerializedName("alerts")
    @Expose
    public List<Alert> alerts;

    // Helper method to validate if the data is loaded
    public boolean isValid() {
        return current != null && hourly != null && !hourly.isEmpty() && daily != null && !daily.isEmpty();
    }

    @Override
    public String toString() {
        return "OpenWeatherJSON{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", timezone='" + timezone + '\'' +
                ", timezoneOffset=" + timezoneOffset +
                ", current=" + (current != null ? current.toString() : "null") +
                ", hourly=" + (hourly != null ? hourly.size() + " items" : "null") +
                ", daily=" + (daily != null ? daily.size() + " items" : "null") +
                ", alerts=" + (alerts != null ? alerts.size() + " items" : "null") +
                '}';
    }

}
