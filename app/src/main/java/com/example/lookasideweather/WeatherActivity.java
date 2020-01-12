package com.example.lookasideweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lookasideweather.gson.Air;
import com.example.lookasideweather.gson.Forecast;
import com.example.lookasideweather.gson.Suggestion;
import com.example.lookasideweather.gson.WeatherForecast;
import com.example.lookasideweather.gson.WeatherLifestyle;
import com.example.lookasideweather.gson.WeatherNow;
import com.example.lookasideweather.service.AutoUpdateService;
import com.example.lookasideweather.util.HttpUtil;
import com.example.lookasideweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private Button navButton;
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout = (DrawerLayout) findViewById(R.id.draw_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherNowString = prefs.getString("WeatherNow", null);
        String weatherForecastString = prefs.getString("WeatherForecast", null);
        String weatherLifestyleString = prefs.getString("WeatherLifestyle", null);
        String airString = prefs.getString("Air", null);
        String bingPic = prefs.getString("bing_pic", null);
        if (weatherNowString != null) {
            WeatherNow weatherNow = Utility.handleWeatherNowResponse(weatherNowString);
            mWeatherId = weatherNow.basic.weatherId;
            showWeatherNowInfo(weatherNow);
        } else {
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeatherNow(mWeatherId);
        }

        if (weatherForecastString != null) {
            WeatherForecast weatherForecast = Utility.handleWeatherForecastResponse(weatherForecastString);
            mWeatherId = weatherForecast.basic.weatherId;
            showWeatherForecastInfo(weatherForecast);
        } else {
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeatherForecast(mWeatherId);
        }

        if (weatherLifestyleString != null) {
            WeatherLifestyle weatherLifestyle = Utility.handleWeatherLifestyleResponse(weatherLifestyleString);
            mWeatherId = weatherLifestyle.basic.weatherId;
            showWeatherLifestyleInfo(weatherLifestyle);
        } else {
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeatherLifestyle(mWeatherId);
        }

        if (airString != null) {
            Air air = Utility.handleAirResponse(airString);
            mWeatherId = air.basic.weatherId;
            showAirInfo(air);
        } else {
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestAir(mWeatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeatherNow(mWeatherId);
                requestWeatherForecast(mWeatherId);
                requestWeatherLifestyle(mWeatherId);
                requestAir(mWeatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    public void requestWeatherNow(final String weatherId) {
        String weatherNowUrl = "https://free-api.heweather.net/s6/weather/now?location=" +
                weatherId + "&key=e6fafde008d3422990d311c028a2d6f9";
        HttpUtil.sendOkHttpRequest(weatherNowUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherNow weatherNow = Utility.handleWeatherNowResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherNow != null && "ok".equals(weatherNow.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("WeatherNow", responseText);
                            editor.apply();
                            mWeatherId = weatherNow.basic.weatherId;
                            showWeatherNowInfo(weatherNow);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取实况天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取实况天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    public void requestWeatherForecast(final String weatherId) {
        String weatherForecastUrl = "https://free-api.heweather.net/s6/weather/forecast?location=" +
                weatherId + "&key=e6fafde008d3422990d311c028a2d6f9";
        HttpUtil.sendOkHttpRequest(weatherForecastUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherForecast weatherForecast = Utility.handleWeatherForecastResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherForecast != null && "ok".equals(weatherForecast.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("WeatherForecast", responseText);
                            editor.apply();
                            mWeatherId = weatherForecast.basic.weatherId;
                            showWeatherForecastInfo(weatherForecast);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取未来几天天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取未来几天天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void requestWeatherLifestyle(final String weatherId) {
        String weatherLifestyleUrl = "https://free-api.heweather.net/s6/weather/lifestyle?location=" +
                weatherId + "&key=e6fafde008d3422990d311c028a2d6f9";
        HttpUtil.sendOkHttpRequest(weatherLifestyleUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherLifestyle weatherLifestyle = Utility.handleWeatherLifestyleResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherLifestyle != null && "ok".equals(weatherLifestyle.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("WeatherLifestyle", responseText);
                            editor.apply();
                            mWeatherId = weatherLifestyle.basic.weatherId;
                            showWeatherLifestyleInfo(weatherLifestyle);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取生活指数信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取生活指数信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void requestAir(final String weatherId) {
        String airUrl = "https://free-api.heweather.net/s6/air/now?location=" +
                weatherId + "&key=e6fafde008d3422990d311c028a2d6f9";
        HttpUtil.sendOkHttpRequest(airUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Air air = Utility.handleAirResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (air != null && "ok".equals(air.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("Air", responseText);
                            editor.apply();
                            mWeatherId = air.basic.weatherId;
                            showAirInfo(air);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取空气质量信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取空气质量信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherNowInfo(WeatherNow weatherNow) {
        String cityName = weatherNow.basic.cityName;
        String updateTime = weatherNow.update.updateTime.split(" ")[1];
        String degree = weatherNow.now.temperature + "℃";
        String weatherInfo = weatherNow.now.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void showWeatherForecastInfo(WeatherForecast weatherForecast) {
        forecastLayout.removeAllViews();
        for (Forecast forecast : weatherForecast.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.info);
            maxText.setText(forecast.max);
            minText.setText(forecast.min);
            forecastLayout.addView(view);
        }
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void showWeatherLifestyleInfo(WeatherLifestyle weatherLifestyle) {
        for (Suggestion suggestion : weatherLifestyle.suggestionList) {
            switch (suggestion.type) {
                case "comf":
                    String comfort = "舒适度：" + suggestion.info;
                    comfortText.setText(comfort);
                    break;
                case "cw":
                    String carWash = "洗车指数：" + suggestion.info;
                    carWashText.setText(carWash);
                    break;
                case "sport":
                    String sport = "运动建议：" + suggestion.info;
                    sportText.setText(sport);
                    break;
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void showAirInfo(Air air) {
        if (air.aqi != null) {
            aqiText.setText(air.aqi.aqi);
            pm25Text.setText(air.aqi.pm25);
        }
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

}
