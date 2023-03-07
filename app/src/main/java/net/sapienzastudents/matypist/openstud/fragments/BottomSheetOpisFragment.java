package net.sapienzastudents.matypist.openstud.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.activities.ExamsActivity;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import matypist.openstud.driver.core.Openstud;
import matypist.openstud.driver.core.models.Student;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class BottomSheetOpisFragment extends BottomSheetDialogFragment {

    @BindView(R.id.surveyCode)
    EditText surveyCode;
    @BindView(R.id.confirm)
    Button confirm;
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    private Openstud os;

    public BottomSheetOpisFragment() {
        // Required empty public constructor
    }

    public static BottomSheetOpisFragment newInstance() {
        return new BottomSheetOpisFragment();
    }

    @OnClick(R.id.abort)
    public void hide() {
        dismiss();
    }

    @OnClick(R.id.confirm)
    public void add() {
        confirm.setEnabled(false);
        new Thread(() -> {
            ExamsActivity activity = (ExamsActivity) getActivity();
            boolean invalidTokenReceived = false;
            if (activity != null) {
                final Runnable notification = () -> Toasty.error(activity, R.string.connection_error).show();
                try {
                    String link = os.getCourseSurvey(surveyCode.getText().toString().trim());
                    if (link == null) {
                        invalidTokenReceived = true;
                        activity.runOnUiThread(() -> Toasty.error(activity, R.string.invalid_opis_code).show());
                    } else ClientHelper.createCustomTab(activity, link);
                } catch (OpenstudConnectionException | OpenstudInvalidResponseException e) {
                    e.printStackTrace();
                    activity.runOnUiThread(notification);
                } catch (OpenstudInvalidCredentialsException e) {
                    e.printStackTrace();
                    ClientHelper.rebirthApp(activity, ClientHelper.getStatusFromLoginException(e).getValue());
                } finally {
                    boolean finalInvalidTokenReceived = invalidTokenReceived;
                    activity.runOnUiThread(() -> {
                        confirm.setEnabled(true);
                        if (!finalInvalidTokenReceived) dismiss();
                    });

                }
            }
        }).start();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.opis_survey, container, false);
        ButterKnife.bind(this, v);
        ClientHelper.setDialogView(v, getDialog(), BottomSheetBehavior.STATE_EXPANDED);
        Context context = getContext();
        os = InfoManager.getOpenStud(context);
        Student student = InfoManager.getInfoStudentCached(context, os);
        if (context == null || student == null || os == null) {
            dismiss();
            return null;
        }


        int tintColorEnabled = LayoutHelper.getColorByAttr(context, R.attr.colorButtonNav, R.color.redSapienza);
        confirm.setEnabled(false);
        confirm.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        surveyCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    confirm.setEnabled(true);
                    confirm.setTextColor(tintColorEnabled);
                } else {
                    confirm.setEnabled(true);
                    confirm.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        surveyCode.setOnKeyListener((v1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                ClientHelper.hideKeyboard(v, context);
                return true;
            }
            return false;
        });
        return null;
    }

}