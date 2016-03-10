package com.mmlab.network.view;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mmlab.network.R;
import com.mmlab.network.controller.NetworkUtils;
import com.mmlab.network.controller.WifiAPManager;

public class HotspotDialog extends DialogFragment {

    public static final String TAG = "HotspotDialog";

    public void setOnChangedListener(OnChangedListener onChangedListener) {
        this.onChangedListener = onChangedListener;
    }

    public OnChangedListener onChangedListener = null;

    public HotspotDialog() {
        // Required empty public constructor
    }

    public interface OnChangedListener {
        void onChanged();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onDetach() {
        super.onDetach();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hotspot, null);

        final WifiAPManager wifiAPManager = new WifiAPManager(getActivity().getApplicationContext());
        final WifiConfiguration newWifiConfiguration = new WifiConfiguration();
        final WifiConfiguration wifiConfiguration = wifiAPManager.getWifiApConfiguration();
        final String preKey = wifiAPManager.getPrevKey();
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinnner);
        final EditText passwordshow_editText = (EditText) view.findViewById(R.id.passwordshow_editText);
        final EditText ssidshow_editText = (EditText) view.findViewById(R.id.ssidshow_editText);
        final TextView password_textView = (TextView) view.findViewById(R.id.password_textView);
        final TextView passwordhint_textView = (TextView) view.findViewById(R.id.passwordhint_textView);
        ssidshow_editText.setText(wifiConfiguration.SSID);

        ArrayAdapter adapter;
        if (wifiAPManager.isHtc) {
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, new String[]{"無", "WPA(TKIP)", "WPA2(AES)", "WEP(128)"});
        } else {
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, new String[]{"無", "WPA(TKIP)", "WPA2(AES)"});
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
                if (position == 0) {
                    passwordshow_editText.setVisibility(View.GONE);
                    passwordhint_textView.setVisibility(View.GONE);
                    password_textView.setVisibility(View.GONE);
                } else {
                    passwordshow_editText.setVisibility(View.VISIBLE);
                    passwordhint_textView.setVisibility(View.VISIBLE);
                    password_textView.setVisibility(View.VISIBLE);
                    passwordshow_editText.setText(preKey);
                }
            }

            public void onNothingSelected(AdapterView arg0) {
            }
        });

        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            passwordshow_editText.setVisibility(View.GONE);
            spinner.setSelection(0);
        } else if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            passwordshow_editText.setText(wifiConfiguration.preSharedKey);
            spinner.setSelection(1);
        } else if (wifiConfiguration.allowedKeyManagement.get(4)) {
            passwordshow_editText.setText(wifiConfiguration.preSharedKey);
            spinner.setSelection(2);
        } else if (wifiAPManager.isHtc && wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            passwordshow_editText.setText(wifiConfiguration.wepKeys[wifiConfiguration.wepTxKeyIndex]);
            spinner.setSelection(3);
        }


        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordshow_editText.setTransformationMethod(null);
                } else {
                    passwordshow_editText.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });


        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.title_dialog_hotspot)
                .customView(view, true)
                .positiveText(R.string.save)
                .negativeText(R.string.dismiss)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.colorPrimary)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        newWifiConfiguration.SSID = ssidshow_editText.getText().toString();
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                                wifiConfiguration.preSharedKey = preKey;
                                break;
                            case 1:
                                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                                newWifiConfiguration.preSharedKey = passwordshow_editText.getText().toString();
                                break;
                            case 2:
                                newWifiConfiguration.allowedKeyManagement.set(4);
                                newWifiConfiguration.preSharedKey = passwordshow_editText.getText().toString();
                                break;
                            case 3:
                                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                                newWifiConfiguration.preSharedKey = passwordshow_editText.getText().toString();
                                break;
                            default:
                        }
                        wifiAPManager.setWifiApConfiguration(newWifiConfiguration);
                    }
                })
                .contentLineSpacing(1.6f)
                .build();
        if (NetworkUtils.isAPEnabled(getActivity().getApplicationContext())) {
            passwordshow_editText.setEnabled(false);
            ssidshow_editText.setEnabled(false);
            checkBox.setEnabled(false);
            View positive = dialog.getActionButton(DialogAction.POSITIVE);
            positive.setEnabled(false);
            spinner.setEnabled(false);
        }
        return dialog;
    }
}

