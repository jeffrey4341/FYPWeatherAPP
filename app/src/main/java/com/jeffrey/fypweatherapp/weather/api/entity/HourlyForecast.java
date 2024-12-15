package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HourlyForecast {
    @SerializedName("dt")
    public long dt;
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
    @SerializedName("weather")
    public List<WeatherCondition> weather;
    @SerializedName("pop")
    public double pop;
}
