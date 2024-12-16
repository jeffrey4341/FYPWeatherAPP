package com.jeffrey.fypweatherapp.weather.api.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AirQualityResponse implements Serializable {

    private static final long serialVersionUID = 1206164576046726422L;

    @SerializedName("list")
    public List<AirQuality> list;


    public static class AirQuality implements Serializable {

        private static final long serialVersionUID = 6529685098267757690L;
        @SerializedName("main")
        public Main main;

        @SerializedName("components")
        public Components components;

        @SerializedName("dt")
        public long dt;

        public static class Main implements Serializable {

            private static final long serialVersionUID = 6529685098267757700L;
            @SerializedName("aqi")
            public int aqi;
        }

        public static class Components implements Serializable {

            private static final long serialVersionUID = 6529685098267757710L;

            @SerializedName("co")
            public double co;
            @SerializedName("no")
            public double no;
            @SerializedName("no2")
            public double no2;
            @SerializedName("o3")
            public double o3;
            @SerializedName("so2")
            public double so2;
            @SerializedName("pm2_5")
            public double pm2_5;
            @SerializedName("pm10")
            public double pm10;
            @SerializedName("nh3")
            public double nh3;
        }
    }

    @Override
    public String toString() {
        return "AirQualityResponse{" +
                "list=" + (list != null ? list.size() + " items" : "null") +
                '}';
    }

}


