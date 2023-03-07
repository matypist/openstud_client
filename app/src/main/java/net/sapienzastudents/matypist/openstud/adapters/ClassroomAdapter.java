package net.sapienzastudents.matypist.openstud.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.ItemTouchHelperViewHolder;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.models.Classroom;

public class ClassroomAdapter extends RecyclerView.Adapter<ClassroomAdapter.ClassesHolder> {

    private List<Classroom> classes;
    private Context context;
    private View view;
    private ClassroomAdapterListener listener;

    public ClassroomAdapter(View view, Context context, List<Classroom> classes, ClassroomAdapterListener listener) {
        this.classes = classes;
        this.context = context;
        this.view = view;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_classroom_details, parent, false);
        ClassesHolder holder = new ClassesHolder(view);
        holder.setContext(context);
        holder.setListener(listener);
        holder.setView(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ClassesHolder holder, int position) {
        Classroom room = classes.get(position);
        holder.setDetails(room);
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    public interface ClassroomAdapterListener {
        void openTimetable(Classroom room);
    }

    static class ClassesHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        @BindView(R.id.className)
        TextView txtName;
        @BindView(R.id.whereClassroom)
        TextView txtWhere;
        @BindView(R.id.statusClassroom)
        TextView txtStatus;
        @BindView(R.id.lesson)
        TextView txtLesson;
        @BindView(R.id.nextLesson)
        TextView txtNextLesson;
        @BindView(R.id.open_map)
        Button openMap;
        @BindView(R.id.open_timetable)
        Button openTimetable;
        private Context context;
        private View view;
        private ClassroomAdapterListener listener;

        ClassesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setListener(ClassroomAdapterListener listener) {
            this.listener = listener;
        }

        private void setContext(Context context) {
            this.context = context;
        }

        private void setView(View view) {
            this.view = view;
        }

        void setDetails(Classroom room) {
            txtName.setText(room.getName());
            txtWhere.setText(context.getResources().getString(R.string.position, room.getWhere()));
            String status;
            int tintColor;
            if (room.isOccupied()) {
                tintColor = LayoutHelper.getColorByAttr(context, R.attr.nonCertifiedExamColor, R.color.red);
                status = context.getResources().getString(R.string.not_available);
            } else {
                tintColor = LayoutHelper.getColorByAttr(context, R.attr.certifiedExamColor, R.color.green);
                status = context.getResources().getString(R.string.available);
            }

            String statusPre = context.getResources().getString(R.string.status_classroom);
            Spannable spannable = new SpannableString(statusPre + status);
            spannable.setSpan(new ForegroundColorSpan(tintColor), statusPre.length(), (statusPre + status).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), statusPre.length(), (statusPre + status).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtStatus.setText(spannable, TextView.BufferType.SPANNABLE);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
            if (room.getLessonNow() != null) {
                txtLesson.setText(context.getResources().getString(R.string.lesson_now, room.getLessonNow().getName()));
                txtLesson.setVisibility(View.VISIBLE);
            } else txtLesson.setVisibility(View.GONE);
            if (room.getNextLesson() != null) {
                txtNextLesson.setText(context.getResources().getString(R.string.next_lesson, room.getNextLesson().getStart().format(formatter), room.getNextLesson().getName()));
                txtNextLesson.setVisibility(View.VISIBLE);
            } else txtNextLesson.setVisibility(View.GONE);

            Drawable mapDrawable;
            if (!room.hasCoordinates()) {
                mapDrawable = LayoutHelper.getDrawableWithColorId(context, R.drawable.ic_map_black_24dp, android.R.color.darker_gray);
                openMap.setEnabled(false);
            } else
                mapDrawable = LayoutHelper.getDrawableWithColorAttr(context, R.drawable.ic_map_black_24dp, R.attr.colorButtonNav, R.color.redSapienza);
            openMap.setCompoundDrawablesWithIntrinsicBounds(mapDrawable, null, null, null);
            openTimetable.setCompoundDrawablesWithIntrinsicBounds(LayoutHelper.getDrawableWithColorAttr(context, R.drawable.ic_event_black_24dp, R.attr.colorButtonNav, android.R.color.darker_gray), null, null, null);
            openMap.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + room.getLatitude() + "," + room.getLongitude() + "(" + room.getName() + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    LayoutHelper.createTextSnackBar(view, R.string.no_map_app, Snackbar.LENGTH_LONG);
                }
            });
            openTimetable.setOnClickListener(v -> listener.openTimetable(room));
        }

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
        }
    }
}