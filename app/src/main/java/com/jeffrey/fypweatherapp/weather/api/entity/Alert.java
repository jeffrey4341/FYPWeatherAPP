package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;

public class Alert {
    @SerializedName("sender_name")
    public String senderName;
    @SerializedName("event")
    public String event;
    @SerializedName("start")
    public long start;
    @SerializedName("end")
    public long end;
    @SerializedName("description")
    public String description;
}
