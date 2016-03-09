package com.mmlab.network.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mmlab.network.model.WifiDoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkManager {

    private static final String TAG = NetworkManager.class.getName();

    private Context mContext;

    private WifiManager mWifiManager;

    private OnResultsListener onResultsListener;

    public NetworkManager(Context context) {
        this.mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        init();
    }

    public void setOnResultsListener(OnResultsListener onResultsListener) {
        this.onResultsListener = onResultsListener;
    }

    public interface OnResultsListener {
        void onScanResults(ArrayList<ScanResult> scanResults);

        void onWifiDocs(ArrayList<WifiDoc> wifiDocs);
    }

    public void unregisterReceiver() {
        mContext.unregisterReceiver(broadcastReceiver);
    }

    public void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void release() {
        mHandler.removeCallbacksAndMessages(null);

        if (pHandler != null)
            pHandler.removeCallbacksAndMessages(null);
    }

    public void startScan() {
        mWifiManager.startScan();
    }

    public List<ScanResult> getScanResults() {
        return mWifiManager.getScanResults();
    }

    public WifiInfo getConnectionInfo() {
        return mWifiManager.getConnectionInfo();
    }

    public String getActivedSSID() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        return normalizedSSID(ssid);
    }

    public HashMap<String, WifiDoc> getWifiDocs() {
        List<ScanResult> scanResults = this.getScanResults();
        HashMap<String, WifiDoc> wifiDocs = new HashMap<>();
        for (ScanResult scanResult : scanResults) {
            WifiDoc wifiDoc;
            if (scanResult.SSID != null && !scanResult.SSID.isEmpty()) {
                if (wifiDocs.containsKey(scanResult.SSID)) {
                    wifiDoc = wifiDocs.get(scanResult.SSID);
                    if (calculateSignalStength(scanResult.level) > calculateSignalStength(wifiDoc.level)) {
                        wifiDoc.update(scanResult);
                    }
                } else {
                    wifiDoc = new WifiDoc();
                    wifiDoc.update(scanResult);
                }
                wifiDocs.put(scanResult.SSID, wifiDoc);
                wifiDocs.put(scanResult.SSID, new WifiDoc(scanResult));
            }
        }
        return wifiDocs;
    }

    public static int calculateSignalStength(int level) {
        return WifiManager.calculateSignalLevel(level, 5);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            if (onResultsListener != null) {
                ArrayList<ScanResult> scanResults = new ArrayList<>();
                scanResults.addAll(getScanResults());
                onResultsListener.onScanResults(scanResults);
            }

            String SSID = "";
            WifiInfo wifiInfo = getConnectionInfo();

            if (wifiInfo != null && wifiInfo.getSSID() != null) {
                SSID = NetworkManager.normalizedSSID(wifiInfo.getSSID());
            }

            HashMap<String, WifiDoc> wifiDocs = getWifiDocs();
            if (wifiInfo != null && wifiInfo.getSSID() != null && wifiDocs.containsKey(SSID)) {
                wifiDocs.get(SSID).state = WifiDoc.FINISHED;
                wifiDocs.get(SSID).level = wifiInfo.getRssi();
            }

            SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            if (supplicantState != null) {
                switch (supplicantState) {
                    case AUTHENTICATING:
                        Log.d(TAG, "AUTHENTICATING...");
                        if (wifiInfo != null && wifiInfo.getSSID() != null && wifiDocs.containsKey(SSID)) {
                            wifiDocs.get(SSID).state = WifiDoc.AUTHENTICATING;
                        }
                        break;
                    case COMPLETED:
                        Log.d(TAG, "COMPLETED...");
                        if (wifiInfo != null && wifiInfo.getSSID() != null && wifiDocs.containsKey(SSID)) {
                            wifiDocs.get(SSID).state = WifiDoc.COMPLETED;
                        }
                        break;
                    case DISCONNECTED:
                        Log.d(TAG, "DISCONNECTED...");
                        if (wifiInfo != null && wifiInfo.getSSID() != null && wifiDocs.containsKey(SSID)) {
                            wifiDocs.get(SSID).state = WifiDoc.DISCONNECTED;
                        }
                        break;
                    case FOUR_WAY_HANDSHAKE:
                        Log.d(TAG, "FOUR_WAY_HANDSHAKE...");
                        if (wifiInfo != null && wifiInfo.getSSID() != null && wifiDocs.containsKey(SSID)) {
                            wifiDocs.get(SSID).state = WifiDoc.AUTHENTICATING;
                        }
                        break;
                    case GROUP_HANDSHAKE:
                        Log.d(TAG, "GROUP_HANDSHAKE...");
                        if (wifiInfo != null && wifiInfo.getSSID() != null && wifiDocs.containsKey(SSID)) {
                            wifiDocs.get(SSID).state = WifiDoc.AUTHENTICATING;
                        }
                        break;
                    default:
                }
            }

            int supplicant_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if (supplicant_error == WifiManager.ERROR_AUTHENTICATING) {
                Log.d(TAG, "ERROR_AUTHENTICATING...");
            }

            ArrayList<WifiDoc> list = new ArrayList<>();
            if (wifiInfo != null && wifiInfo.getSSID() != null && wifiDocs.containsKey(SSID)) {
                WifiDoc wifiDoc = wifiDocs.get(SSID);
                wifiDocs.remove(SSID);

                list.add(wifiDoc);
                list.addAll(new ArrayList<>(wifiDocs.values()));
                wifiDocs.put(SSID, wifiDoc);
            } else {
                list.addAll(new ArrayList<>(wifiDocs.values()));
            }

            mHandler.obtainMessage(STATE_CHANGE_ACTION, list).sendToTarget();
            if (onResultsListener != null) {
                onResultsListener.onWifiDocs(list);
            }
        }
    };

    public static String normalizedSSID(String ssid) {
        return ssid.replaceFirst("^\"", "").replaceFirst("\"$", "");
    }

    public boolean connect(WifiDoc wifiDoc) {
        WifiConfiguration wifiConfiguration = getConfiguredNetwork(wifiDoc.SSID);
        disableNetwork();
        if (wifiConfiguration != null) {
            Log.d(TAG, "connect()...success");
            return connectConfigured(wifiDoc.SSID);
        } else {
            Log.d(TAG, "connect()...fail");
            return connectUnconfigured(wifiDoc);
        }
    }

    public boolean connectUnconfigured(WifiDoc wifiDoc) {
        WifiConfiguration wifiConfiguration = getConfiguredNetwork(wifiDoc.SSID);
        if (wifiConfiguration != null) {
            mWifiManager.removeNetwork(wifiConfiguration.networkId);
        }

        WifiConfiguration wifiConfig = this.createWifiInfo(wifiDoc.SSID, wifiDoc.SSIDpwd, WifiDoc.getType(wifiDoc.capabilities));
        if (wifiConfig == null) {
            Log.i(TAG, "wifiConfig is null");
            return false;
        }
        int netID = mWifiManager.addNetwork(wifiConfig);

        return mWifiManager.enableNetwork(netID, true) && mWifiManager.saveConfiguration() && mWifiManager.reconnect();
    }

    private WifiConfiguration createWifiInfo(String SSID, String SSIDpwd, int type) {

        WifiConfiguration config = new WifiConfiguration();

        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + SSID + "\"";
        config.status = WifiConfiguration.Status.DISABLED;
        config.priority = 40;

        if (type == WifiDoc.NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            return config;
        } else if (type == WifiDoc.WEP) {
            config.wepKeys[0] = "\"" + SSIDpwd + "\"";
            config.hiddenSSID = true;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.wepTxKeyIndex = 0;

            return config;
        } else if (type == WifiDoc.WPA) {
            config.preSharedKey = "\"" + SSIDpwd + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.status = WifiConfiguration.Status.ENABLED;
            return config;
        } else {
            return null;
        }
    }

    public WifiConfiguration getConfiguredNetwork(String SSID) {
        List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
        if (configurations != null) {
            for (WifiConfiguration wifiConfiguration : configurations) {
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals("\"" + SSID + "\"")) {
                    return wifiConfiguration;
                }
            }
        }
        return null;
    }

    public boolean connectConfigured(String SSID) {
        WifiConfiguration wifiConfiguration = getConfiguredNetwork(SSID);
        return (mWifiManager.enableNetwork(wifiConfiguration.networkId, true) && mWifiManager.reconnect());
    }

    public boolean disableNetwork() {
        int netId = mWifiManager.getConnectionInfo().getNetworkId();
        return netId < 0 || mWifiManager.disableNetwork(netId);
    }

    public boolean disconnectNetwork(int netId) {
        return netId < 0 || mWifiManager.removeNetwork(netId);
    }

    private static final int STATE_CHANGE_ACTION = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            int what = msg.what;
            Object object = msg.obj;
            switch (what) {
                default:
            }
            super.handleMessage(msg);
        }
    };

    private Handler pHandler;

    public void init() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        pHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                int what = msg.what;
                Object object = msg.obj;
                switch (what) {
                    default:
                }
                super.handleMessage(msg);
            }
        };
    }
}
