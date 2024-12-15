package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DailyForecast implements Serializable {

    private static final long serialVersionUID = 1393763907620392674L;
    @SerializedName("dt")
    public long dt;
    @SerializedName("sunrise")
    public String sunrise;
    @SerializedName("sunset")
    public String sunset;
    @SerializedName("moonrise")
    public long moonrise;
    @SerializedName("moonset")
    public long moonset;
    @SerializedName("moon_phase")
    public double moonPhase;
    @SerializedName("summary")
    public String summary;
    @SerializedName("temp")
    public Temperature temp;
    @SerializedName("feels_like")
    public FeelsLike feelsLike;
    @SerializedName("pressure")
    public int pressure;
    @SerializedName("humidity")
    public int humidity;
    @SerializedName("dew_point")
    public double dewPoint;
    @SerializedName("wind_speed")
    public double windSpeed;
    @SerializedName("wind_deg")
    public int windDeg;
    @SerializedName("weather")
    public List<WeatherCondition> weather;
    @SerializedName("clouds")
    public int clouds;
    @SerializedName("pop")
    public double pop;
    @SerializedName("rain")
    public double rain;
    @SerializedName("uvi")
    public double uvi;
}
