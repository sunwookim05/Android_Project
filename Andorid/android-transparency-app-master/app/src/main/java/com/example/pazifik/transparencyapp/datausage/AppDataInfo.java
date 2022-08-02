package com.example.pazifik.transparencyapp.datausage;

import android.graphics.drawable.Drawable;

public class AppDataInfo {
    int uid;
    String name;
    String packageName;
    Drawable icon;
    double progress;
    long transmitted;


    public AppDataInfo(int uid, String name, String packageName, Drawable icon, double progress, long transmitted) {
        this.uid = uid;
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.progress = progress;
        this.transmitted = transmitted;
    }

}
