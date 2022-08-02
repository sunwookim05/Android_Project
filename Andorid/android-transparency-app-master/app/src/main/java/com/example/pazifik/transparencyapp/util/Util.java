package com.example.pazifik.transparencyapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Util {

    /**
     * Returns the respective file size text representation of a bytes value
     *
     * @param size file size in bytes as a long object
     * @return Returns the String representation of the converted Long value <br>
     * Example: 12 B, 3 KB, 4.8 MB, 5.1 GB, 6 TB
     */
    public static String getFileSize(long size) {
        if (size == 0)
            return "0";
        else {
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }
    }

    /**
     * Returns a List of all currently installed applications
     *
     * @param context context
     * @return Returns a List of ApplicationInfo Objects which contain information about all installed applications
     */
    public static List<ApplicationInfo> getInstalledApplications(Context context) {
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        return packages;
    }

    /**
     * Returns a List of all currently installed applications which are not system apps.
     * A non-system app can be an app which is installed from the play store or an pre-installed app by the device manufacturer which was updated by the user
     *
     * @param context context
     * @return Returns a List of ApplicationInfo Objects which contain information about all installed non-system applications
     */
    public static List<ApplicationInfo> getNonSystemInstalledApplications(Context context) {
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> tempList = new ArrayList<>();
        for (ApplicationInfo applicationInfo : packages) {
            if (!isSystemPackage(applicationInfo)) {
                tempList.add(applicationInfo);
            }
        }
        return tempList;
    }

    /**
     * Checks if the provided app (ApplicationInfo object) is an Android system package
     *
     * @param applicationInfo ApplicationInfo object
     * @return Returns false if the app is not a system app (e.g. from the play store) or
     * a system app which was updated with a newer version (e.g. manufacturer app updated); otherwise true
     */
    public static boolean isSystemPackage(ApplicationInfo applicationInfo) {
        boolean result = true;
        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            // NON-SYSTEM PLAY STORE
            result = false;
        } else if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            // NON-SYSTEM MANUFACTURER
            result = false;
        }
        return result;
    }

    /**
     * Returns the calendar representing today's date at time 00:00:00.00
     *
     * @return calendar object representing today's date at time 00:00:00.00
     */
    public static Calendar getCalendarToday() {
        Calendar calendar = Calendar.getInstance();
        // for 0-12 clocks
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        // for 0-24 clocks
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
//        Log.i("DataUsage", "getCalendarToday: " + calendar.getTime());
        return calendar;
    }

    /**
     * Returns the calendar representing the current's date and time PLUS one day
     *
     * @return calendar object representing current's date and time PLUS one day
     */
    public static Calendar getCalendarOneDayLater() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
//        Log.i("DataUsage", "getCalendarOneDayLater: " + calendar.getTime());
        return calendar;
    }

    /**
     * Returns the calendar representing the first day of the current week at time 00:00:00.00
     *
     * @return calendar object representing the first day of the current week at time 00:00:00.00
     */
    public static Calendar getCalendarCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        // for 0-12 clocks
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        // for 0-24 clocks
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
//        Log.i("DataUsage", "getCalendarCurrentWeek: " + calendar.getTime());
        return calendar;
    }

    /**
     * Returns the calendar representing the first day of the current month at time 00:00:00.00
     *
     * @return calendar object representing the first day of the current month at time 00:00:00.00
     */
    public static Calendar getCalendarCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // for 0-12 clocks
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        // for 0-24 clocks
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
//        Log.i("DataUsage", "getCalendarCurrentMonth: " + calendar.getTime());
        return calendar;
    }

    /**
     * Returns the calendar representing the first day at time 00:00:00.00 three months back from current time
     *
     * @return calendar object representing the first day at time 00:00:00.00 three months back from current time
     */
    public static Calendar getCalendarLastThreeMonths() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        // for 0-12 clocks
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        // for 0-24 clocks
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
//        Log.i("DataUsage", "getCalendarLastThreeMonths: " + calendar.getTime());
        return calendar;
    }

    /**
     * Returns the unique subscriber ID, for example, the IMSI for a GSM phone
     *
     * @param context     context
     * @param networkType network type, e.g. ConnectivityManager.TYPE_MOBILE or ConnectivityManager.TYPE_WIFI
     * @return subscriber ID if both the provided networkType is ConnectivityManager.TYPE_MOBILE and the Manifest.permission.READ_PHONE_STATS is allowed, otherwise returns ""
     */
    public static String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return "";
            } else {
                return tm.getSubscriberId();
            }
        }
        return "";
    }

    /**
     * Runs the provided cmd command and returns the output
     *
     * @param cmd command
     * @return output of cmd command
     * @throws IOException
     * @throws InterruptedException
     */
    public static String runCommand(String cmd) throws IOException, InterruptedException {
        Process ns;
        BufferedReader br = null;
        StringBuilder output = new StringBuilder();
        try {
            ns = Runtime.getRuntime().exec(cmd);
            ns.waitFor();
            br = new BufferedReader(new InputStreamReader(ns.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            while ((read = br.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
        } finally {
            Util.close(br);
        }
        return output.toString();
    }

    public static String decodeIp4(String s) {
        return Integer.parseInt(s.substring(6, 8), 16) + "."
                + Integer.parseInt(s.substring(4, 6), 16) + "."
                + Integer.parseInt(s.substring(2, 4), 16) + "."
                + Integer.parseInt(s.substring(0, 2), 16) + ":"
                + Integer.parseInt(s.substring(9), 16);
    }

    public static String decodeIp6(String s) {
        StringBuilder ip6 = new StringBuilder();
        for (int i = 0; i < s.length(); i += 4) {
            if (s.substring(i, i + 4).equalsIgnoreCase("0000")) {
                continue;
            }
            if (i == 16 && s.substring(i, i + 4).equalsIgnoreCase("FFFF")) {
                ip6.append("").append(Integer.parseInt(s.substring(30, 32), 16)).append(".").append(Integer.parseInt(s.substring(28, 30), 16)).append(".").append(Integer.parseInt(s.substring(26, 28), 16)).append(".").append(Integer.parseInt(s.substring(24, 26), 16));
                return ip6.toString();
            }
            ip6.append(s.substring(i, i + 4)).append((i == 28) ? "" : ":");
        }
        return ip6.toString();
    }

    public static String getHostnameFromIP(final String ip) throws UnknownHostException {
        InetAddress inetAddr = InetAddress.getByName(ip);
        String hostname = inetAddr.getHostName();
        if (!hostname.equals(ip))
            return hostname;
        return "";
    }

    /**
     * Closes the IO resource (null-check included)
     *
     * @param io IO resource
     */
    public static void close(Closeable io) {
        try {
            if (io != null) io.close();
        } catch (IOException e) {
        }
    }
}

