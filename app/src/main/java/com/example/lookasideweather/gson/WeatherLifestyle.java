package com.example.lookasideweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherLifestyle {
    public String status;
    public Basic basic;
    @SerializedName("lifestyle")
    public List<Suggestion> suggestionList;
}
