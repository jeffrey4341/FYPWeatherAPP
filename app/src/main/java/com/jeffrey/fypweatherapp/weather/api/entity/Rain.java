package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Rain implements Serializable {
    private static final long serialVersionUID = 6529685098267757702L;

    @SerializedName("1h")
    public float hour;

}
