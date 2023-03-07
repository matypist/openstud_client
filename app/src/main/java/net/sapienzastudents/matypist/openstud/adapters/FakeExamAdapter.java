package net.sapienzastudents.matypist.openstud.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.ItemTouchHelperViewHolder;
import net.sapienzastudents.matypist.openstud.activities.StatsActivity;
import net.sapienzastudents.matypist.openstud.helpers.ItemTouchHelperAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.models.ExamDone;

public class FakeExamAdapter extends RecyclerView.Adapter<FakeExamAdapter.ExamDoneHolder> implements ItemTouchHelperAdapter {

    private List<ExamDone> exams;
    private StatsActivity activity;
    private FakeExamListener ocl;

    public FakeExamAdapter(StatsActivity activity, List<ExamDone> exams, FakeExamListener listener) {
        this.exams = exams;
        this.activity = activity;
        ocl = listener;
    }

    @NonNull
    @Override
    public ExamDoneHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_row_exam_fake, parent, false);
        ExamDoneHolder holder = new ExamDoneHolder(view, ocl);
        holder.setContext(activity);
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

    @Override

    public void onItemDismiss(int position) {
        activity.removeFakeExam(position);
    }

    @Override

    public void onItemMove(int fromPosition, int toPosition) {
        ExamDone prev = exams.remove(fromPosition);
        exams.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);

    }

    public interface FakeExamListener {
        void deleteFakeExamAdapter(ExamDone exam, int position);
    }

    static class ExamDoneHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        @BindView(R.id.examName)
        TextView txtName;
        @BindView(R.id.cfuExam)
        TextView txtCFU;
        @BindView(R.id.resultExam)
        TextView txtResult;
        @BindView(R.id.delete)
        ImageButton delete;
        private Context context;
        private FakeExamListener ocl;

        ExamDoneHolder(View itemView, FakeExamListener ocl) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.ocl = ocl;
        }

        private void setContext(Context context) {
            this.context = context;
        }

        void setDetails(ExamDone exam) {
            txtName.setText(exam.getDescription());
            String result = String.valueOf(exam.getResult());
            String cfu = String.valueOf(exam.getCfu());
            txtResult.setText(context.getResources().getString(R.string.grade_stats, result));
            txtCFU.setText(context.getResources().getString(R.string.cfu_exams, cfu));
            delete.setOnClickListener(v -> ocl.deleteFakeExamAdapter(exam, getAdapterPosition()));
        }

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
        }
    }

}
