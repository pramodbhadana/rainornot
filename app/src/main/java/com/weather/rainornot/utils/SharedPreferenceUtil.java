package com.weather.rainornot.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by pramodbhadana on 16/04/17.
 */

public class SharedPreferenceUtil {

    private static SharedPreferenceUtil instance;
    private SharedPreferences SP;
    private SharedPreferences.Editor editor;
    private String KeyLastPlace="LastAccessedPlace";

    private SharedPreferenceUtil(Context context)
    {
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        editor = SP.edit();
    }

    public static SharedPreferenceUtil getInstance(Context context)
    {
        if(instance == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (instance == null)
                {
                    instance = new SharedPreferenceUtil(context);
                }
            }
        }
        return instance;
    }

    public int getLastPlace()
    {
         return SP.getInt(KeyLastPlace,0);
    }

    public void setLastPlace(int value)
    {
        editor.putInt(KeyLastPlace,value);
        editor.commit();
    }

}
