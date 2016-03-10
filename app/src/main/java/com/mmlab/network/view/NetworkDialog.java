package com.mmlab.network.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.support.v7.widget.SwitchCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mmlab.network.R;
import com.mmlab.network.controller.NetworkUtils;


public class NetworkDialog extends DialogFragment {

    private SwitchCompat switch_wifi = null;
    private SwitchCompat switch_mobile = null;
    private SwitchCompat switch_hotspot = null;

    public NetworkDialog() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onDetach() {
        super.onDetach();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_network, null);
        switch_wifi = (SwitchCompat) view.findViewById(R.id.switch_wifi);
        switch_wifi.setChecked(NetworkUtils.isWifiEnabled(getActivity()));
        switch_wifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    NetworkUtils.setWifiEnabled(getActivity(), true);
                    switch_hotspot.setChecked(false);
                } else {
                    NetworkUtils.setWifiEnabled(getActivity(), false);
                }
            }
        });
        switch_mobile = (SwitchCompat) view.findViewById(R.id.switch_mobile);
        switch_mobile.setChecked(NetworkUtils.isMobileEnabled(getActivity()));
        switch_mobile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    NetworkUtils.setMobileDataEnabled(getActivity(), true);
                } else {
                    NetworkUtils.setMobileDataEnabled(getActivity(), false);
                }
            }
        });
        switch_hotspot = (SwitchCompat) view.findViewById(R.id.switch_hotspot);
        switch_hotspot.setChecked(NetworkUtils.isAPEnabled(getActivity()));
        switch_hotspot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    NetworkUtils.setAPEnabled(getActivity(), true);
                    switch_wifi.setChecked(false);
                } else {
                    NetworkUtils.setAPEnabled(getActivity(), false);
                }
            }
        });

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.title_dialog_network)
                .customView(view, true)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.dismiss)
                .contentLineSpacing(1.6f)
                .build();
    }
}
