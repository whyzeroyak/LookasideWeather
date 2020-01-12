package com.example.lookasideweather.gson;

import com.google.gson.annotations.SerializedName;

public class Air {
    public String status;
    public Basic basic;
    @SerializedName("air_now_city")
    public AQI aqi;
}
