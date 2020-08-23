package com.example.sunnyweather.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {
    public AQICity city;
    public class AQICity{
        @SerializedName("aqi")
        String aqi;

        @SerializedName("pm25")
        String pm25;
    }
}