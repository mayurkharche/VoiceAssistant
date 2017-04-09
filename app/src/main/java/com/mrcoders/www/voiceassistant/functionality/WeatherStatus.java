package com.mrcoders.www.voiceassistant.functionality;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import github.vatsal.easyweather.Helper.WeatherCallback;
import github.vatsal.easyweather.WeatherMap;
import github.vatsal.easyweather.retrofit.models.Weather;
import github.vatsal.easyweather.retrofit.models.WeatherResponseModel;

/**
 * Created by rohit on 5/4/17.
 */

public class WeatherStatus extends AppCompatActivity{

    WeatherMap weatherMap;

    public void weatherStatus(String city) {
        weatherMap.getCityWeather(city, new WeatherCallback() {
            @Override
            public void success(WeatherResponseModel weatherResponseModel) {
                Weather weather[] = weatherResponseModel.getWeather();
                String weatherMain = weather[0].getMain();
                Toast.makeText(WeatherStatus.this,weatherMain,Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(String s) {

            }
        });
    }
}
