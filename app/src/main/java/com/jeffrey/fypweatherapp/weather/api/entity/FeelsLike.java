package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FeelsLike implements Serializable {
    private static final long serialVersionUID = 6529685098267757704L;

    @SerializedName("day")
    public double day;

    @SerializedName("night")
    public double night;

    @SerializedName("eve")
    public double eve;

    @SerializedName("morn")
    public double morn;
}
