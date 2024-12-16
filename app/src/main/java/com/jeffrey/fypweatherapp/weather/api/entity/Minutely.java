package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Minutely implements Serializable {
    private static final long serialVersionUID = 6529685098267757707L;

    @SerializedName("dt")
    public long dt;
    @SerializedName("precipitation")
    public double precipitation;
}
