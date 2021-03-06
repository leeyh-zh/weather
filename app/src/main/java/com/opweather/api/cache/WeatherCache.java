package com.opweather.api.cache;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.opweather.api.helper.IOUtils;
import com.opweather.api.helper.LogUtils;
import com.opweather.api.helper.StringUtils;
import com.opweather.api.nodes.RootWeather;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class WeatherCache implements Cache {
    private static final int DEFAULT_DISK_CACHE_SIZE = 10 * 1024 * 1024;
    private static final int DEFAULT_MEM_CACHE_SIZE = 8;
    private static final int DISK_CACHE_INDEX = 0;
    private static final String TAG = "WeatherCache";
    private static final int VERSION_CODE = 1;
    private static final String WEATHER_CACHE_DIR = "weather";
    private static final Object mDiskCacheLock = new Object();
    private static WeatherCache sInstance;
    private Context mContext;
    private boolean mDiskCacheStarting;
    private DiskLruCache mDiskLruCache;
    private LruCache<String, RootWeather> mMemoryCache;

    private class CacheAsyncTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            try {
                initDiskCache();
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: " + e.getMessage());
                Log.d(TAG, "failed to write disk");
            }
            return null;
        }
    }

    public static WeatherCache getInstance(Context context) {
        if (sInstance == null) {
            synchronized (WeatherCache.class) {
                if (sInstance == null) {
                    sInstance = new WeatherCache(context);
                }
            }
        }
        return sInstance;
    }

    private WeatherCache(Context context) {
        mDiskCacheStarting = true;
        mContext = context.getApplicationContext();
        init();
    }

    private void init() {
        LogUtils.d(TAG, "Memory cache created (size = 8)");
        mMemoryCache = new LruCache<>(DEFAULT_MEM_CACHE_SIZE);
        initDiskCacheBackground();
    }

    private void initDiskCacheBackground() {
        new CacheAsyncTask().execute();
    }

    public void initDiskCache() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = getDiskCacheDir(mContext, WEATHER_CACHE_DIR);
                if (!diskCacheDir.exists()) {
                    diskCacheDir.mkdirs();
                }
                if (getUsableSpace(diskCacheDir) > DEFAULT_DISK_CACHE_SIZE) {
                    try {
                        LogUtils.d(TAG, "Disk cache initialized star");
                        mDiskLruCache = DiskLruCache.open(diskCacheDir, VERSION_CODE, VERSION_CODE,
                                PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED);
                    } catch (IOException e) {
                        LogUtils.e(TAG, "initDiskCache - " + e);
                    }
                }

            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    public static long getUsableSpace(File path) {
        return path.getUsableSpace();
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = context.getCacheDir().getPath();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable()) {
            File ext = getExternalCacheDir(context);
            if (ext != null) {
                cachePath = ext.getPath();
            }
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public static boolean isExternalStorageRemovable() {
        return Environment.isExternalStorageRemovable();
    }

    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
    }

    public static String hashKeyForDisk(String key) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            return bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(key.hashCode());
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = DISK_CACHE_INDEX; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 255);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public RootWeather getFromMemCache(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        RootWeather memValue = null;
        if (mMemoryCache != null) {
            memValue = mMemoryCache.get(key);
        }
        LogUtils.d(TAG, "Memory cache hit");
        return memValue;
    }

    public byte[] getFromDiskCache(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        String hashKey = hashKeyForDisk(key);
        byte[] bArr = null;
        synchronized (mDiskCacheLock) {
            InputStream inputStream;
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                } catch (Throwable th) {
                }
            }
            if (mDiskLruCache != null) {
                inputStream = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKey);
                    if (snapshot != null) {
                        LogUtils.d(TAG, "Disk cache hit");
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            bArr = IOUtils.toByteArray(inputStream);
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (IOException e3) {
                    LogUtils.e(TAG, "getBitmapFromDiskCache - " + e3);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e4) {
                        }
                    }
                }
            }
        }
        return bArr;
    }

    public void putToMemory(String key, RootWeather value) {
        if (!StringUtils.isBlank(key) && value != null && this.mMemoryCache != null) {
            this.mMemoryCache.put(key, value);
        }
    }

    public void putToDisk(String key, byte[] value) {
        if (!StringUtils.isBlank(key) && value != null) {
            LogUtils.d(TAG, "DiskKey: " + key);
            synchronized (mDiskCacheLock) {
                OutputStream out;
                try {
                    if (mDiskLruCache != null) {
                        out = null;
                        try {
                            DiskLruCache.Editor editor = mDiskLruCache.edit(hashKeyForDisk(key));
                            if (editor != null) {
                                out = editor.newOutputStream(DISK_CACHE_INDEX);
                                IOUtils.write(value, out);
                                editor.commit();
                                out.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e2) {
                            LogUtils.e(TAG, "putToDisk - " + e2);
                            if (out != null) {
                                out.close();
                            }
                        }
                    }
                } catch (Throwable th) {
                }
            }
        }
    }

    public void clear() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
            LogUtils.d(TAG, "Memory cache cleared");
        }
        synchronized (mDiskCacheLock) {
            this.mDiskCacheStarting = true;
            if (!(this.mDiskLruCache == null || this.mDiskLruCache.isClosed())) {
                try {
                    this.mDiskLruCache.delete();
                    LogUtils.d(TAG, "Disk cache cleared");
                } catch (IOException e) {
                    LogUtils.e(TAG, "clear - " + e);
                }
                this.mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    public void flush() {
        synchronized (mDiskCacheLock) {
            if (this.mDiskLruCache != null) {
                try {
                    this.mDiskLruCache.flush();
                    LogUtils.d(TAG, "Disk cache flushed");
                } catch (IOException e) {
                    LogUtils.e(TAG, "flush - " + e);
                }
            }
        }
    }

    public void close() {
        synchronized (mDiskCacheLock) {
            if (this.mDiskLruCache != null) {
                try {
                    if (!this.mDiskLruCache.isClosed()) {
                        this.mDiskLruCache.close();
                        this.mDiskLruCache = null;
                        LogUtils.d(TAG, "Disk cache closed");
                    }
                } catch (IOException e) {
                    LogUtils.e(TAG, "close - " + e);
                }
            }
        }
    }
}
