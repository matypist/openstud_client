package net.sapienzastudents.matypist.openstud.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;

import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.models.ExamDone;

public class ExamDoneAdapter extends RecyclerView.Adapter<ExamDoneAdapter.ExamDoneHolder> {

    private List<ExamDone> exams;
    private Context context;
    private int mode;

    public ExamDoneAdapter(Context context, List<ExamDone> exams, int mode) {
        this.exams = exams;
        this.context = context;
        this.mode = mode;
    }

    @NonNull
    @Override
    public ExamDoneHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_exam_completed, parent, false);
        ExamDoneHolder holder = new ExamDoneHolder(view);
        holder.setContext(context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ExamDoneHolder holder, int position) {
        ExamDone exam = exams.get(position);
        holder.setDetails(exam);
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }

    static class ExamDoneHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.examName)
        TextView txtName;
        @BindView(R.id.ssdExam)
        TextView txtSSD;
        @BindView(R.id.cfuExam)
        TextView txtCFU;
        @BindView(R.id.resultExam)
        TextView txtResult;
        @BindView(R.id.examDate)
        TextView txtExamDate;
        private int mode;
        private Context context;

        ExamDoneHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setMode(int mode) {
            this.mode = mode;
        }

        private void setContext(Context context) {
            this.context = context;
        }

        void setDetails(ExamDone exam) {
            txtName.setText(exam.getDescription());
            String result = exam.getNominalResult();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            if (result.equals("30 e lode")) txtResult.setText("30L / 30");
            else txtResult.setText(StringUtils.capitalize(result));
            if (exam.getResult() >= 18 || result.equals("idoneo") || exam.isPassed()) {
                if (exam.isCertified())
                    txtResult.setTextColor(LayoutHelper.getColorByAttr(context, R.attr.certifiedExamColor, R.color.green));
                else
                    txtResult.setTextColor(LayoutHelper.getColorByAttr(context, R.attr.nonCertifiedExamColor, R.color.yellow));
            } else
                txtResult.setTextColor(LayoutHelper.getColorByAttr(context, R.attr.nonPassedExamColor, R.color.red));
            txtCFU.setText(context.getResources().getString(R.string.cfu_exams, String.valueOf(exam.getCfu())));
            txtSSD.setText(context.getResources().getString(R.string.ssd_exams, exam.getSsd()));
            if (exam.getDate() != null && PreferenceManager.isExamDateEnabled(context)) {
                txtExamDate.setVisibility(View.VISIBLE);
                txtExamDate.setText(context.getResources().getString(R.string.exam_done_date, exam.getDate().format(formatter)));
            } else txtExamDate.setVisibility(View.GONE);
        }
    }
}
