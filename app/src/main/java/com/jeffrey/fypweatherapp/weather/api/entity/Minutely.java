package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;

public class Minutely {
    @SerializedName("dt")
    public long dt;
    @SerializedName("precipitation")
    public double precipitation;
}
