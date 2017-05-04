package com.mrcoders.www.voiceassistant.functionality;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mrcoders.www.voiceassistant.activity.TalkActivity;

import java.text.DecimalFormat;

import github.vatsal.easyweather.Helper.WeatherCallback;
import github.vatsal.easyweather.WeatherMap;
import github.vatsal.easyweather.retrofit.models.Main;
import github.vatsal.easyweather.retrofit.models.Weather;
import github.vatsal.easyweather.retrofit.models.WeatherResponseModel;

/**
 * Created by rohit on 5/4/17.
 */

public class WeatherStatus extends WeatherCallback{

    private WeatherMap weatherMap;
    private TalkActivity context;

    public WeatherStatus(WeatherMap weatherMap, TalkActivity context) {
        this.weatherMap = weatherMap;
        this.context = context;
    }

    public void weatherStatus(String city) {

        Toast.makeText(context,"weather",Toast.LENGTH_LONG).show();
        weatherMap.getCityWeather(city,this);
    }

    @Override
    public void success(WeatherResponseModel weatherResponseModel) {
        Main main = weatherResponseModel.getMain();
        Float temp = Float.parseFloat(main.getTemp());
        temp = temp - 273;
        Double pressure = Double.parseDouble(main.getPressure());
        Double humidity = Double.parseDouble(main.getHumidity());

        String toSpeak = "Tempreture,"+temp+"degree celsius,"+"Pressure,"+pressure+"mm,"+"Humidity"+humidity;
        String toDisplay = "Tempreture : "+temp+(char) 0x00B0+"C"+"\nPressure :"+pressure+"mm"+"\nHumidity :"+humidity;

        //Toast.makeText(context,toDisplay,Toast.LENGTH_LONG).show();
        context.speakOut(toSpeak);
        context.updateResponse(toDisplay);
    }

    @Override
    public void failure(String s) {
        Toast.makeText(context,"Weather status failed",Toast.LENGTH_LONG).show();
        context.speakOut("Sorry cann't get the weather status");
    }
}
