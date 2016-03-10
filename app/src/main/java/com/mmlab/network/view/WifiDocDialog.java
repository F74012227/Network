package com.mmlab.network.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mmlab.network.R;
import com.mmlab.network.controller.NetworkManager;
import com.mmlab.network.model.WifiDoc;

public class WifiDocDialog extends DialogFragment {
    private static String TAG = "WifiDocDialog";

    private static MaterialDialog materialDialog = null;

    private WifiDoc wifiDoc;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiDoc = (WifiDoc) getArguments().getSerializable("wifidoc");
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final NetworkManager networkManager = new NetworkManager(getActivity().getApplicationContext());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.wifidoc_dialog, null);
        final MaterialDialog.Builder materialDialogBuilder = new MaterialDialog.Builder(getActivity());
        materialDialogBuilder.customView(promptView, true);

        final TextView securityshowTextView = (TextView) promptView.findViewById(R.id.securityshow_textView);
        final TextView passwordTextView = (TextView) promptView.findViewById(R.id.password_textView);
        final EditText passwordshowEditView = (EditText) promptView.findViewById(R.id.passwordshow_editText);
        final CheckBox checkBox = (CheckBox) promptView.findViewById(R.id.checkBox);

        if (wifiDoc.getWifiEncrypt() == WifiDoc.NOPASS || networkManager.getConfiguredNetwork(wifiDoc.SSID) != null || !wifiDoc.SSIDpwd.isEmpty()) {
            passwordTextView.setVisibility(View.GONE);
            passwordshowEditView.setVisibility(View.GONE);
            checkBox.setVisibility(View.GONE);
        } else {
            passwordshowEditView.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.d(TAG, "beforeTextChanged()...");
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "onTextChanged()...");
                }

                public void afterTextChanged(Editable s) {
                    Log.d(TAG, "afterTextChanged()...");
                    if (passwordshowEditView.getText() != null && !passwordshowEditView.getText().toString().isEmpty()) {
                        materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    } else {
                        materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    }
                }
            });

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        passwordshowEditView.setTransformationMethod(null);
                    } else {
                        passwordshowEditView.setTransformationMethod(new PasswordTransformationMethod());
                    }
                }
            });
        }
        // 設置安全性
        securityshowTextView.setText(wifiDoc.getSecurity());

        // 建立對話窗視窗
        materialDialogBuilder.title(wifiDoc.SSID);

        if (networkManager.getConfiguredNetwork(wifiDoc.SSID) != null) {

            materialDialogBuilder.neutralText(R.string.clear).onNeutral(new MaterialDialog.SingleButtonCallback() {
                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                    networkManager.disconnectNetwork(networkManager.getConfiguredNetwork(wifiDoc.SSID).networkId);
                }
            });
        }

        if (!networkManager.getActivedSSID().equals(wifiDoc.SSID)) {
            materialDialogBuilder.positiveText(R.string.connect).onPositive(new MaterialDialog.SingleButtonCallback() {
                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                    if (passwordshowEditView.getVisibility() == View.VISIBLE) {
                        wifiDoc.SSIDpwd = passwordshowEditView.getText().toString();
                    }
                    networkManager.connect(wifiDoc);
                }
            });
        }

        materialDialogBuilder.negativeText(R.string.dismiss).onNegative(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                materialDialog.dismiss();
            }
        });


        materialDialog = materialDialogBuilder
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.colorPrimary).build();
        materialDialog.show();
        if (wifiDoc.getWifiEncrypt() == WifiDoc.NOPASS || networkManager.getConfiguredNetwork(wifiDoc.SSID) != null || !wifiDoc.SSIDpwd.isEmpty()) {
            materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
        } else {
            materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
        }
        // 創建對話窗視窗
        return materialDialog;
    }
}
