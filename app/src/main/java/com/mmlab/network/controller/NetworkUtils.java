package com.mmlab.network.controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getName();

    public static boolean isMobileEnabled(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean isAPEnabled(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static void setMobileDataEnabled(final Context context, boolean enabled) {
        final ConnectivityManager conman =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(
                    iConnectivityManager.getClass().getName());

            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (Exception e) {
            setMobileDataEnabled2(context, enabled);
            Log.d(TAG, e.toString(), e);
        }
    }

    public static void setMobileDataEnabled2(final Context context, boolean enabled) {

        Class[] cArg = new Class[2];
        cArg[0] = String.class;
        cArg[1] = Boolean.TYPE;
        final ConnectivityManager conman =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(
                    iConnectivityManager.getClass().getName());

            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", cArg);
            Object[] pArg = new Object[2];
            pArg[0] = context.getPackageName();
            pArg[1] = enabled;
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, pArg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setWifiEnabled(Context context, boolean enabled) {
        if (enabled)
            setAPEnabled(context, false);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }

    public static boolean setAPEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            if (enabled) {
                setWifiEnabled(context, false);
            }
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, null, enabled);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
