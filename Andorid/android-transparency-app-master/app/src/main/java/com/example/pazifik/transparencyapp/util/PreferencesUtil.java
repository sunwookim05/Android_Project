package com.example.pazifik.transparencyapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {
    public final static String PREFS_FILE_NAME = "TransparencyApp";

    public static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime){
        SharedPreferences sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }
    public static boolean isFirstTimeAskingPermission(Context context, String permission){
        return context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).getBoolean(permission, true);
    }
}
