package com.example.videoconferenceapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.example.videoconferenceapp.helperFunctions.FieldValidation;
import com.example.videoconferenceapp.model.Constant;
import com.example.videoconferenceapp.model.Methods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

import petrov.kristiyan.colorpicker.ColorPicker;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettingsFragment()).commit();
    }


    public static class MainSettingsFragment extends PreferenceFragmentCompat {

        private SwitchPreference darkModeSwitch;
        private Preference changePassword, changeTheme;
        private FirebaseDatabase firebaseDatabase;
        private DatabaseReference databaseReference;
        FirebaseAuth firebaseAuth;
        FirebaseUser user;
        private Methods methods;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Users");
            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();

            methods = new Methods();

            findPreference("language_key").setOnPreferenceChangeListener(listener);
            darkModeSwitch = (SwitchPreference) findPreference("enable_dark_mode");
            changePassword = findPreference("change_password");
            changePassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    openDialog().show();
                    return true;
                }
            });

            changeTheme = findPreference("change_theme");
            changeTheme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).edit();
                    showColorPicker((SettingsActivity)preference.getContext(), editor);
                    return true;
                }
            });

            darkModeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isEnabled = (Boolean) newValue;
                    if(isEnabled){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    ((AppCompatActivity)getActivity()).getDelegate().applyDayNight();
                    getActivity().recreate();
                    return true;
                }
            });
        }

        private Dialog openDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.change_password_dialog, null);
            builder.setView(view);
            Dialog dialog = builder.create();

            TextInputLayout old_password = view.findViewById(R.id.old_password);
            TextInputLayout new_password = view.findViewById(R.id.new_password);
            TextInputLayout repeat_password = view.findViewById(R.id.repeat_password);
            Button cancel_btn = view.findViewById(R.id.cancel_btn);
            Button okBtn = view.findViewById(R.id.okBtn);

            cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newPassword = new_password.getEditText().getText().toString();
                    String oldPassword = old_password.getEditText().getText().toString();
                    if(FieldValidation.validatePassword(old_password) & FieldValidation.validatePassword(new_password) &
                            FieldValidation.validateRepeatPassword(new_password, repeat_password)){
                        changePassword(newPassword, oldPassword, dialog);
                    }
                }
            });

            return dialog;
        }

        private void showColorPicker(Activity activity, SharedPreferences.Editor editor){
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    methods.setColorTheme(color);
                    editor.putInt("color", color);
                    editor.putInt("theme", Constant.theme);
                    editor.apply();

                    Intent intent = new Intent(activity, CallsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                @Override
                public void onCancel() {

                }
            });
            colorPicker.show();
        }

        private void changePassword(String new_password, String old_password, Dialog dialog){
            Log.d("NEW_PASSWORD", new_password);
            Log.d("OLD_PASSWORD", old_password);

            String email = user.getEmail();
            String currentUserId = user.getUid();
            AuthCredential authCredential = EmailAuthProvider.getCredential(email, old_password);
            user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        user.updatePassword(new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    databaseReference.child(currentUserId).child("password").setValue(new_password);
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                }
            });
        }

    }

    private static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(preference instanceof ListPreference) {
                String stringValue = newValue.toString();
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                if (index == 0) {
                    setLocale("en", preference);
                } else if (index == 1) {
                    setLocale("sr", preference);
                } else {
                    setLocale("de", preference);
                }
            }
            return true;

        }

        private void setLocale(String lang, Preference preference){
            Locale myLocale = new Locale(lang);
            Resources res = preference.getContext().getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            getSetting(preference);
        }

        private void getSetting(final Preference preference){
            preference.getContext().startActivity(new Intent(preference.getContext(), CallsActivity.class));
        }

    };

}
