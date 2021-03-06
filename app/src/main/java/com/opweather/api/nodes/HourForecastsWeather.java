package com.opweather.api.nodes;

import android.content.Context;

import java.util.Date;

public abstract class HourForecastsWeather extends AbstractWeather {
    public abstract Temperature getTemperature();

    public abstract Date getTime();

    public abstract int getWeatherId();

    public abstract String getWeatherText(Context context);

    public HourForecastsWeather(String areaCode, String areaName, String dataSource) {
        super(areaCode, areaName, dataSource);
    }

    public String getWeatherName() {
        return "Hour Forecasts Weather";
    }
}
