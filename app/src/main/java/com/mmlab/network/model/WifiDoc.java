package com.mmlab.network.model;

import android.net.wifi.ScanResult;

import java.io.Serializable;

public class WifiDoc implements Serializable {
    private static final long serialVersionUID = 5117886679575134589L;

    public static final String AUTHENTICATING = "WifiDoc.AUTHENTICATING";
    public static final String COMPLETED = "WifiDoc.COMPLETED";
    public static final String DISCONNECTED = "WifiDoc.DISCONNECTED";
    public static final String FINISHED = "WifiDoc.FINISHED";
    public static final String ERROR_AUTHENTICATING = "WifiDoc.ERROR_AUTHENTICATING";

    public static final int WPA = 0;
    public static final int WPA2 = 1;
    public static final int WEP = 2;
    public static final int WPS = 3;
    public static final int NOPASS = 4;

    public String SSID;

    public String SSIDpwd;

    public String BSSID;

    public String capabilities;

    public String status;

    public String state;

    public int level;

    public int frequency;

    public WifiDoc() {

    }

    public WifiDoc(ScanResult scanResult) {
        update(scanResult);
    }

    public void update(ScanResult scanResult) {
        this.SSID = scanResult.SSID;
        this.SSIDpwd = "";
        this.BSSID = scanResult.BSSID;
        this.capabilities = scanResult.capabilities;
        this.level = scanResult.level;
        this.frequency = scanResult.frequency;
        this.status = "";
        this.state = "";
    }

    public String getState() {
        String security = "";
        switch (state) {
            case FINISHED:
                security = "已連線";
                break;
            case COMPLETED:
                security = "認證中";
                break;
            case AUTHENTICATING:
                security = "認證中";
                break;
            case DISCONNECTED:
                security = "中斷連線";
                break;
            case ERROR_AUTHENTICATING:
                security = "認證失敗";
                break;
            default:
                String capabilities = this.capabilities;
                if (capabilities.contains("WPA") && capabilities.contains("WPA2")) {
                    security = "透過WPA/WPA2加密保護";
                    if (capabilities.contains("WPS")) {
                        security += "(可使用WPS)";
                    }
                } else if (capabilities.contains("WPA")) {
                    security = "透過WPA加密保護";
                    if (capabilities.contains("WPS")) {
                        security += "(可使用WPS)";
                    }
                } else if (capabilities.contains("WEP") || capabilities.contains("IEEE")) {
                    security = "透過WEP加密保護";
                    if (capabilities.contains("WPS")) {
                        security += "(可使用WPS)";
                    }
                } else if (capabilities.contains("WPA2")) {
                    security = "透過WPA2加密保護";
                    if (capabilities.contains("WPS")) {
                        security += "(可使用WPS)";
                    }
                }
        }
        return security;
    }

    public static int getType(String capabilities) {
        if (capabilities.contains("WEP") || capabilities.contains("IEEE")) {
            return WifiDoc.WEP;
        } else if (capabilities.contains("WPA") || capabilities.contains("WPA2"))
            return WifiDoc.WPA;
        else {
            return WifiDoc.NOPASS;
        }
    }

    public String getSecurity() {
        String status = "";
        String capabilities = this.capabilities;
        if (capabilities.contains("WPA") && capabilities.contains("WPA2")) {
            status = "WPA/WPA2";
            if (capabilities.contains("PSK")) {
                status += " PSK";
            }
        } else if (capabilities.contains("WPA")) {
            status = "WPA";
            if (capabilities.contains("PSK")) {
                status += " PSK";
            }
        } else if (capabilities.contains("WPA2")) {
            status = "WPA2";
            if (capabilities.contains("PSK")) {
                status += " PSK";
            }
        } else if (capabilities.contains("WEP") || capabilities.contains("IEEE")) {
            status = "WEP";
            if (capabilities.contains("PSK")) {
                status += " PSK";
            }
        } else {
            status = "無";
        }
        return status;
    }

    public int getWifiEncrypt() {
        if (capabilities.contains("WPA") || capabilities.contains("WPA2")) {
            return WPA;
        }
        if (capabilities.contains("WEP") || capabilities.contains("IEEE")) {
            return WEP;
        }
        return NOPASS;
    }
}
