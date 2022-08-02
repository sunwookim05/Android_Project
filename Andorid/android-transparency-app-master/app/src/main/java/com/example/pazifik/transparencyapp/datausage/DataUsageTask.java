package com.example.pazifik.transparencyapp.datausage;

import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.pazifik.transparencyapp.LoggingState;
import com.example.pazifik.transparencyapp.R;
import com.example.pazifik.transparencyapp.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataUsageTask extends AsyncTask<Void, AppDataInfo, Long> {
    private static final String TAG = LoggingState.tagDataUsage;
    private static final boolean debug = LoggingState.debugDataUsage;

    // Weak references will still allow the Activity/Context to be garbage-collected
    private WeakReference<Context> contextReference;
    private List<AppDataInfo> appDataInfoList;
    private SparseArray<long[]> uidsWithNetworkUsageMap;
    private DataItemRecyclerViewAdapter adapter;
    private int spinnerItemPos;
    private ProgressBar progressBar;
    private int progressCounter;

    public DataUsageTask(Context context, int spinnerItemPos) {
        contextReference = new WeakReference<>(context);
        this.spinnerItemPos = spinnerItemPos;
        appDataInfoList = new ArrayList<>();
        uidsWithNetworkUsageMap = new SparseArray<>();
        progressCounter = 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // get a reference to the activity if it is still there
        Context context = contextReference.get();
        if (context != null) {
            if (debug) Log.d(TAG, "onPreExecute: called");
            Activity activity = (Activity) context;

            TextView textView = activity.findViewById(R.id.label_data_amount);
            textView.setText("");

            progressBar = activity.findViewById(R.id.progressBarData);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);

            RecyclerView recyclerView = activity.findViewById(R.id.recycler_view_data);
            recyclerView.setHasFixedSize(true);
            adapter = new DataItemRecyclerViewAdapter(contextReference.get(), appDataInfoList);
            recyclerView.setAdapter(adapter);
        } else {
            if (debug) Log.d(TAG, "onPreExecute: context is null");
        }
    }

    @Override
    protected Long doInBackground(Void... params) {
        long txBytesCounter = 0;
        Context context = contextReference.get();
        if (context != null) {
            if (debug) Log.d(TAG, "doInBackground: called");
            List<ApplicationInfo> installedAppsList = Util.getInstalledApplications(context);

            // if current API Level < 23 (Marshmallow), use TrafficStats, else NetworkStatsManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                progressBar.setMax(installedAppsList.size());
                txBytesCounter = useTrafficStats(context, installedAppsList);
            } else {
                if (spinnerItemPos == 0)
                    txBytesCounter = useNetworkStats(context, installedAppsList, Util.getCalendarToday());
                else if (spinnerItemPos == 1)
                    txBytesCounter = useNetworkStats(context, installedAppsList, Util.getCalendarCurrentWeek());
                else if (spinnerItemPos == 2)
                    txBytesCounter = useNetworkStats(context, installedAppsList, Util.getCalendarCurrentMonth());
                else if (spinnerItemPos == 3)
                    txBytesCounter = useNetworkStats(context, installedAppsList, Util.getCalendarLastThreeMonths());
            }
        } else {
            if (debug) Log.d(TAG, "doInBackground: context is null");
        }
        return txBytesCounter;
    }

    /**
     * Uses the TrafficStats class. <br>
     * For every UID in the provided 'installedAppsList', all app-related information will be extracted
     *
     * @param context           context
     * @param installedAppsList list of apps to consider
     * @return the total bytes transmitted of all apps considered in the time frame
     */
    private long useTrafficStats(Context context, List<ApplicationInfo> installedAppsList) {
        final PackageManager pm = context.getPackageManager();
        long totalRxBytes = TrafficStats.getTotalRxBytes();
        long totalTxBytes = TrafficStats.getTotalTxBytes();
        if (debug) {
            Log.d(TAG, "Total Received: " + Util.getFileSize(totalRxBytes));
            Log.d(TAG, "Total Transmitted: " + Util.getFileSize(totalTxBytes));
        }
        List<Integer> processedUIDs = new ArrayList<>();
        for (ApplicationInfo app : installedAppsList) {
            // Escape early if cancel() is called
            if (isCancelled()) break;

            int uid = app.uid;
            // in order to prevent having duplicate entries, e.g. multiple UID 1000
            if (!processedUIDs.contains(uid)) {
                processedUIDs.add(uid);

                long rxBytesUID = TrafficStats.getUidRxBytes(uid);
                long txBytesUID = TrafficStats.getUidTxBytes(uid);

                // skip apps with zero transmitted data
                if (txBytesUID > 0) {
                    String packageName = app.packageName;
                    String name = (String) pm.getApplicationLabel(app);
                    Drawable icon = pm.getApplicationIcon(app);
                    double progress = ((double) txBytesUID / (double) totalTxBytes) * 100;

                    if (uid == 1000) {
                        name = "Android System";
                        icon = pm.getDefaultActivityIcon();
                    }
                    AppDataInfo obj = new AppDataInfo(uid, name, packageName, icon, progress, txBytesUID);
                    publishProgress(obj);

                    if (debug)
                        Log.d(TAG, "UID: " + uid + " | name: " + name + " | package: " + packageName + " | received: " + Util.getFileSize(rxBytesUID) + " | transmitted: " + Util.getFileSize(txBytesUID));
                }
            }
        }
        return totalTxBytes;
    }

    /**
     * Uses the NetworkStatsManager class. <br>
     * For every UID which is in uidsWithNetworkUsageMap (which will be filled when running getBytesSummary methods), all app-related information will be extracted if the app exists in the provided 'installedAppsList'
     *
     * @param context           context
     * @param installedAppsList list of apps to consider
     * @param calendar          calendar object which determines the starting time for collecting network usage data
     * @return the total bytes transmitted of all apps considered in the time frame
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private long useNetworkStats(Context context, List<ApplicationInfo> installedAppsList, Calendar calendar) {
        final PackageManager pm = context.getPackageManager();
        long txBytesWifiSummary = getBytesWifiSummary(context, calendar)[1];
        long txBytesMobileSummary = getBytesMobileSummary(context, calendar)[1];
        long txBytesSummary = txBytesWifiSummary + txBytesMobileSummary;

        long txBytesCounter = 0;
        progressBar.setMax(uidsWithNetworkUsageMap.size());

        int size = uidsWithNetworkUsageMap.size();
        for (int i = 0; i < size; i++) {
            int uid = uidsWithNetworkUsageMap.keyAt(i);
            if (isCancelled()) break;
            for (ApplicationInfo applicationInfo : installedAppsList) {
                // Escape early if cancel() is called
                if (isCancelled()) break;

                if (applicationInfo.uid == uid) {
                    long txBytesUID = uidsWithNetworkUsageMap.get(uid)[1];
                    txBytesCounter = txBytesCounter + txBytesUID;

                    String packageName = applicationInfo.packageName;
                    String name = (String) pm.getApplicationLabel(applicationInfo);
                    Drawable icon = pm.getApplicationIcon(applicationInfo);
                    double progress = ((double) txBytesUID / (double) txBytesSummary) * 100;

                    if (uid == 1000) {
                        name = "Android System";
                        icon = pm.getDefaultActivityIcon();
                    }
                    AppDataInfo obj = new AppDataInfo(uid, name, packageName, icon, progress, txBytesUID);
                    if (txBytesUID != 0) {
                        publishProgress(obj);
                    }
                    if (debug)
                        Log.d(TAG, "UID: " + uid + " | name: " + name + " | package: " + packageName + " | transmitted: " + Util.getFileSize(txBytesUID));
                    break;
                }
            }
        }
        if (debug) {
            Log.d(TAG, "Total Transmitted querySummary: " + Util.getFileSize(txBytesSummary));
            Log.d(TAG, "Total Transmitted txBytesCounter: " + Util.getFileSize(txBytesCounter));
        }
        return txBytesCounter;
    }

    @Override
    protected void onProgressUpdate(AppDataInfo... values) {
        appDataInfoList.add(values[0]);
        progressCounter++;
        progressBar.setProgress(progressCounter);
        Collections.sort(appDataInfoList, new Comparator<AppDataInfo>() {
            public int compare(AppDataInfo obj1, AppDataInfo obj2) {
                return Long.compare(obj2.transmitted, obj1.transmitted);
            }
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPostExecute(Long result) {
        if (debug) Log.d(TAG, "onPostExecute: called");
        // Check if activity / context still exists and is active
        Context context = contextReference.get();
        if (context != null) {
            Activity activity = (Activity) context;
            TextView textView = activity.findViewById(R.id.label_data_amount);
            if (textView != null)
                textView.setText(Util.getFileSize(result));
            progressBar.setVisibility(View.GONE);
        } else {
            if (debug) Log.d(TAG, "onPostExecute: context is null");
        }
    }

    /**
     * Retrieves the network usage data of all apps for the provided networkType, starting at the time of the provided calendar
     *
     * @param context     context
     * @param networkType for example ConnectivityManager.TYPE_WIFI or ConnectivityManager.TYPE_MOBILE
     * @param calendar    calendar object which determines the starting time for collecting network usage data
     * @return a long array consisting of two values; <br>
     * long[0] = total bytes received for the selected time frame <br>
     * long[1] = total bytes transmitted for the selected time frame
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private long[] getBytesSummary(Context context, int networkType, Calendar calendar) {
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.querySummary(
                    networkType,
                    Util.getSubscriberId(context, networkType),
                    calendar.getTimeInMillis(),
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            if (debug) Log.e(TAG, "getBytesSummary: " + e.toString());
        }
        long[] result = new long[2];
        long totalRxBytes = 0;
        long totalTxBytes = 0;

        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        if (networkStats != null) {
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                int uid = bucket.getUid();
                long uidRxBytes = bucket.getRxBytes();
                long uidTxBytes = bucket.getTxBytes();
                if (uidsWithNetworkUsageMap.indexOfKey(uid) < 0) {
                    long[] uidBytes = new long[2];
                    uidBytes[0] = uidRxBytes;
                    uidBytes[1] = uidTxBytes;
                    uidsWithNetworkUsageMap.put(uid, uidBytes);
                } else {
                    long[] value = uidsWithNetworkUsageMap.get(uid);
                    value[0] = value[0] + uidRxBytes;
                    value[1] = value[1] + uidTxBytes;
                    uidsWithNetworkUsageMap.put(uid, value);
                }
                totalRxBytes += bucket.getRxBytes();
                totalTxBytes += bucket.getTxBytes();
            }
            networkStats.close();
        }
        result[0] = totalRxBytes;
        result[1] = totalTxBytes;
        return result;
    }

    /**
     * Retrieves the network usage data of the app for the provided UID for the provided networkType, starting at the time of the provided calendar
     *
     * @param context     context
     * @param networkType for example ConnectivityManager.TYPE_WIFI or ConnectivityManager.TYPE_MOBILE
     * @param uid         UID of app
     * @param calendar    calendar object which determines the starting time for collecting network usage data
     * @return a long array consisting of two values; <br>
     * long[0] = total bytes received for the selected time frame <br>
     * long[1] = total bytes transmitted for the selected time frame
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static long[] getBytesUID(@NonNull Context context, int networkType, int uid, @NonNull Calendar calendar) {
        long[] result = new long[2];
        long rxBytes = 0;
        long txBytes = 0;
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStats networkStats = networkStatsManager.queryDetailsForUid(
                    networkType,
                    Util.getSubscriberId(context, networkType),
                    calendar.getTimeInMillis(),
                    Util.getCalendarOneDayLater().getTimeInMillis(),
                    uid);

            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            if (networkStats != null) {
                while (networkStats.hasNextBucket()) {
                    networkStats.getNextBucket(bucket);
                    rxBytes += bucket.getRxBytes();
                    txBytes += bucket.getTxBytes();
                }
                networkStats.close();
            }
            result[0] = rxBytes;
            result[1] = txBytes;
        return result;
    }

    /**
     * Retrieves the network usage data of all apps for the network type WIFI, starting at the time of the provided calendar
     *
     * @param context  context
     * @param calendar calendar object which determines the starting time for collecting network usage data
     * @return a long array consisting of two values; <br>
     * long[0] = total bytes received for the selected time frame <br>
     * long[1] = total bytes transmitted for the selected time frame
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public long[] getBytesWifiSummary(Context context, Calendar calendar) {
        return getBytesSummary(context, ConnectivityManager.TYPE_WIFI, calendar);
    }

    /**
     * Retrieves the network usage data of all apps for the network type MOBILE, starting at the time of the provided calendar
     *
     * @param context  context
     * @param calendar calendar object which determines the starting time for collecting network usage data
     * @return a long array consisting of two values; <br>
     * long[0] = total bytes received for the selected time frame <br>
     * long[1] = total bytes transmitted for the selected time frame
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public long[] getBytesMobileSummary(Context context, Calendar calendar) {
        return getBytesSummary(context, ConnectivityManager.TYPE_MOBILE, calendar);
    }

    /**
     * Retrieves the network usage data of the app for the provided UID for the network type MOBILE, starting at the time of the provided calendar
     *
     * @param context  context
     * @param calendar calendar object which determines the starting time for collecting network usage data
     * @return a long array consisting of two values; <br>
     * long[0] = total bytes received for the selected time frame <br>
     * long[1] = total bytes transmitted for the selected time frame
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static long[] getBytesWifiUID(Context context, int uid, Calendar calendar) {
        return getBytesUID(context, ConnectivityManager.TYPE_WIFI, uid, calendar);
    }

    /**
     * Retrieves the network usage data of the app for the provided UID for the network type MOBILE, starting at the time of the provided calendar
     *
     * @param context  context
     * @param calendar calendar object which determines the starting time for collecting network usage data
     * @return a long array consisting of two values; <br>
     * long[0] = total bytes received for the selected time frame <br>
     * long[1] = total bytes transmitted for the selected time frame
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static long[] getBytesMobileUID(Context context, int uid, Calendar calendar) {
        return getBytesUID(context, ConnectivityManager.TYPE_MOBILE, uid, calendar);
    }


}
