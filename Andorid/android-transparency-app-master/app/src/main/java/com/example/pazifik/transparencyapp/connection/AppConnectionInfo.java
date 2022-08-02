package com.example.pazifik.transparencyapp.connection;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * This class represents a mapping of an app to all its current network connections
 */
public class AppConnectionInfo {
    int uid;
    String name;
    Drawable icon;
    ArrayList<NetConnection> connectionList;

    public AppConnectionInfo(int uid, String name, Drawable icon){
        this.uid = uid;
        this.name = name;
        this.icon = icon;
        this.connectionList = new ArrayList<>();
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void addConnection(NetConnection connection) {
        this.connectionList.add(connection);
    }
}
