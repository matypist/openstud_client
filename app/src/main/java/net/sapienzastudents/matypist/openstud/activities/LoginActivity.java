package net.sapienzastudents.matypist.openstud.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.fragments.BottomSheetRecoveryFragment;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import matypist.openstud.driver.core.Openstud;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidAnswerException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;
import matypist.openstud.driver.exceptions.OpenstudUserNotEnabledException;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.studentIdLogin)
    EditText username;
    @BindView(R.id.passwordLogin)
    EditText password;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout layout;
    @BindView(R.id.button)
    Button btn;
    @BindView(R.id.recovery)
    Button recovery;
    @BindView(R.id.rememberFlag)
    CheckBox rememberFlag;
    private Openstud os;
    private LoginEventHandler h = new LoginEventHandler(this);

    @OnFocusChange({R.id.studentIdLogin, R.id.passwordLogin})
    void onFocusChanged(View v, boolean focused) {
        if (!focused) ClientHelper.hideKeyboard(v, getApplication());
    }

    @OnClick(R.id.recovery)
    void onClickRecovery(View v) {
        if (!ClientHelper.isNetworkAvailable(getApplication())) {
            LayoutHelper.createTextSnackBar(layout, R.string.device_no_internet, Snackbar.LENGTH_LONG);
            recovery.setEnabled(true);
            return;
        }
        recovery();
    }

    @OnClick(R.id.button)
    void onClickLogin(View v) {
        if (!ClientHelper.isNetworkAvailable(getApplication())) {
            LayoutHelper.createTextSnackBar(layout, R.string.device_no_internet, Snackbar.LENGTH_LONG);
            runOnUiThread(() -> setElementsEnabled(true));
            return;
        }
        login();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        analyzeExtras(getIntent().getExtras());
        ClientHelper.updateGradesWidget(this, false);
        ClientHelper.updateExamWidget(this, false);
        if (InfoManager.hasLogin(this)) {
            username.setText(InfoManager.getStudentId(this));
            rememberFlag.setChecked(true);
            if (savedInstanceState == null && PreferenceManager.isBiometricsEnabled(this)) {
                handleBiometrics();
            }
        }
    }

    public void sendRecoveryRequest(String answer, String studentID) {
        runOnUiThread(() -> setElementsEnabled(false));
        Openstud os = InfoManager.getOpenStudRecovery(this, studentID);
        if (os == null) return;
        try {
            os.recoverPassword(answer);
            h.sendEmptyMessage(ClientHelper.Status.RECOVERY_OK.getValue());
        } catch (OpenstudConnectionException e) {
            h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR.getValue());
            e.printStackTrace();
        } catch (OpenstudInvalidResponseException e) {
            if (e.isMaintenance()) h.sendEmptyMessage(ClientHelper.Status.MAINTENANCE.getValue());
            h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
            e.printStackTrace();
        } catch (OpenstudInvalidCredentialsException e) {
            h.sendEmptyMessage(ClientHelper.Status.INVALID_STUDENT_ID.getValue());
            e.printStackTrace();
        } catch (OpenstudInvalidAnswerException e) {
            h.sendEmptyMessage(ClientHelper.Status.INVALID_ANSWER.getValue());
            e.printStackTrace();
        }

    }

    private void recovery() {
        runOnUiThread(() -> setElementsEnabled(false));
        String username = this.username.getText().toString();
        if (username.isEmpty()) {
            LayoutHelper.createTextSnackBar(layout, R.string.blank_username_error, Snackbar.LENGTH_LONG);
            h.sendEmptyMessage(ClientHelper.Status.FAIL_LOGIN.getValue());
            return;
        }
        os = InfoManager.getOpenStudRecovery(getApplication(), username);
        new Thread(() -> _recovery(os, username)).start();
    }

    private void _recovery(Openstud os, String username) {
        if (os == null) {
            h.sendEmptyMessage(ClientHelper.Status.UNEXPECTED_VALUE.getValue());
            return;
        }
        try {
            String question = os.getSecurityQuestion();
            if (question == null) {
                h.sendEmptyMessage(ClientHelper.Status.NO_RECOVERY.getValue());
                return;
            }
            BottomSheetRecoveryFragment recoveryFrag = BottomSheetRecoveryFragment.newInstance(username, question);
            recoveryFrag.show(getSupportFragmentManager(), recoveryFrag.getTag());
            new Handler(Looper.getMainLooper()).postDelayed(() -> h.sendEmptyMessage(ClientHelper.Status.ENABLE_BUTTONS.getValue()), 1000);
        } catch (OpenstudConnectionException e) {
            h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR_RECOVERY.getValue());
            e.printStackTrace();
        } catch (OpenstudInvalidResponseException e) {
            if (e.isMaintenance()) h.sendEmptyMessage(ClientHelper.Status.MAINTENANCE.getValue());
            h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
            e.printStackTrace();
        } catch (OpenstudInvalidCredentialsException e) {
            h.sendEmptyMessage(ClientHelper.Status.INVALID_STUDENT_ID.getValue());
            e.printStackTrace();
        }
    }

    private void login() {
        runOnUiThread(() -> setElementsEnabled(false));
        String username = this.username.getText().toString();
        final String password = this.password.getText().toString();
        if (username.isEmpty()) {
            if (password.isEmpty())
                LayoutHelper.createTextSnackBar(layout, R.string.blank_username_password_error, Snackbar.LENGTH_LONG);
            else
                LayoutHelper.createTextSnackBar(layout, R.string.blank_username_error, Snackbar.LENGTH_LONG);
            h.sendEmptyMessage(ClientHelper.Status.FAIL_LOGIN.getValue());
            return;
        }
        if (password.isEmpty()) {
            LayoutHelper.createTextSnackBar(layout, R.string.blank_password_error, Snackbar.LENGTH_LONG);
            h.sendEmptyMessage(ClientHelper.Status.FAIL_LOGIN.getValue());
            return;
        }
        os = InfoManager.getOpenStud(getApplication(), username, password);
        new Thread(() -> _login(os, username, password, rememberFlag.isChecked())).start();
    }

    private synchronized void _login(Openstud os, String id, String password, boolean rememberFlag) {
        if (os == null) {
            h.sendEmptyMessage(ClientHelper.Status.UNEXPECTED_VALUE.getValue());
            return;
        }
        try {
            os.login();
            InfoManager.getInfoStudent(this, os);
            InfoManager.getIsee(this, os);
            if (!rememberFlag || (InfoManager.hasLogin(this) && !InfoManager.getStudentId(this).equals(id)))
                PreferenceManager.setBiometricsEnabled(this, false);
            InfoManager.saveOpenStud(this, os, id, password, rememberFlag);
            h.sendEmptyMessage(ClientHelper.Status.OK.getValue());
        } catch (OpenstudInvalidCredentialsException e) {
            ClientHelper.Status status = ClientHelper.getStatusFromLoginException(e);
            if (status == ClientHelper.Status.INVALID_CREDENTIALS) {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("maxAttempts", e.getMaxAttempts());
                data.putInt("attemptNumber", e.getAttemptNumber());
                msg.setData(data);
                msg.what = status.getValue();
                h.sendMessage(msg);
            } else h.sendEmptyMessage(status.getValue());
            e.printStackTrace();
        } catch (OpenstudUserNotEnabledException e) {
            h.sendEmptyMessage(ClientHelper.Status.USER_NOT_ENABLED.getValue());
            e.printStackTrace();
        } catch (OpenstudConnectionException e) {
            h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR.getValue());
            e.printStackTrace();
        } catch (OpenstudInvalidResponseException e) {
            if (e.isMaintenance()) h.sendEmptyMessage(ClientHelper.Status.MAINTENANCE.getValue());
            h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
            e.printStackTrace();
        }
    }

    private void analyzeExtras(Bundle bdl) {
        if (bdl == null) return;
        int error = bdl.getInt("error", -1);
        if (error == -1) return;
        else if (error == ClientHelper.Status.INVALID_CREDENTIALS.getValue())
            LayoutHelper.createTextSnackBar(layout, R.string.invalid_password_error, Snackbar.LENGTH_LONG);
        else if (error == ClientHelper.Status.ACCOUNT_BLOCKED.getValue())
            LayoutHelper.createTextSnackBar(layout, R.string.account_blocked_error, Snackbar.LENGTH_LONG);
        else if (error == ClientHelper.Status.EXPIRED_CREDENTIALS.getValue())
            LayoutHelper.createTextSnackBar(layout, R.string.expired_password_error, Snackbar.LENGTH_LONG);
        else if (error == ClientHelper.Status.LOCKOUT_BIOMETRICS.getValue())
            LayoutHelper.createTextSnackBar(layout, R.string.biometric_lockout, Snackbar.LENGTH_LONG);
    }

    private void setElementsEnabled(boolean enabled) {
        btn.setEnabled(enabled);
        recovery.setEnabled(enabled);
        rememberFlag.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
    }


    private void handleBiometrics() {
        ExecutorService exe = Executors.newSingleThreadExecutor();
        BiometricPrompt prompt = new BiometricPrompt(this, exe, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                    PreferenceManager.setBiometricsEnabled(LoginActivity.this, false);
                    h.sendEmptyMessage(ClientHelper.Status.NO_BIOMETRICS.getValue());
                } else if (errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT) {
                    PreferenceManager.setBiometricsEnabled(LoginActivity.this, false);
                    h.sendEmptyMessage(ClientHelper.Status.NO_BIOMETRIC_HW.getValue());
                } else if (errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT || errorCode == BiometricPrompt.ERROR_LOCKOUT)
                    h.sendEmptyMessage(ClientHelper.Status.LOCKOUT_BIOMETRICS.getValue());
                else if (errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE)
                    h.sendEmptyMessage(ClientHelper.Status.BIOMETRIC_UNAVAILABLE.getValue());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                h.sendEmptyMessage(ClientHelper.Status.OK.getValue());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(this.getResources().getString(R.string.biometric_login))
                .setNegativeButtonText(this.getResources().getString(R.string.delete_abort))
                .build();
        prompt.authenticate(promptInfo);
    }


    private static class LoginEventHandler extends Handler {
        private final WeakReference<LoginActivity> mActivity;

        LoginEventHandler(LoginActivity activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = mActivity.get();
            if (activity != null) {
                View.OnClickListener listener = v -> new Thread(activity::login).start();
                View.OnClickListener listener2 = v -> new Thread(activity::recovery).start();
                if (msg.what == ClientHelper.Status.OK.getValue()) {
                    if (activity.rememberFlag.isChecked()) {
                        ClientHelper.updateGradesWidget(activity, false);
                        ClientHelper.updateExamWidget(activity, false);
                    }
                    Intent intent = new Intent(activity, ExamsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                } else if (msg.what == ClientHelper.Status.CONNECTION_ERROR.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.layout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.CONNECTION_ERROR_RECOVERY.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.layout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, listener2);
                } else if (msg.what == ClientHelper.Status.INVALID_RESPONSE.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.invalid_response_error, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.MAINTENANCE.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.infostud_maintenance, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.USER_NOT_ENABLED.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.layout, R.string.user_not_enabled_error, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == (ClientHelper.Status.INVALID_CREDENTIALS).getValue()) {
                    Bundle data = msg.getData();
                    boolean fired = false;
                    if (msg.getData() != null) {
                        int maxAttempts = data.getInt("maxAttempts", -1);
                        int attemptNumber = data.getInt("attemptNumber", -1);
                        if (maxAttempts != -1 && attemptNumber != -1) {
                            fired = true;
                            int attemptsLeft = maxAttempts - attemptNumber;
                            if (attemptsLeft == 1)
                                LayoutHelper.createTextSnackBar(activity.layout, activity.getResources().getString(R.string.invalid_password_error_with_counter_singular, String.valueOf(attemptsLeft)), Snackbar.LENGTH_LONG);
                            else
                                LayoutHelper.createTextSnackBar(activity.layout, activity.getResources().getString(R.string.invalid_password_error_with_counter_plural, String.valueOf(attemptsLeft)), Snackbar.LENGTH_LONG);
                        }
                        if (!fired)
                            LayoutHelper.createTextSnackBar(activity.layout, R.string.invalid_password_error, Snackbar.LENGTH_LONG);
                    }
                } else if (msg.what == (ClientHelper.Status.EXPIRED_CREDENTIALS).getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.expired_password_error, Snackbar.LENGTH_LONG);
                } else if (msg.what == (ClientHelper.Status.ACCOUNT_BLOCKED).getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.account_blocked_error, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.UNEXPECTED_VALUE.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.invalid_response_error, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.RECOVERY_OK.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.recovery_ok, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.INVALID_STUDENT_ID.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.invalid_student_id, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.INVALID_ANSWER.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.invalid_answer, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.NO_RECOVERY.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.no_recovery, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.LOCKOUT_BIOMETRICS.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.biometric_lockout, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.BIOMETRIC_UNAVAILABLE.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.layout, R.string.biometric_unavailable, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.NO_BIOMETRICS.getValue() || msg.what == ClientHelper.Status.NO_BIOMETRIC_HW.getValue()) {
                    Intent intent = new Intent(activity, ExamsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("error", msg.what);
                    activity.startActivity(intent);
                }
                if (msg.what != ClientHelper.Status.OK.getValue()) {
                    activity.runOnUiThread(() -> activity.setElementsEnabled(true));
                }
            }
        }
    }


}
