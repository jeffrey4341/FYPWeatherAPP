package com.jeffrey.fypweatherapp.weather.api.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Temperature implements Parcelable {
    @SerializedName("day")
    public double day;
    @SerializedName("min")
    public double min;
    @SerializedName("max")
    public double max;
    @SerializedName("night")
    public double night;
    @SerializedName("eve")
    public double eve;
    @SerializedName("morn")
    public double morn;

    public Temperature() {}

    protected Temperature(Parcel in) {
        day = in.readDouble();
        min = in.readDouble();
        max = in.readDouble();
        night = in.readDouble();
        eve = in.readDouble();
        morn = in.readDouble();
    }

    public static final Creator<Temperature> CREATOR = new Creator<Temperature>() {
        @Override
        public Temperature createFromParcel(Parcel in) {
            return new Temperature(in);
        }

        @Override
        public Temperature[] newArray(int size) {
            return new Temperature[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(day);
        dest.writeDouble(min);
        dest.writeDouble(max);
        dest.writeDouble(night);
        dest.writeDouble(eve);
        dest.writeDouble(morn);
    }
}
