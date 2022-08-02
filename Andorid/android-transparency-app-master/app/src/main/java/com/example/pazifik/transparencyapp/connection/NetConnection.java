package com.example.pazifik.transparencyapp.connection;

/**
 * This class represents a network connection
 */
public class NetConnection {
    private String local;
    private String remote;
    private String hostname;
    private String city;
    private String countryCode;
    private String company;
    private String state;
    private String protocol;

    public NetConnection(String local, String remote, String hostname, String city , String countryCode, String company, String state, String protocol) {
        this.local = local;
        this.remote = remote;
        this.hostname = hostname;
        this.city = city;
        this.countryCode = countryCode;
        this.company = company;
        this.state = state;
        this.protocol = protocol;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
