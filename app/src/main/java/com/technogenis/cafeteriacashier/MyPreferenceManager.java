package com.technogenis.cafeteriacashier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;


public class MyPreferenceManager {
    Context context;
    private SharedPreferences preference;
    @SuppressLint("StaticFieldLeak")




    private MyPreferenceManager(Context context) {
        // Constructor implementation
        preference = context.getSharedPreferences(Constant.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    @SuppressLint("StaticFieldLeak")
    private static volatile MyPreferenceManager instance;

    public static MyPreferenceManager getInstance(Context context) {
        if (instance == null) {
            synchronized (MyPreferenceManager.class) {
                if (instance == null) {
                    instance = new MyPreferenceManager(context);
                }
            }
        }
        return instance;
    }



    public void putString(String key, String value) {
        preference.edit().putString(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        preference.edit().putBoolean(key, value).apply();
    }

    public void putInt(String key, int value) {
        preference.edit().putInt(key, value).apply();
    }

    public void putLong(String key, long value) {
        preference.edit().putLong(key, value).apply();
    }

    public void putFloat(String key, float value) {
        preference.edit().putFloat(key, value).apply();
    }

    public String getString(String key) {
        return preference.getString(key, "");
    }

    public boolean getBoolean(String key) {
        return preference.getBoolean(key, false);
    }

    public int getInt(String key) {
        return preference.getInt(key, 0);
    }

    public long getLong(String key) {
        return preference.getLong(key, 0);
    }

    public float getFloat(String key) {
        return preference.getFloat(key, 0.0f);
    }

    public void clear() {
        preference.edit().clear().apply();
    }


}
