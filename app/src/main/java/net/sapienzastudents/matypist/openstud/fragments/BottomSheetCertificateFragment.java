package net.sapienzastudents.matypist.openstud.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import matypist.openstud.driver.core.Openstud;
import matypist.openstud.driver.core.models.Career;
import matypist.openstud.driver.core.models.CertificateType;
import matypist.openstud.driver.core.models.Student;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class BottomSheetCertificateFragment extends BottomSheetDialogFragment {

    @BindView(R.id.registration)
    LinearLayout registration;
    @BindView(R.id.exams_taken)
    LinearLayout examsTaken;
    @BindView(R.id.degree_with_evaluation)
    LinearLayout degreeEvaluation;
    @BindView(R.id.degree_with_evaluation_eng)
    LinearLayout degreeEvaluationEng;
    @BindView(R.id.degree_with_exams)
    LinearLayout degreeExams;
    @BindView(R.id.degree_with_exams_eng)
    LinearLayout degreeExamsEng;
    @BindView(R.id.degree_ransom)
    LinearLayout degreeRansom;
    @BindView(R.id.degree_with_thesis)
    LinearLayout degreeThesis;
    @BindView(R.id.degree_with_thesis_eng)
    LinearLayout degreeThesisEng;
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    private Openstud os;
    private Student student;

    public BottomSheetCertificateFragment() {
        // Required empty public constructor
    }

    public static BottomSheetCertificateFragment newInstance() {
        return new BottomSheetCertificateFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.certificates_sheet, container, false);
        ButterKnife.bind(this, v);
        ClientHelper.setDialogView(v, getDialog(), BottomSheetBehavior.STATE_EXPANDED);
        Context context = getContext();
        os = InfoManager.getOpenStud(context);
        student = InfoManager.getInfoStudentCached(context, os);
        if (context == null || student == null || os == null) {
            dismiss();
            return null;
        }
        registration.setOnClickListener(v1 -> getCertificate(CertificateType.REGISTRATION));
        examsTaken.setOnClickListener(v1 -> getCertificate(CertificateType.EXAMS_COMPLETED));
        degreeEvaluation.setOnClickListener(v1 -> getCertificate(CertificateType.DEGREE_WITH_EVALUATION));
        degreeEvaluationEng.setOnClickListener(v1 -> getCertificate(CertificateType.DEGREE_WITH_EVALUATION_ENG));
        degreeThesis.setOnClickListener(v1 -> getCertificate(CertificateType.DEGREE_WITH_THESIS));
        degreeThesisEng.setOnClickListener(v1 -> getCertificate(CertificateType.DEGREE_WITH_THESIS_ENG));
        degreeExams.setOnClickListener(v1 -> getCertificate(CertificateType.DEGREE_WITH_EXAMS));
        degreeExamsEng.setOnClickListener(v1 -> getCertificate(CertificateType.DEGREE_WITH_EXAMS_ENG));
        degreeRansom.setOnClickListener(v1 -> getCertificate(CertificateType.DEGREE_FOR_RANSOM));
        return null;
    }


    private void setButtonsState(boolean enabled) {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            registration.setEnabled(enabled);
            examsTaken.setEnabled(enabled);
            degreeEvaluation.setEnabled(enabled);
            degreeEvaluationEng.setEnabled(enabled);
            degreeThesis.setEnabled(enabled);
            degreeThesisEng.setEnabled(enabled);
            degreeExams.setEnabled(enabled);
            degreeExamsEng.setEnabled(enabled);
            degreeRansom.setEnabled(enabled);
        });
    }

    private void getCertificate(CertificateType certificate) {
        Activity activity = getActivity();
        if (activity == null) return;
        new Thread(() -> {
            setButtonsState(false);
            selectCareer(activity, certificate);
        }).start();
    }


    private void selectCareer(Activity activity, CertificateType cert) {
        List<Career> careers = null;
        try {
            careers = os.getCareersChoicesForCertificate(student, cert);
        } catch (OpenstudConnectionException | OpenstudInvalidResponseException e) {
            activity.runOnUiThread(() -> Toasty.error(activity, R.string.failed_get_network).show());
            e.printStackTrace();
        } catch (OpenstudInvalidCredentialsException e) {
            InfoManager.clearSharedPreferences(activity);
            ClientHelper.rebirthApp(activity, ClientHelper.getStatusFromLoginException(e).getValue());
            e.printStackTrace();
        }
        if (careers == null) {
            setButtonsState(true);
            return;
        }

        if (careers.isEmpty()) {
            activity.runOnUiThread(() -> Toasty.warning(activity, R.string.no_certificate).show());
            setButtonsState(true);
        } else if (careers.size() == 1) getFile(activity, cert, careers.get(0));
        else createChoiceCareerDialog(activity, careers, cert);
    }

    private void createChoiceCareerDialog(Activity activity, List<Career> careers, CertificateType cert) {
        CharSequence[] items = new CharSequence[careers.size()];
        for (Career career : careers) {
            items[career.getIndex()] = career.getDescription();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, ThemeEngine.getAlertDialogTheme(activity)));
        builder.setTitle(activity.getString(R.string.select_careers));
        builder.setSingleChoiceItems(items, -1, (dialog, item) -> {
            dialog.dismiss();
            new Thread(() -> getFile(activity, cert, careers.get(item))).start();
        });
        activity.runOnUiThread(() -> {
            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    private void getFile(Activity activity, CertificateType cert, Career career) {
        boolean check = false;

        String directory = activity.getExternalFilesDir("/OpenStud/pdf/certs/").getPath();
        File dirs = new File(directory);
        if (!directory.endsWith("/")) directory = directory + "/";
        dirs.mkdirs();
        File pdfFile = new File(directory + cert.toString() + ".pdf");
        try {
            if (pdfFile.exists()) pdfFile.delete();
            pdfFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(pdfFile);
            activity.runOnUiThread(() -> Toasty.info(activity, R.string.downloading_certificate).show());
            byte[] content = os.getCertificatePDF(student, career, cert);
            fos.write(content);
            fos.close();
            check = true;
        } catch (IOException e) {
            activity.runOnUiThread(() -> Toasty.error(activity, R.string.storage_error).show());
            e.printStackTrace();
        } catch (OpenstudConnectionException | OpenstudInvalidResponseException e) {
            activity.runOnUiThread(() -> Toasty.error(activity, R.string.failed_get_network_certificate).show());
            e.printStackTrace();
        } catch (OpenstudInvalidCredentialsException e) {
            InfoManager.clearSharedPreferences(activity);
            if (e.isPasswordExpired())
                ClientHelper.rebirthApp(activity, ClientHelper.Status.EXPIRED_CREDENTIALS.getValue());
            else
                ClientHelper.rebirthApp(activity, ClientHelper.Status.INVALID_CREDENTIALS.getValue());
            e.printStackTrace();
        } finally {
            setButtonsState(true);
        }
        if (!check) {
            pdfFile.delete();
            return;
        }
        ClientHelper.openActionViewPDF(activity, pdfFile);
    }

}