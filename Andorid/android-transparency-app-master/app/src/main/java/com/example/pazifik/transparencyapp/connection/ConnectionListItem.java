package com.example.pazifik.transparencyapp.connection;

import android.graphics.drawable.Drawable;

/**
 * This class only exists as representation for a App-Connection mapping in the Recyclerview List
 */
public class ConnectionListItem {
    int uid;
    String name;
    Drawable icon;
    String local;
    String remote;
    String hostname;
    String city;
    String country;
    String countryCode;
    String company;
    String state;
    String protocol;

    public ConnectionListItem(int uid, String name, Drawable icon, String local, String remote, String hostname, String city, String country, String countryCode, String company, String state, String protocol) {
        this.uid = uid;
        this.name = name;
        this.icon = icon;
        this.local = local;
        this.remote = remote;
        this.hostname = hostname;
        this.city = city;
        this.country = country;
        this.countryCode = countryCode;
        this.company = company;
        this.state = state;
        this.protocol = protocol;
    }
}
