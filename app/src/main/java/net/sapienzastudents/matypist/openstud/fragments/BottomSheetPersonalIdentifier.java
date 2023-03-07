package net.sapienzastudents.matypist.openstud.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BottomSheetPersonalIdentifier extends BottomSheetDialogFragment {
    String identifier;
    @BindView(R.id.subtitle)
    TextView subtitle;
    @BindView(R.id.barcode)
    ImageView barcode;
    @BindView(R.id.close)
    Button confirm;

    private Float original_brightness = null;

    public BottomSheetPersonalIdentifier() {
        // Required empty public constructor
    }

    public static BottomSheetPersonalIdentifier newInstance(String identifier) {
        BottomSheetPersonalIdentifier myFragment = new BottomSheetPersonalIdentifier();
        Bundle args = new Bundle();
        if (identifier != null && !identifier.isEmpty()) args.putString("identifier", identifier);
        myFragment.setArguments(args);
        return myFragment;
    }

    @OnClick(R.id.close)
    public void hide() {
        dismiss();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bdl = getArguments();
        if (bdl != null) {
            identifier = bdl.getString("identifier", null);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cf_barcode, container, false);
        ButterKnife.bind(this, v);
        ClientHelper.setDialogView(v, getDialog(), BottomSheetBehavior.STATE_EXPANDED);
        Activity activity = getActivity();
        if (identifier == null || activity == null) dismiss();
        else {
            setBrightness(activity, 1F);
            subtitle.setText(identifier.toUpperCase());
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(identifier, BarcodeFormat.CODE_39, 1000, 200);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                barcode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }

        }
        return null;
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