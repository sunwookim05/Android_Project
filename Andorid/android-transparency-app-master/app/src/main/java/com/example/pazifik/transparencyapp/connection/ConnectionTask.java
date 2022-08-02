package com.example.pazifik.transparencyapp.connection;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.pazifik.transparencyapp.LoggingState;
import com.example.pazifik.transparencyapp.R;
import com.example.pazifik.transparencyapp.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConnectionTask extends AsyncTask<Void, ConnectionListItem, Void> {
    private static final String TAG = LoggingState.tagConnection;
    private static final boolean debug = LoggingState.debugConnection;

    private WeakReference<Context> contextReference;
    private List<ApplicationInfo> installedAppsList;
    private Map<Integer, AppConnectionInfo> apps = new HashMap<>();
    private PackageManager pm;
    private ArrayList<ConnectionListItem> appConnections = new ArrayList<>();
    private ConnectionItemRecyclerViewAdapter adapter;

    public ConnectionTask(Context context) {
        contextReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // get a reference to the activity if it is still there
        Context context = contextReference.get();
        if (context != null) {
            Activity activity = (Activity) context;

            pm = context.getPackageManager();

            RecyclerView recyclerView = activity.findViewById(R.id.recycler_view_connection);
            adapter = new ConnectionItemRecyclerViewAdapter(context, appConnections);
            recyclerView.setAdapter(adapter);
        } else {
            if (debug) Log.d(TAG, "onPreExecute: context is null");
        }

    }

    @Override
    protected Void doInBackground(Void... voids) {
        // get a reference to the activity if it is still there
        Context context = contextReference.get();
        if (context != null) {
            installedAppsList = Util.getInstalledApplications(context);
        } else {
            if (debug) Log.d(TAG, "doInBackground: context is null");
        }
        buildNetstat();
        return null;
    }

    @Override
    protected void onProgressUpdate(ConnectionListItem... values) {
        appConnections.add(values[0]);
        adapter.notifyDataSetChanged();
    }

    /**
     * Parses all information from the /proc/net/ location for the transport protocols TCP, TCP6, UDP, UDP6. <br>
     * For every connection, additional information will be retrieved such as City, Country and Company for the IP address
     */
    private void buildNetstat() {
        apps.clear();
        appConnections.clear();
        try {
            parseProcNet("TCP");
            parseProcNet("TCP6");
            parseProcNet("UDP");
            parseProcNet("UDP6");
            for (final AppConnectionInfo appConnectionInfo : apps.values()) {
                for (final NetConnection conn : appConnectionInfo.connectionList) {
                    String ip = conn.getRemote().split(":")[0];
                    if (!ip.equals("") && !ip.equals(":0")) {
                        String hostname = Util.getHostnameFromIP(ip);
                        conn.setHostname(hostname);
                        getWhoisFromIP(ip, new VolleyCallback() {
                            @Override
                            public void onSuccess(String[] result) {
                                conn.setCity(result[0]);
                                conn.setCountryCode(result[1]);
                                conn.setCompany(result[2]);

                                Locale locale = new Locale("", result[1]);
                                String countryName = locale.getDisplayCountry(Locale.ENGLISH);
                                ConnectionListItem item = new ConnectionListItem(appConnectionInfo.uid, appConnectionInfo.name, appConnectionInfo.icon, conn.getLocal(), conn.getRemote(), conn.getHostname(), conn.getCity(), countryName, conn.getCountryCode(), conn.getCompany(), conn.getState(), conn.getProtocol());
                                if (debug) Log.d(TAG, "buildNetstat: IP: " + item.remote);
                                if (debug) Log.d(TAG, "buildNetstat: State: " + item.state);
                                if (debug) Log.d(TAG, "buildNetstat: City: " + item.city);
                                if (debug) Log.d(TAG, "buildNetstat: Country: " + item.country);
                                if (debug) Log.d(TAG, "buildNetstat: Company: " + item.company);

                                publishProgress(item);
                            }
                        });
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            if (debug) Log.e(TAG, "buildNetstat: " + e.toString());
        }
    }

    /**
     * Retrieves the Whois information for the given IP. <br>
     * Needs to implement a VolleyCallback which returns a String array consisting of 3 values: <br>
     * String[0] = City location of the server IP <br>
     * String[1] = 2-letter country code <br>
     * String[2] = company which owns the server IP
     *
     * @param ip       IP address
     * @param callback
     */
    private void getWhoisFromIP(final String ip, final VolleyCallback callback) {
        Context context = contextReference.get();
        if (context != null) {
            WhoisRequester queue = WhoisRequester.getInstance(context.getApplicationContext());

            String url = "https://extreme-ip-lookup.com/json/" + ip;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    String company = "";
                    String country = "";
                    String city = "";

                    try {
                        if (!response.getString("status").equals("fail")) {
                            city = response.getString("city");
                            country = response.getString("countryCode");
                            company = response.getString("org");
                        }
                        String[] temp = new String[]{city, country, company};
                        callback.onSuccess(temp);

                    } catch (JSONException e) {
                        if (debug) Log.e(TAG, "onResponse: " + e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        if (debug) Log.e(TAG, "onErrorResponse: " + error.toString());
                    } else if (error instanceof AuthFailureError) {
                        if (debug) Log.e(TAG, "onErrorResponse: " + error.toString());
                    } else if (error instanceof ServerError) {
                        if (debug) Log.e(TAG, "onErrorResponse: " + error.toString());
                    } else if (error instanceof NetworkError) {
                        if (debug) Log.e(TAG, "onErrorResponse: " + error.toString());
                    } else if (error instanceof ParseError) {
                        if (debug) Log.e(TAG, "onErrorResponse: " + error.toString());
                    }
                    String[] temp = new String[]{"", "", ""};
                    callback.onSuccess(temp);
                }
            });

            request.setShouldCache(true);
            queue.addToRequestQueue(request);
        } else {
            if (debug) Log.d(TAG, "getWhoisFromIP: context is null");
        }
    }

    /**
     * Parses all information from the /proc/net/PROTOCOL files. <br>
     * Fills the internal 'apps' Hashmap with the information which apps made which connections
     *
     * @param protocol valid protocols are: TCP, TCP6, UDP, UDP6
     * @throws IOException
     * @throws InterruptedException
     */
    private void parseProcNet(String protocol) throws IOException, InterruptedException {
        String cmdResult;
        switch (protocol) {
            case "TCP":
                cmdResult = Util.runCommand("cat /proc/net/tcp");
                break;
            case "TCP6":
                cmdResult = Util.runCommand("cat /proc/net/tcp6");
                break;
            case "UDP":
                cmdResult = Util.runCommand("cat /proc/net/udp");
                break;
            case "UDP6":
                cmdResult = Util.runCommand("cat /proc/net/udp6");
                break;
            default:
                if (debug) Log.d(TAG, "parseProcNet: abort, no valid protocol provided!");
                return;
        }

        String[] lines = cmdResult.trim().split("\\r?\\n");
        for (int i = 1; i < lines.length; i++) {
            Log.d("Connection", lines[i]);
            String[] data = lines[i].trim().split("\\s+");
            int uid = Integer.parseInt(data[7]);
            if (uid != 0) {
                if (!apps.containsKey(uid)) {
                    String name = pm.getNameForUid(uid);
                    Drawable icon = pm.getDefaultActivityIcon();
                    if (uid == 1000) {
                        name = "Android System";
                        icon = pm.getDefaultActivityIcon();
                    } else {
                        for (ApplicationInfo applicationInfo : installedAppsList) {
                            if (uid == applicationInfo.uid) {
                                name = (String) pm.getApplicationLabel(applicationInfo);
                                icon = pm.getApplicationIcon(applicationInfo);
                                break;
                            }
                        }
                    }
                    apps.put(uid, new AppConnectionInfo(uid, name, icon));
                }

                String local = "";
                String remote = "";
                if (protocol.equals("TCP") || protocol.equals("UDP")) {
                    local = Util.decodeIp4(data[1]);
                    remote = Util.decodeIp4(data[2]);
                } else if (protocol.equals("TCP6") || protocol.equals("UDP6")) {
                    local = Util.decodeIp6(data[1].substring(0, 32)) + ":" + Integer.parseInt(data[1].substring(33), 16);
                    remote = Util.decodeIp6(data[2].substring(0, 32)) + ":" + Integer.parseInt(data[2].substring(33), 16);
                }
                String state = getState(data[3]);
                if (!state.equals(getState("0A")) && !remote.contains("0.0.0.0")) {
                    NetConnection connection = new NetConnection(local, remote, "", "", "", "", state, protocol);
                    apps.get(uid).addConnection(connection);
                }
            }
        }
    }

    private String getState(String entry) {
        String result;
        switch(entry){
            case "01": result = "ESTABLISHED"; break;
            case "02": result = "SYN_SENT"; break;
            case "03": result = "SYN_RECV"; break;
            case "04": result = "FIN_WAIT1"; break;
            case "05": result = "FIN_WAIT2"; break;
            case "06": result = "TIME_WAIT"; break;
            case "07": result = "CLOSE"; break;
            case "08": result = "CLOSE_WAIT"; break;
            case "09": result = "LAST_ACK"; break;
            case "0A": result = "LISTEN"; break;
            case "0B": result = "CLOSING"; break;
            default: result = entry; break;
        }
        return result;
    }

    public interface VolleyCallback {
        void onSuccess(String[] result);
    }
}
