package com.opweather.api;

import com.opweather.api.WeatherResponse.CacheListener;
import com.opweather.api.WeatherResponse.NetworkListener;
import com.opweather.api.nodes.RootWeather;
import com.opweather.api.parser.ResponseParser;
import com.opweather.util.Validate;

import java.util.Locale;


public abstract class WeatherRequest {
    private CacheListener mCacheListener;
    private CacheMode mCacheMode;
    private final String mKey;
    private Locale mLocale;
    private NetworkListener mNetworkListener;
    private final int mType;
    private boolean mUseHttpCache;

    public enum CacheMode {
        LOAD_DEFAULT,
        LOAD_CACHE_ELSE_NETWORK,
        LOAD_NO_CACHE,
        LOAD_CACHE_ONLY
    }

    public interface Type {
        int ALARM = 32;
        int ALL = 15;
        int AQI = 8;
        int CURRENT = 1;
        int DAILY_FORECASTS = 4;
        int HOUR_FORECASTS = 2;
        int LIFE_INDEX = 16;
        int SUCCESS = 64;
    }

    public abstract String getDiskCacheKey(int i);

    public abstract String getMemCacheKey();

    public abstract String getRequestUrl(int i);

    public abstract ResponseParser getResponseParser();

    public static boolean contain(int request, int type) {
        return (request & type) == type;
    }

    private static boolean validRequestType(int type) {
        return contain(type, Type.CURRENT) || contain(type, Type.HOUR_FORECASTS) || contain(type, Type
                .DAILY_FORECASTS) || contain(type, Type.AQI) || contain(type, Type.LIFE_INDEX) || contain(type,
                Type.ALARM);
    }

    public WeatherRequest(String key) {
        this(Type.ALL, key, null, null);
    }

    public WeatherRequest(int type, String key) {
        this(type, key, null, null);
    }

    public WeatherRequest(String key, NetworkListener networkListener, CacheListener cacheListener) {
        this(Type.ALL, key, networkListener, cacheListener);
    }

    public WeatherRequest(int type, String key, NetworkListener networkListener, CacheListener cacheListener) {
        mCacheMode = CacheMode.LOAD_DEFAULT;
        mUseHttpCache = true;
        Validate.notEmpty(key, "Key should not be empty!");
        if (validRequestType(type)) {
            mKey = key;
            mType = type;
            mNetworkListener = networkListener;
            mCacheListener = cacheListener;
            return;
        }
        throw new IllegalArgumentException("Type should contain at least one of type AQI, " +
                "LIFE_INDEX, CURRENT, HOUR_FORECASTS, ALARM or DAILY_FORECASTS.");
    }

    public WeatherRequest setLocale(Locale locale) {
        mLocale = locale;
        return this;
    }

    public Locale getLocale() {
        return mLocale;
    }

    public Locale getLocale(Locale defaultLocale) {
        return mLocale == null ? defaultLocale : mLocale;
    }

    public String getRequestKey() {
        return mKey;
    }

    public int getRequestType() {
        return mType;
    }

    public void deliverNetworkResponse(RootWeather weather) {
        if (mNetworkListener != null) {
            mNetworkListener.onResponseSuccess(weather);
        }
    }

    public void deliverNetworkError(WeatherException e) {
        if (mNetworkListener != null) {
            mNetworkListener.onResponseError(e);
        }
    }

    public void deliverCacheResponse(RootWeather weather) {
        if (mCacheListener != null) {
            mCacheListener.onResponse(weather);
        }
    }

    public void setNetworkListener(NetworkListener listener) {
        mNetworkListener = listener;
    }

    public void setCacheListener(CacheListener listener) {
        mCacheListener = listener;
    }

    public NetworkListener getNetworkListener() {
        return mNetworkListener;
    }

    public CacheListener getCacheListener() {
        return mCacheListener;
    }

    public final WeatherRequest setCacheMode(CacheMode mode) {
        if (mode == null) {
            throw new NullPointerException("mode should not be null.");
        }
        mCacheMode = mode;
        return this;
    }

    public final CacheMode getCacheMode() {
        return mCacheMode;
    }

    public final boolean containRequest(int type) {
        return contain(mType, type);
    }

    public WeatherRequest setHttpCacheEnable(boolean enable) {
        mUseHttpCache = enable;
        return this;
    }

    public boolean getHttpCacheEnable() {
        return mUseHttpCache;
    }
}
