package com.rcmapps.safetycharger.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.rcmapps.safetycharger.R;
import com.rcmapps.safetycharger.activites.AboutActivity;
import com.rcmapps.safetycharger.activites.SettingsActivity;
import com.rcmapps.safetycharger.inappbilling.BillingCallback;
import com.rcmapps.safetycharger.inappbilling.InappBillingManager;
import com.rcmapps.safetycharger.interfaces.Callback;
import com.rcmapps.safetycharger.interfaces.SettingsView;
import com.rcmapps.safetycharger.presenters.SettingsFragmentPresenter;
import com.rcmapps.safetycharger.utils.InstructionManager;
import com.rcmapps.safetycharger.utils.PreferenceContants;
import com.rcmapps.safetycharger.utils.SharedPreferenceUtils;
import com.rcmapps.safetycharger.utils.UtilMethods;

public class SettingsFragment extends PreferenceFragmentCompat implements SettingsView, Callback, BillingCallback {

    private SettingsFragmentPresenter presenter;
    private PasswordChangeDialogFragment passwordChangeDialogFragment = new PasswordChangeDialogFragment();
    private InstructionManager instructionManager = new InstructionManager();
    private InappBillingManager billingManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new SettingsFragmentPresenter(this);

        billingManager = new InappBillingManager(getActivity());
        billingManager.setBillingCallback(this);

        if(getBooleanPref(PreferenceContants.KEY_IS_FIRSTRUN,true)){
            instructionManager.showInstructionOnTapPasswordPref(getActivity(), this);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_settings);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        switch (preference.getKey()) {
            case PreferenceContants.KEY_PASSWORD:
                checkIfPasswordisAlreadySet();
                break;
            case PreferenceContants.KEY_RESET_ALARM:
                showConfirmationDialog();
                break;
            case PreferenceContants.KEY_REMOVE_AD:

                if(billingManager!=null){
                    billingManager.setup();
                }
                break;
            case PreferenceContants.KEY_ABOUT: {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                break;
            }
        }

        return true;
    }

    private void checkIfPasswordisAlreadySet(){

        String password =SharedPreferenceUtils.getInstance(getActivity()).getString(PreferenceContants.KEY_PASSWORD,"");

        if(password.isEmpty()){
            showPasswordChangeDialog();
        }
        else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage(getActivity().getString(R.string.password_already_set));
            dialog.setCancelable(false);
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showPasswordChangeDialog();
                }
            });
            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.create();
            dialog.show();
        }
    }

    private void showPasswordChangeDialog(){
        passwordChangeDialogFragment = new PasswordChangeDialogFragment();
        passwordChangeDialogFragment.setPresenter(presenter);
        passwordChangeDialogFragment.show(getChildFragmentManager(), PasswordChangeDialogFragment.class.getSimpleName());
    }

    private void showConfirmationDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(getResourceString(R.string.confirm));
        dialog.setMessage(getResourceString(R.string.sure_to_change_alarm));
        dialog.setPositiveButton(getResourceString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferenceUtils.getInstance(getActivity()).clear(PreferenceContants.KEY_SELECTED_ALARM_TONE_URI);
            }
        });
        dialog.setNegativeButton(getResourceString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    @Override
    public void saveNewPassword(String newPassword) {
        SharedPreferenceUtils.getInstance(getActivity()).putString(PreferenceContants.KEY_PASSWORD, newPassword);
    }

    @Override
    public void closePasswordChangeDialog() {

        if (passwordChangeDialogFragment != null) {
            passwordChangeDialogFragment.dismiss();
        }
    }

    @Override
    public void continueInstruction() {
        instructionManager.showInstructionOnTapBackButton(getActivity(), ((SettingsActivity)getActivity()).getToolbar());
    }

    @Override
    public void onRestorePurchase(boolean mIsPremium) {
        if(mIsPremium){
            UtilMethods.showSimpleAlertWithMessage(getActivity(),"Alert","Already purchased.");
        }
        else{
            billingManager.startBilling();
        }

    }

    @Override
    public void onPurchaseSuccess() {
        SharedPreferenceUtils.getInstance(getActivity()).putBoolean(PreferenceContants.KEY_PREMIUM,true);
    }

    @Override
    public void onPurchaseFailure(String message) {
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String title, String message) {
        UtilMethods.showSimpleAlertWithMessage(getActivity(), title, message);
    }

    @Override
    public String getResourceString(int stringId) {
        return getActivity().getResources().getString(stringId);
    }

    @Override
    public boolean getBooleanPref(String prefName, boolean defaultVal) {
        return SharedPreferenceUtils.getInstance(getActivity()).getBoolean(prefName, defaultVal);
    }

    @Override
    public void showToast(String message) {
        UtilMethods.showToastMessage(getActivity(), message);
    }

    @Override
    public void closeApp() {

    }

    @Override
    public void onTargetViewDismissed() {
        showPasswordChangeDialog();
    }
}
