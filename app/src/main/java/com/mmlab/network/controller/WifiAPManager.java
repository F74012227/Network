package com.mmlab.network.controller;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class WifiAPManager {

    private static final String TAG = WifiAPManager.class.getName();

    private Context mContext;
    private WifiManager mWifiManager;
    private static final Map<String, Method> methodMap = new HashMap<String, Method>();
    public boolean isHtc = false;

    public WifiAPManager(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        try {
            Field field = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
            isHtc = field != null;
        } catch (Exception ignored) {
        }

        try {
            Method method = WifiManager.class.getMethod("getWifiApState");
            methodMap.put("getWifiApState", method);
        } catch (SecurityException | NoSuchMethodException ignored) {
        }

        try {
            Method method = WifiManager.class.getMethod("getWifiApConfiguration");
            methodMap.put("getWifiApConfiguration", method);
        } catch (SecurityException | NoSuchMethodException ignored) {
        }

        try {
            Method method = WifiManager.class.getMethod(getSetWifiApConfigName(), WifiConfiguration.class);
            methodMap.put("setWifiApConfiguration", method);
        } catch (SecurityException | NoSuchMethodException ignored) {
        }

        try {
            Method method = WifiManager.class.getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            methodMap.put("setWifiApEnabled", method);
        } catch (SecurityException | NoSuchMethodException ignored) {
        }
    }

    public int getWifiApState() {
        try {
            Method method = methodMap.get("getWifiApState");
            return (Integer) method.invoke(mWifiManager);
        } catch (IllegalAccessException e) {
            Log.d(TAG, e.toString(), e);
        } catch (InvocationTargetException e) {
            Log.d(TAG, e.toString(), e);
        }
        return -1;
    }

    private void getHtcWifiApConfiguration(WifiConfiguration wifiConfiguration) {
        try {
            Object mWifiApProfileValue = getFieldValue(wifiConfiguration, "mWifiApProfile");
            if (mWifiApProfileValue != null) {
                wifiConfiguration.SSID = (String) getFieldValue(mWifiApProfileValue, "SSID");
                switch ((String) getFieldValue(mWifiApProfileValue, "secureType")) {
                    case "open":
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        wifiConfiguration.wepKeys[0] = "";
                        wifiConfiguration.wepTxKeyIndex = 0;
                        wifiConfiguration.preSharedKey = (String) getFieldValue(mWifiApProfileValue, "key");
                        break;
                    case "wpa-psk":
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConfiguration.preSharedKey = (String) getFieldValue(mWifiApProfileValue, "key");
                        break;
                    case "wpa2-psk":
                        int WPA2_PSK = 4;
                        wifiConfiguration.allowedKeyManagement.set(WPA2_PSK);
                        wifiConfiguration.preSharedKey = (String) getFieldValue(mWifiApProfileValue, "key");
                        break;
                    case "wep":
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                        wifiConfiguration.wepKeys[0] = (String) getFieldValue(mWifiApProfileValue, "key");
                        wifiConfiguration.wepTxKeyIndex = 0;
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
        }
    }

    public String getPrevKey() {
        WifiConfiguration configuration = null;
        String preKey = "";
        try {
            Method method = methodMap.get("getWifiApConfiguration");
            configuration = (WifiConfiguration) method.invoke(mWifiManager);
            if (isHtc)
                getHtcWifiApConfiguration(configuration);
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
        }

        if (configuration != null)
            if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                preKey = configuration.preSharedKey;
            } else if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                preKey = configuration.preSharedKey;
            } else if (configuration.allowedKeyManagement.get(4)) {
                preKey = configuration.preSharedKey;
            } else if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                preKey = configuration.wepKeys[configuration.wepTxKeyIndex];
            }
        return preKey;
    }

    public WifiConfiguration getWifiApConfiguration() {
        WifiConfiguration configuration = new WifiConfiguration();
        try {
            Method method = methodMap.get("getWifiApConfiguration");
            configuration = (WifiConfiguration) method.invoke(mWifiManager);
        } catch (Exception ignored) {
        }

        if (configuration == null || configuration.SSID == null) {
            if (isHtc)
                getHtcWifiApConfiguration(configuration);
        }

        return configuration;
    }

    private void setupHtcWifiConfiguration(WifiConfiguration config) {
        try {
            Object mWifiApProfileValue = getFieldValue(config, "mWifiApProfile");

            if (mWifiApProfileValue != null) {
                setFieldValue(mWifiApProfileValue, "SSID", config.SSID);
                if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                    setFieldValue(mWifiApProfileValue, "secureType", "open");
                    setFieldValue(mWifiApProfileValue, "key", config.preSharedKey);
                } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                    setFieldValue(mWifiApProfileValue, "secureType", "wpa-psk");
                    setFieldValue(mWifiApProfileValue, "key", config.preSharedKey);
                } else if (config.allowedKeyManagement.get(4)) {
                    setFieldValue(mWifiApProfileValue, "secureType", "wpa2-psk");
                    setFieldValue(mWifiApProfileValue, "key", config.preSharedKey);
                } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                    setFieldValue(mWifiApProfileValue, "secureType", "wep");
                    setFieldValue(mWifiApProfileValue, "key", config.wepKeys[config.wepTxKeyIndex]);
                }
                setFieldValue(mWifiApProfileValue, "dhcpEnable", 1);
                setFieldValue(mWifiApProfileValue, "ipAddress", "192.168.1.1");
                setFieldValue(mWifiApProfileValue, "dhcpSubnetMask", "255.255.255.0");
                setFieldValue(mWifiApProfileValue, "startingIP", "192.168.1.100");
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
        }
    }

    public boolean setWifiApConfiguration(WifiConfiguration config) {
        boolean result = false;
        try {
            if (isHtc)
                setupHtcWifiConfiguration(config);

            Method method = methodMap.get("setWifiApConfiguration");

            if (isHtc) {
                int value = (Integer) method.invoke(mWifiManager, config);
                result = value > 0;
            } else {
                result = (Boolean) method.invoke(mWifiManager, config);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return result;
    }


    private String getSetWifiApConfigName() {
        return isHtc ? "setWifiApConfig" : "setWifiApConfiguration";
    }

    private Object getFieldValue(Object object, String propertyName)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(propertyName);
        field.setAccessible(true);
        return field.get(object);
    }

    private void setFieldValue(Object object, String propertyName, Object value)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(propertyName);
        field.setAccessible(true);
        field.set(object, value);
        field.setAccessible(false);
    }
}
