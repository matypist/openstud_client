package net.sapienzastudents.matypist.openstud.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;

import org.threeten.bp.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import matypist.openstud.driver.core.Openstud;
import matypist.openstud.driver.core.models.Student;
import matypist.openstud.driver.core.models.StudentCard;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class BottomSheetStudentCard extends BottomSheetDialogFragment {
    @BindView(R.id.progress_layout)
    RelativeLayout progressLayout;
    @BindView(R.id.content_layout)
    ConstraintLayout contentLayout;
    @BindView(R.id.subtitle)
    TextView subtitle;
    @BindView(R.id.photo)
    ImageView photo;
    @BindView(R.id.barcode)
    ImageView barcode;
    @BindView(R.id.fullname)
    TextView fullName;
    @BindView(R.id.studentId)
    TextView studentId;
    @BindView(R.id.birthDate)
    TextView birthDate;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.close)
    Button confirm;
    private Student student;
    private Openstud os;
    private Float original_brightness = null;
    private StudentCard cachedCard;


    public BottomSheetStudentCard() {
        // Required empty public constructor
    }

    public static BottomSheetStudentCard newInstance() {
        return new BottomSheetStudentCard();
    }

    @OnClick(R.id.close)
    public void hide() {
        dismiss();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.student_card, container, false);
        ButterKnife.bind(this, v);
        ClientHelper.setDialogView(v, getDialog(), BottomSheetBehavior.STATE_EXPANDED);
        Activity activity = getActivity();
        if (activity == null) {
            dismiss();
            return null;
        }
        progressLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        os = InfoManager.getOpenStud(activity);
        student = InfoManager.getInfoStudentCached(activity, os);
        if (student == null) dismiss();
        else {
            cachedCard = InfoManager.getStudentCardCached(activity, os);
            if (cachedCard != null) applyCard(activity, cachedCard);
            getCard(activity);
        }
        return null;
    }

    private void getCard(Activity activity) {
        new Thread(() -> {
            try {
                StudentCard card = InfoManager.getStudentCard(activity, os, student);
                if (card != null && card.equals(cachedCard)) return;
                applyCard(activity, card);
            } catch (OpenstudConnectionException e) {
                e.printStackTrace();
                if (cachedCard == null) {
                    activity.runOnUiThread(() -> Toasty.error(activity, R.string.connection_error).show());
                    dismiss();
                } else {
                    activity.runOnUiThread(() -> Toasty.warning(activity, R.string.update_card_error).show());
                }
            } catch (OpenstudInvalidResponseException e) {
                e.printStackTrace();
                if (cachedCard == null) {
                    activity.runOnUiThread(() -> Toasty.error(activity, R.string.invalid_response_error).show());
                    dismiss();
                } else {
                    activity.runOnUiThread(() -> Toasty.warning(activity, R.string.update_card_error).show());
                }
            } catch (OpenstudInvalidCredentialsException e) {
                e.printStackTrace();
                ClientHelper.rebirthApp(activity, ClientHelper.getStatusFromLoginException(e).getValue());
                if (cachedCard == null) dismiss();
            }
        }).start();
    }

    private synchronized void applyCard(Activity activity, StudentCard card) {
        if (card == null) {
            activity.runOnUiThread(() -> {
                subtitle.setText(R.string.no_student_card_found);
                progressLayout.setVisibility(View.GONE);
            });
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        byte[] photoBytes = card.getImage();
        Bitmap bmpPhoto = null;
        if (photoBytes != null)
            bmpPhoto = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        Bitmap bmpBarcode = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(card.getCode(), BarcodeFormat.CODE_128, 1000, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bmpBarcode = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            dismiss();
        }
        if (bmpBarcode == null) dismiss();

        Bitmap finalBmpBarcode = bmpBarcode;
        Bitmap finalBmpPhoto = bmpPhoto;
        activity.runOnUiThread(() -> {
            barcode.setImageBitmap(finalBmpBarcode);
            subtitle.setVisibility(View.GONE);
            if (finalBmpPhoto != null) photo.setImageBitmap(finalBmpPhoto);
            fullName.setText(String.format("%s %s", student.getDisplayFirstName(), student.getDisplayLastName()));
            studentId.setText(activity.getResources().getString(R.string.studentId_combo, student.getStudentID()));
            birthDate.setText(activity.getResources().getString(R.string.birthDate_combo, formatter.format(student.getBirthDate())));
            progressLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
            setBrightness(activity, 1F);
        });
    }


    private void setBrightness(Activity activity, Float value) {
        Window window = activity.getWindow();
        if (window != null && value != null) {
            WindowManager.LayoutParams new_params = window.getAttributes();
            if (original_brightness == null) original_brightness = new_params.screenBrightness;
            new_params.screenBrightness = value;
            window.setAttributes(new_params);
        }
    }


    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity != null) setBrightness(activity, original_brightness);
    }

}