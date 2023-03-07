package net.sapienzastudents.matypist.openstud.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.sapienzastudents.matypist.openstud.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.models.ExamDoable;

public class ExamDoableAdapter extends RecyclerView.Adapter<ExamDoableAdapter.ExamDoableHolder> {

    private List<ExamDoable> exams;
    private Context context;
    private View.OnClickListener edal;

    public ExamDoableAdapter(Context context, List<ExamDoable> exams, View.OnClickListener listener) {
        this.exams = exams;
        this.context = context;
        this.edal = listener;
    }

    @NonNull
    @Override
    public ExamDoableHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_exam_doable, parent, false);
        ExamDoableHolder holder = new ExamDoableHolder(view);
        holder.setContext(context);
        view.setOnClickListener(edal);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ExamDoableHolder holder, int position) {
        ExamDoable exam = exams.get(position);
        holder.setDetails(exam);
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }


    class ExamDoableHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.examName)
        TextView txtName;
        @BindView(R.id.ssdExam)
        TextView txtSSD;
        @BindView(R.id.cfuExam)
        TextView txtCFU;
        private Context context;

        ExamDoableHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setContext(Context context) {
            this.context = context;
        }

        void setDetails(final ExamDoable exam) {
            txtName.setText(exam.getDescription());
            txtCFU.setText(context.getResources().getString(R.string.cfu_exams, String.valueOf(exam.getCfu())));
            txtSSD.setText(context.getResources().getString(R.string.ssd_exams, exam.getSsd()));
        }
    }
}
