package net.sapienzastudents.matypist.openstud.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.activities.StatsActivity;
import net.sapienzastudents.matypist.openstud.adapters.DropdownExamAdapter;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import com.warkiz.widget.IndicatorSeekBar;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import matypist.openstud.driver.core.OpenstudHelper;
import matypist.openstud.driver.core.models.Exam;
import matypist.openstud.driver.core.models.ExamDoable;
import matypist.openstud.driver.core.models.ExamDone;

public class BottomSheetStatsFragment extends BottomSheetDialogFragment {
    @BindView(R.id.exam_name)
    AutoCompleteTextView examName;
    @BindView(R.id.cfu)
    IndicatorSeekBar cfu;
    @BindView(R.id.grade)
    IndicatorSeekBar grade;
    @BindView(R.id.add)
    Button add;

    private List<ExamDoable> examsDoable = new LinkedList<>();

    public BottomSheetStatsFragment() {
        // Required empty public constructor
    }

    public static BottomSheetStatsFragment newInstance(List<ExamDoable> exams) {
        BottomSheetStatsFragment myFragment = new BottomSheetStatsFragment();
        if (exams != null) {
            Bundle args = new Bundle();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ExamDoable>>() {
            }.getType();
            try {
                args.putString("doable", gson.toJson(exams, listType));
                myFragment.setArguments(args);
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        }
        return myFragment;
    }

    @OnClick(R.id.abort)
    public void hide() {
        dismiss();
    }

    @OnClick(R.id.add)
    public void createExam() {
        StatsActivity activity = (StatsActivity) getActivity();
        if (activity != null) {
            activity.addFakeExam(OpenstudHelper.createFakeExamDone(examName.getText().toString().trim(), cfu.getProgress(), grade.getProgress()));
            dismiss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bdl = getArguments();
        if (bdl != null) {
            String examsJson = bdl.getString("doable", null);
            if (examsJson != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ExamDoable>>() {
                }.getType();
                try {
                    examsDoable.addAll(gson.fromJson(examsJson, listType));
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_exam_stats, container, false);
        ButterKnife.bind(this, v);
        ClientHelper.setDialogView(v, getDialog(), BottomSheetBehavior.STATE_EXPANDED);
        Context context = getContext();
        if (context == null) {
            dismiss();
            return null;
        }
        int tintColorEnabled = LayoutHelper.getColorByAttr(context, R.attr.colorButtonNav, R.color.redSapienza);
        add.setEnabled(false);
        add.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        DropdownExamAdapter adapter = new DropdownExamAdapter(context, (List<Exam>) (Object) examsDoable);
        filterExamsDoable(getContext());
        examName.setThreshold(2);
        examName.setAdapter(adapter);
        examName.setOnItemClickListener((parent, view, position, id) -> {
            Exam exam = adapter.getItem(position);
            if (exam != null) {
                examName.setText(exam.getDescription());
                cfu.setProgress(exam.getCfu());
            }
        });

        examName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.trim().length() == 0) {
                    add.setEnabled(false);
                    add.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                } else {
                    add.setEnabled(true);
                    add.setTextColor(tintColorEnabled);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        examName.setOnKeyListener((v1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                ClientHelper.hideKeyboard(v, context);
                return true;
            }
            return false;
        });
        return null;
    }

    private void filterExamsDoable(Context context) {
        List<ExamDone> fakeExams = InfoManager.getFakeExams(context, InfoManager.getOpenStud(context));
        if (fakeExams == null) return;
        List<ExamDoable> remove = new LinkedList<>();
        for (ExamDone fake : fakeExams) {
            for (ExamDoable doable : examsDoable) {
                if (doable.getDescription().toLowerCase().equals(fake.getDescription().toLowerCase())) {
                    remove.add(doable);
                    break;
                }
            }
        }
        examsDoable.removeAll(remove);
    }
}