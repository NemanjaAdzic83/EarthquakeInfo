package com.example.earthquakeinfo;

import android.os.Bundle;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.graphics.Color;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import com.example.earthquakeinfo.databinding.ActivitySettingsBinding;

/*
    Settings screen: the user can choose the minimum magnitude
    and whether earthquakes are ordered by magnitude or by time.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v,windowInsets) ->{
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left,bars.top,bars.right,0);
            binding.settingsContainer.setPadding(bars.left,0,bars.right,bars.bottom);
            return windowInsets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new EarthquakePreferenceFragment())
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class EarthquakePreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_main, rootKey);

            EditTextPreference minMagnitude = findPreference(getString(R.string.setting_min_magnitude_key));
            if (minMagnitude != null) {
                // Show the current value as the summary
                minMagnitude.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
                // Numeric keyboard (androidx ignores android:inputType in XML)
                minMagnitude.setOnBindEditTextListener(editText ->
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
                // Go back to the list as soon as a new value is chosen
                minMagnitude.setOnPreferenceChangeListener(this::saveAndClose);
            }

            ListPreference orderBy = findPreference(getString(R.string.settings_order_by_key));
            if (orderBy != null) {
                orderBy.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
                orderBy.setOnPreferenceChangeListener(this::saveAndClose);
            }
        }

        /*
            Called when the user picks a new value
            Returning true tells Android to save a value. then we close the Settings screen and EarthquakeActivity reloads the list in onStart()
            with the new setting
         */
        private boolean saveAndClose(Preference preference, Object newValue) {
            requireActivity().finish();
            return true;
        }
    }
}