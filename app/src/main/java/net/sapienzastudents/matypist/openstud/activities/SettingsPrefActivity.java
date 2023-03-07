package net.sapienzastudents.matypist.openstud.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsPrefActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    androidx.appcompat.widget.Toolbar toolbar;
    @BindView(R.id.main_layout)
    ConstraintLayout mainLayout;
    BiometricPrompt pendingPrompt;
    PreferenceFragmentCompat prefFrag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeEngine.applySettingsTheme(this);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.settings);
        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState != null) prefFrag = (PreferenceFragmentCompat) fm.getFragment(savedInstanceState, "prefFrag");
        else prefFrag =  new MainPreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, prefFrag).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createRestartDialog(int styleId) {
        new AlertDialog.Builder(new ContextThemeWrapper(this, styleId))
                .setTitle(getResources().getString(R.string.restart_required))
                .setMessage(getResources().getString(R.string.restart_required_description))
                .setPositiveButton(getResources().getString(R.string.restart_ok), (dialog, which) -> {
                    Intent i = new Intent(SettingsPrefActivity.this, LauncherActivity.class);
                    startActivity(i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                })
                .setNegativeButton(getResources().getString(R.string.restart_cancel), (dialogInterface, i) -> {
                })
                .show();
    }

    private void createBiometricDialog(CheckBoxPreference preference) {
        ExecutorService exe = Executors.newSingleThreadExecutor();
        pendingPrompt = new BiometricPrompt(this, exe, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT)
                    LayoutHelper.createTextSnackBar(mainLayout, R.string.no_biometric_hw_found, Snackbar.LENGTH_LONG);
                else if (errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE)
                    LayoutHelper.createTextSnackBar(mainLayout, R.string.biometric_unavailable, Snackbar.LENGTH_LONG);
                else if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS)
                    LayoutHelper.createTextSnackBar(mainLayout, R.string.no_biometrics_found, Snackbar.LENGTH_LONG);
                else if (errorCode == BiometricPrompt.ERROR_LOCKOUT || errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT)
                    LayoutHelper.createTextSnackBar(mainLayout, R.string.biometric_lockout, Snackbar.LENGTH_LONG);
                runOnUiThread(() -> preference.setChecked(false));
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(() -> preference.setChecked(true));
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(this.getResources().getString(R.string.biometric_login_enable))
                .setNegativeButtonText(this.getResources().getString(R.string.delete_abort))
                .build();
        pendingPrompt.authenticate(promptInfo);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (pendingPrompt != null) pendingPrompt.cancelAuthentication();
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "prefFrag", prefFrag);
    }

    public static class MainPreferenceFragment extends PreferenceFragmentCompat {
        ThemeEngine.Theme oldTheme;
        private int alertDialogTheme;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SettingsPrefActivity activity = (SettingsPrefActivity) getActivity();
            if (activity == null) return;
            addPreferencesFromResource(R.xml.pref_main);
            alertDialogTheme = ThemeEngine.getAlertDialogTheme(activity);
            oldTheme = PreferenceManager.getTheme(activity);
            Preference theme = findPreference(getString(R.string.key_theme));
            theme.setOnPreferenceChangeListener((preference, o) -> {
                String newTheme = o.toString();
                Context context = getContext();
                if (context == null) return false;
                int id = Integer.parseInt(newTheme);
                if (ThemeEngine.Theme.getTheme(id) == oldTheme) return false;
                PreferenceManager.setTheme(context, ThemeEngine.Theme.getTheme(id));
                oldTheme = ThemeEngine.Theme.getTheme(id);
                //activity.createRestartDialog(alertDialogTheme);
                activity.recreate();
                return true;
            });
            Preference delete = findPreference(getString(R.string.key_delete));
            delete.setOnPreferenceClickListener(preference -> {
                String directory = activity.getExternalFilesDir("/OpenStud").getPath();
                File dir = new File(directory);
                try {
                    ClientHelper.deleteRecursive(dir);
                    LayoutHelper.createTextSnackBar(getView(), R.string.success_delete_pdf, Snackbar.LENGTH_LONG);
                } catch (Exception e) {
                    e.printStackTrace();
                    LayoutHelper.createTextSnackBar(getView(), R.string.failed_delete_pdf, Snackbar.LENGTH_LONG);
                }
                return true;
            });
            /* Preference enableLesson = findPreference(getString(R.string.key_enable_lesson));
            enableLesson.setOnPreferenceChangeListener((preference, newValue) -> {
                Boolean enable = (Boolean) newValue;
                if (enable) LayoutHelper.createCalendarNotification(activity, alertDialogTheme);
                PreferenceManager.setCalendarNotificationEnabled(getContext(), false);
                return true;
            }); */
            EditTextPreference laude = findPreference(getString(R.string.key_default_laude));
            laude.setOnBindEditTextListener(editText -> {
                editText.setTextColor(ThemeEngine.getPrimaryTextColor(activity));
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            });
            laude.setOnPreferenceChangeListener((preference, newValue) -> {
                String newLaude = (String) newValue;
                boolean valid = true;
                try {
                    int value = Integer.parseInt(newLaude);
                    if (value > 34 || value < 30) valid = false;
                } catch (NumberFormatException e) {
                    valid = false;
                }
                if (valid) {
                    PreferenceManager.setStatsNotificationEnabled(getContext(), false);
                    ClientHelper.updateGradesWidget(activity, true);
                } else {
                    LayoutHelper.createTextSnackBar(getView(), R.string.laude_value_set_error, Snackbar.LENGTH_LONG);
                }
                return valid;
            });
            CheckBoxPreference enableBiometricLogin = findPreference(getString(R.string.key_biometrics));
            enableBiometricLogin.setOnPreferenceClickListener(preference -> {
                if (enableBiometricLogin.isChecked()) enableBiometricLogin.setChecked(false);
                else activity.createBiometricDialog(enableBiometricLogin);
                return true;
            });
            enableBiometricLogin.setOnPreferenceChangeListener((preference, newValue) -> false);
            disableUnavailablePreferences();
            Preference addCustomLesson = findPreference(getString(R.string.key_add_custom_lesson));
            addCustomLesson.setOnPreferenceClickListener(preference -> {
                Intent i = new Intent(getActivity(), CustomCourseListActivity.class);
                startActivity(i);
                return true;
            });
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        }

        private void disableUnavailablePreferences() {
            if (!PreferenceManager.BIOMETRIC_FEATURE_AVAILABLE)
                findPreference(getString(R.string.key_security_category)).setVisible(false);
        }
    }

}