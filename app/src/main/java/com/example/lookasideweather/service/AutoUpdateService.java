package com.example.lookasideweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.lookasideweather.gson.Air;
import com.example.lookasideweather.gson.WeatherForecast;
import com.example.lookasideweather.gson.WeatherLifestyle;
import com.example.lookasideweather.gson.WeatherNow;
import com.example.lookasideweather.util.HttpUtil;
import com.example.lookasideweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000; // 这是8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherNowString = prefs.getString("WeatherNow", null);
        String weatherForecastString = prefs.getString("WeatherForecast", null);
        String weatherLifestyleString = prefs.getString("WeatherLifestyle", null);
        String airString = prefs.getString("Air", null);
        if (weatherNowString != null) {
            // 有缓存时直接解析天气数据
            WeatherNow weatherNow = Utility.handleWeatherNowResponse(weatherNowString);
            String weatherId = weatherNow.basic.weatherId;
            String weatherNowUrl = "https://free-api.heweather.net/s6/weather/now?location=" +
                    weatherId + "&key=e6fafde008d3422990d311c028a2d6f9";
            HttpUtil.sendOkHttpRequest(weatherNowUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    WeatherNow weatherNow = Utility.handleWeatherNowResponse(responseText);
                    if (weatherNow != null && "ok".equals(weatherNow.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("WeatherNow", responseText);
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
        if (weatherForecastString != null) {
            // 有缓存时直接解析天气数据
            WeatherForecast weatherForecast = Utility.handleWeatherForecastResponse(weatherForecastString);
            String weatherId = weatherForecast.basic.weatherId;
            String weatherForecastUrl = "https://free-api.heweather.net/s6/weather/forecast?location=" +
                    weatherId + "&key=e6fafde008d3422990d311c028a2d6f9";
            HttpUtil.sendOkHttpRequest(weatherForecastUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    WeatherForecast weatherForecast = Utility.handleWeatherForecastResponse(responseText);
                    if (weatherForecast != null && "ok".equals(weatherForecast.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("WeatherForecast", responseText);
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
        if (weatherLifestyleString != null) {
            // 有缓存时直接解析天气数据
            WeatherLifestyle weatherLifestyle = Utility.handleWeatherLifestyleResponse(weatherLifestyleString);
            String weatherId = weatherLifestyle.basic.weatherId;
            String weatherLifestyleUrl = "https://free-api.heweather.net/s6/weather/lifestyle?location=" +
                    weatherId + "&key=e6fafde008d3422990d311c028a2d6f9";
            HttpUtil.sendOkHttpRequest(weatherLifestyleUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    WeatherLifestyle weatherLifestyle = Utility.handleWeatherLifestyleResponse(responseText);
                    if (weatherLifestyle != null && "ok".equals(weatherLifestyle.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("WeatherLifestyle", responseText);
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
        if (airString != null) {
            // 有缓存时直接解析天气数据
            Air air = Utility.handleAirResponse(airString);
            String weatherId = air.basic.weatherId;
            String airUrl = "https://free-api.heweather.net/s6/air/now?location=" +
                    weatherId + "&key=e6fafde008d3422990d311c028a2d6f9";
            HttpUtil.sendOkHttpRequest(airUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Air air = Utility.handleAirResponse(responseText);
                    if (air != null && "ok".equals(air.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("Air", responseText);
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}
