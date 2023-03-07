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
import net.sapienzastudents.matypist.openstud.data.CustomCourse;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomCourseAdapter extends RecyclerView.Adapter<CustomCourseAdapter.CustomCourseHolder> {

    private List<CustomCourse> lessons;
    private CustomCourseListener ccl;
    private Context context;

    public CustomCourseAdapter(Context context, List<CustomCourse> lessons, CustomCourseListener listener) {
        this.lessons = lessons;
        ccl = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomCourseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_custom_course, parent, false);
        CustomCourseHolder holder = new CustomCourseHolder(context, view, ccl);
        holder.setContext(context);
        view.setOnClickListener(v -> ccl.onClick(v));
        return holder;
    }


    @Override
    public int getItemCount() {
        return lessons.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CustomCourseHolder holder, int position) {
        CustomCourse course = lessons.get(position);
        holder.setDetails(course);
    }


    public interface CustomCourseListener {
        void onDeleteClick(CustomCourse course, int position);

        void onClick(View v);
    }

    static class CustomCourseHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.lessonName)
        TextView txtName;
        @BindView(R.id.nameTeacher)
        TextView txtTeacher;
        @BindView(R.id.delete)
        ImageButton delete;
        private Context context;
        private CustomCourseListener ccl;

        CustomCourseHolder(Context context, View itemView, CustomCourseListener ccl) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.ccl = ccl;
            this.context = context;
        }

        private void setContext(Context context) {
            this.context = context;
        }

        void setDetails(CustomCourse course) {
            txtName.setText(course.getTitle());
            txtTeacher.setText(context.getResources().getString(R.string.teacher_event, StringUtils.capitalize(course.getTeacher())));
            delete.setOnClickListener(v -> ccl.onDeleteClick(course, getAdapterPosition()));
        }

    }

}
