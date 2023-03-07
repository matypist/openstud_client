package net.sapienzastudents.matypist.openstud.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.data.CustomLesson;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomLessonAdapter extends RecyclerView.Adapter<CustomLessonAdapter.CustomLessonHolder> {

    private List<CustomLesson> lessons;
    private CustomLessonListener ocl;
    private Context context;

    public CustomLessonAdapter(Context context, List<CustomLesson> lessons, CustomLessonListener listener) {
        this.lessons = lessons;
        ocl = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomLessonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_custom_lesson_week_info, parent, false);
        CustomLessonHolder holder = new CustomLessonHolder(context, view, ocl);
        holder.setContext(context);
        return holder;
    }


    @Override
    public int getItemCount() {
        return lessons.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CustomLessonHolder holder, int position) {
        CustomLesson lesson = lessons.get(position);
        holder.setDetails(lesson);
    }


    public interface CustomLessonListener {
        void delete(CustomLesson lesson, int position);
    }

    static class CustomLessonHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.dayWeek)
        Spinner dayWeek;
        @BindView(R.id.where)
        AppCompatEditText where;
        @BindView(R.id.startLessonTime)
        TextView startLessonTime;
        @BindView(R.id.endLessonTime)
        TextView endLessonTime;

        @BindView(R.id.startLessonLayout)
        ConstraintLayout layoutStartTime;
        @BindView(R.id.endLessonLayout)
        ConstraintLayout layoutEndTime;

        @BindView(R.id.delete)
        ImageButton delete;
        private Context context;
        private CustomLessonListener ocl;

        CustomLessonHolder(Context context, View itemView, CustomLessonListener ocl) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.ocl = ocl;
            this.context = context;
        }

        private void setContext(Context context) {
            this.context = context;
        }

        void setDetails(CustomLesson lesson) {
            LayoutHelper.setColorSrcAtop(dayWeek.getBackground(),LayoutHelper.getColorByAttr(context, R.attr.primaryTextColor, android.R.color.darker_gray));
            dayWeek.setSelection(lesson.getDayOfWeek().getValue() - 1);
            startLessonTime.setText(String.format("%02d:%02d", lesson.getStart().getHour(), lesson.getStart().getMinute()));
            endLessonTime.setText(String.format("%02d:%02d", lesson.getEnd().getHour(), lesson.getEnd().getMinute()));
            where.setText(lesson.getWhere());
            delete.setOnClickListener(v -> ocl.delete(lesson, getAdapterPosition()));
            dayWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    lesson.setDayOfWeek(DayOfWeek.of(position + 1));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            where.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    lesson.setWhere(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            TimePickerDialog.OnTimeSetListener endSetListener = (view, hourOfDay, minute) -> {
                endLessonTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                lesson.setEnd(LocalTime.of(hourOfDay, minute, 0));
            };

            TimePickerDialog.OnTimeSetListener startSetListener = (view, hourOfDay, minute) -> {
                startLessonTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                lesson.setStart(LocalTime.of(hourOfDay, minute, 0));
                if (lesson.getStart().isAfter(lesson.getEnd()))
                    endSetListener.onTimeSet(null, hourOfDay + 1, minute);
            };

            layoutStartTime.setOnClickListener(v -> {
                TimePickerDialog dialog =
                        new TimePickerDialog(context, ThemeEngine.getTimePickerTheme(context), startSetListener, 0, 0, DateFormat.is24HourFormat(context));
                dialog.show();
            });
            layoutEndTime.setOnClickListener(v -> {
                TimePickerDialog dialog =
                        new TimePickerDialog(context, ThemeEngine.getTimePickerTheme(context), endSetListener, 0, 0, DateFormat.is24HourFormat(context));
                dialog.show();
            });
        }

    }

}
