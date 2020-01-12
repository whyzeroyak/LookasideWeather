package com.example.lookasideweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherForecast {
    public String status;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
