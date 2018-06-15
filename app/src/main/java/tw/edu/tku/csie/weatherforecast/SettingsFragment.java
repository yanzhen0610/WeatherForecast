/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw.edu.tku.csie.weatherforecast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import tw.edu.tku.csie.weatherforecast.data.WeatherAppContract;
import tw.edu.tku.csie.weatherforecast.sync.SyncUtils;
import tw.edu.tku.csie.weatherforecast.sync.UpdateCityNameByLatitudeAndLongitude;
import tw.edu.tku.csie.weatherforecast.sync.UpdateLocalizedAndEnglishCityName;
import tw.edu.tku.csie.weatherforecast.utilities.UpdateCurrentLocation;
import tw.edu.tku.csie.weatherforecast.utilities.PermissionUtils;

/**
 * The SettingsFragment serves as the display for all of the user's settings. In Sunshine, the
 * user will be able to change their preference for units of measurement from metric to imperial,
 * set their preferred weather location, and indicate whether or not they'd like to see
 * notifications.
 *
 * Please note: If you are using our dummy weather services, the location returned will always be
 * Mountain View, California.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private Activity mSettingActivity;
    private SharedPreferences mSharedPreferences;
    private EditTextPreference mEditTextLocationPreference;
    private CheckBoxPreference mCheckBoxUseCurrentLocationPreference;

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    private void updateCurrentLocation() {
        if (PermissionUtils.checkAccessFineLocationPermission(mSettingActivity)) {
            UpdateCurrentLocation.updateCurrentLocation(mSettingActivity);
//            mSettingActivity.startService(new Intent(mSettingActivity, UpdateCityNameByLatitudeAndLongitude.class));
            UpdateCityNameByLatitudeAndLongitude.startUpdate(mSettingActivity);
        }
    }

    private void updateEditTextLocationTextAndSummary() {
        mEditTextLocationPreference.setSummary(mSharedPreferences.getString(
                getString(R.string.pref_locale_location_key),
                mSharedPreferences.getString(getString(R.string.pref_user_input_location_key), "")));
        mEditTextLocationPreference.setText(mSharedPreferences.getString(
                getString(R.string.pref_locale_location_key),
                mSharedPreferences.getString(getString(R.string.pref_user_input_location_key), "")));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        mSettingActivity = getActivity();
        mEditTextLocationPreference =
                (EditTextPreference) findPreference(getString(R.string.pref_user_input_location_key));
        mCheckBoxUseCurrentLocationPreference =
                (CheckBoxPreference) findPreference(getString(R.string.pref_use_current_location_key));

        mSharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                String value = mSharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }

        mEditTextLocationPreference.setEnabled(!mSharedPreferences.getBoolean(
                getString(R.string.pref_use_current_location_key),
                getResources().getBoolean(R.bool.pref_use_current_location_by_default)));

        if (mSharedPreferences.getBoolean(getString(R.string.pref_use_current_location_key),
                getResources().getBoolean(R.bool.pref_use_current_location_by_default))) {
            mCheckBoxUseCurrentLocationPreference.setSummary(
                    mSharedPreferences.getString(getString(R.string.pref_current_city_key), ""));
        }

        updateEditTextLocationTextAndSummary();
        updateCurrentLocation();
        UpdateCityNameByLatitudeAndLongitude.startUpdate(mSettingActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_user_input_location_key))) {
            // we've changed the location
            // Wipe out any potential PlacePicker latlng values so that we can use this text entry.
//            WeatherAppPreferences.resetLocationCoordinates(mSettingActivity);
//            SyncUtils.startImmediateSync(mSettingActivity);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pref_en_location_key),
                    sharedPreferences.getString(key, getString(R.string.pref_location_default)));
            editor.putString(getString(R.string.pref_locale_location_key),
                    sharedPreferences.getString(key, getString(R.string.pref_location_default)));
            editor.apply();
            UpdateLocalizedAndEnglishCityName.startUpdate(mSettingActivity);
        } else if (key.equals(getString(R.string.pref_locale_location_key))) {
            updateEditTextLocationTextAndSummary();
        } else if (key.equals(getString(R.string.pref_units_key))) {
            // units have changed. update lists of weather entries accordingly
            mSettingActivity.getContentResolver().notifyChange(WeatherAppContract.WeatherEntry.CONTENT_URI, null);
        } else if (key.equals(getString(R.string.pref_use_current_location_key))) {

            boolean value = sharedPreferences.getBoolean(key,
                    getResources().getBoolean(R.bool.pref_use_current_location_by_default));

            if (value) {
                if (PermissionUtils.checkAccessFineLocationPermission(mSettingActivity)) {
                    UpdateCurrentLocation.updateCurrentLocation(mSettingActivity);
                    SyncUtils.startImmediateSync(mSettingActivity);
//                    mSettingActivity.startService(new Intent(mSettingActivity, UpdateCityNameByLatitudeAndLongitude.class));
                    UpdateCityNameByLatitudeAndLongitude.startUpdate(mSettingActivity);
                } else {
                    PermissionUtils.requestAccessFineLocationPermission(mSettingActivity);
                }
            } else {
                SyncUtils.startImmediateSync(mSettingActivity);
            }

            mEditTextLocationPreference.setEnabled(!value);
            mCheckBoxUseCurrentLocationPreference.setChecked(value);

        } else if (key.equals(getString(R.string.pref_current_city_key))) {

            mCheckBoxUseCurrentLocationPreference.setSummary(
                    sharedPreferences.getString(getResources().getString(R.string.pref_current_city_key), ""));

        }

        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }

}
