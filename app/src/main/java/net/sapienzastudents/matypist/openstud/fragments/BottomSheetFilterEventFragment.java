package net.sapienzastudents.matypist.openstud.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.widget.CompoundButtonCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.activities.CalendarActivity;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BottomSheetFilterEventFragment extends BottomSheetDialogFragment {
    @BindView(R.id.list)
    LinearLayout linearLayout;
    private boolean refreshNeeded = false;
    private List<String> elements = new LinkedList<>();

    public BottomSheetFilterEventFragment() {
        // Required empty public constructor
    }

    public static BottomSheetFilterEventFragment newInstance(List<String> names) {
        BottomSheetFilterEventFragment myFragment = new BottomSheetFilterEventFragment();
        Bundle args = new Bundle();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        String json = gson.toJson(names, listType);
        args.putSerializable("filter_elements", json);
        myFragment.setArguments(args);
        return myFragment;
    }

    @OnClick(R.id.close)
    void onClick() {
        dismiss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bdl = getArguments();
        if (bdl != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            String json = bdl.getString("filter_elements", null);
            if (json != null) {
                List<String> passedElements = gson.fromJson(json, listType);
                elements.clear();
                elements.addAll(passedElements);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.filter_calendar, container, false);
        ButterKnife.bind(this, v);
        ClientHelper.setDialogView(v, getDialog(), BottomSheetBehavior.STATE_EXPANDED);
        Context context = getContext();
        Activity activity = getActivity();
        InfoManager.removeOldEntriesFilter(context, new LinkedList<>(elements));
        if (context == null || activity == null) return null;
        int i = 0;
        for (String name : elements) {
            CheckBox ckb = new CheckBox(context);
            ckb.setId(i++);
            ckb.setText(name);
            if (!ThemeEngine.isLightTheme(context)) {
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_checked}, // unchecked
                                new int[]{android.R.attr.state_checked}, // checked
                        },
                        new int[]{
                                context.getColor(android.R.color.darker_gray),
                                context.getColor(R.color.redLight),
                        }
                );
                CompoundButtonCompat.setButtonTintList(ckb, colorStateList);
            }
            ckb.setTextColor(ThemeEngine.getPrimaryTextColor(activity));
            ckb.setPadding(0, 0, 0, 10);
            ckb.setChecked(!InfoManager.filterContains(context, name));
            linearLayout.addView(ckb);
            ckb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                refreshNeeded = true;
                if (isChecked) InfoManager.removeExceptionFromFilter(context, name);
                else InfoManager.addExceptionToFilter(context, name);
            });
        }
        return null;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        CalendarActivity activity = (CalendarActivity) getActivity();
        if (activity == null) return;
        activity.refreshAfterDismiss = refreshNeeded;
        activity.onDismiss(dialog);
    }


}