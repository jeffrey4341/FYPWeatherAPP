package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CurrentWeather implements Serializable {
    private static final long serialVersionUID = 6529685098267757692L;

    @SerializedName("dt")
    public long dt;
    @SerializedName("sunrise")
    public long sunrise;
    @SerializedName("sunset")
    public long sunset;
    @SerializedName("temp")
    public double temp;
    @SerializedName("feels_like")
    public double feelsLike;
    @SerializedName("pressure")
    public int pressure;
    @SerializedName("humidity")
    public int humidity;
    @SerializedName("dew_point")
    public double dewPoint;
    @SerializedName("uvi")
    public double uvi;
    @SerializedName("clouds")
    public int clouds;

    @SerializedName("visibility")
    public int visibility;
    @SerializedName("wind_speed")
    public double windSpeed;
    @SerializedName("wind_deg")
    public int windDeg;
    @SerializedName("wind_gust")
    public double windGust;
    @SerializedName("rain")
    public Rain rain;

    @SerializedName("weather")
    public List<WeatherCondition> weather;

    // Nested class WeatherCondition
    public static class WeatherCondition implements Serializable {
        private static final long serialVersionUID = 6529685098267757703L;

        @SerializedName("id")
        public int id;

        @SerializedName("main")
        public String main;

        @SerializedName("description")
        public String description;

        @SerializedName("icon")
        public String icon;
    }
}
