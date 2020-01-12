package com.example.lookasideweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("type")
    public String type;

    @SerializedName("brf")
    public String brief;

    @SerializedName("txt")
    public String info;
}
