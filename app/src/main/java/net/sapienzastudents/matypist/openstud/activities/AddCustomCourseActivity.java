package net.sapienzastudents.matypist.openstud.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.adapters.CustomLessonAdapter;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.data.CustomCourse;
import net.sapienzastudents.matypist.openstud.data.CustomLesson;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddCustomCourseActivity extends AppCompatActivity {
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView rv;
    @BindView(R.id.lessonName)
    AppCompatEditText titleTxt;
    @BindView(R.id.teacherName)
    AppCompatEditText teacherTxt;
    @BindView(R.id.startCourseLayout)
    ConstraintLayout startCourseLayout;
    @BindView(R.id.endCourseLayout)
    ConstraintLayout endCourseLayout;
    @BindView(R.id.startCourseTxt)
    TextView startCourseTxt;
    @BindView(R.id.endCourseTxt)
    TextView endCourseTxt;
    @BindView(R.id.addLesson)
    Button addLesson;
    private CustomLessonAdapter adapter;
    private LocalDate startCourse;
    private LocalDate endCourse;
    private List<CustomLesson> lessons;
    private List<CustomCourse> courses;
    private int position = -1;
    private boolean end;

    @OnClick(R.id.addLesson)
    void onClick() {
        addNewLesson();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeEngine.applyCustomCourseTheme(this);
        setContentView(R.layout.activity_add_custom_course);
        ButterKnife.bind(this);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_course);
        addLesson.setCompoundDrawablesWithIntrinsicBounds(LayoutHelper.getDrawableWithColorAttr(this, R.drawable.ic_add_black_24dp, R.attr.colorButtonNav, android.R.color.darker_gray), null, null, null);
        lessons = new LinkedList<>();
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        adapter = new CustomLessonAdapter(this, lessons, this::removeLesson);
        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        loadValues();
        setListeners();
    }


    private void loadValues() {
        Intent intent = getIntent();
        String jsonList = intent.getStringExtra("list");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CustomCourse>>() {
        }.getType();
        courses = gson.fromJson(jsonList, listType);
        position = intent.getIntExtra("position", -1);
        if (position != -1) {
            CustomCourse course = courses.get(position);
            startCourse = course.getStartCourse();
            endCourse = course.getEndCourse();
            teacherTxt.setText(course.getTeacher());
            titleTxt.setText(course.getTitle());
            if (course.getLessons() != null) {
                lessons.addAll(course.getLessons());
                adapter.notifyDataSetChanged();
            }
        } else {
            startCourse = LocalDate.now();
            endCourse = LocalDate.now().plusMonths(3);
            addNewLesson();
            adapter.notifyDataSetChanged();
        }
        startCourseTxt.setText(startCourse.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        endCourseTxt.setText(endCourse.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void setListeners() {
        startCourseLayout.setOnClickListener(v -> {
            createDatePickerDialog((view, year, month, dayOfMonth) -> {
                startCourse = LocalDate.of(year, month + 1, dayOfMonth);
                startCourseTxt.setText(startCourse.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                if (startCourse.isAfter(endCourse)) {
                    endCourse = startCourse;
                    endCourseTxt.setText(endCourse.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }, startCourse).show();
        });
        endCourseLayout.setOnClickListener(v -> createDatePickerDialog((view, year, month, dayOfMonth) -> {
            endCourse = LocalDate.of(year, month + 1, dayOfMonth);
            endCourseTxt.setText(endCourse.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            if (endCourse.isBefore(startCourse)) {
                startCourse = endCourse;
                startCourseTxt.setText(startCourse.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }, endCourse).show());
    }

    private DatePickerDialog createDatePickerDialog(DatePickerDialog.OnDateSetListener listener, LocalDate startDate) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog;
        if (startDate != null)
            dialog = new DatePickerDialog(AddCustomCourseActivity.this, ThemeEngine.getDatePickerTheme(this), listener, startDate.getYear(), startDate.getMonthValue() - 1, startDate.getDayOfMonth());
        else
            dialog = new DatePickerDialog(AddCustomCourseActivity.this, ThemeEngine.getDatePickerTheme(this), listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 1);
        maxDate.add(Calendar.MONTH, 6);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        return dialog;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_confirm, menu);
        Drawable drawable = menu.findItem(R.id.confirm).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.confirm).setIcon(drawable);
        menu.findItem(R.id.confirm).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.confirm:
                if (end) return false;
                if (lessons.isEmpty()) {
                    LayoutHelper.createTextSnackBar(mainLayout, R.string.add_one_lesson_day, Snackbar.LENGTH_LONG);
                    return false;
                }
                if (TextUtils.getTrimmedLength(teacherTxt.getText()) == 0 || TextUtils.getTrimmedLength(titleTxt.getText()) == 0 || areClassroomsEmpty()) {
                    LayoutHelper.createTextSnackBar(mainLayout, R.string.missing_parameters, Snackbar.LENGTH_LONG);
                    return false;
                }
                CustomCourse course;
                if (position == -1) course = new CustomCourse();
                else course = courses.get(position);
                course.setTeacher(Objects.requireNonNull(teacherTxt.getText()).toString().trim());
                course.setTitle(Objects.requireNonNull(titleTxt.getText()).toString().trim());
                course.setStartCourse(startCourse);
                course.setEndCourse(endCourse);
                course.setLessons(lessons);
                if (position == -1) courses.add(course);
                else {
                    courses.remove(position);
                    courses.add(position, course);
                }
                PreferenceManager.setCustomCourses(this, courses);
                end = true;
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private synchronized void addNewLesson() {
        CustomLesson lesson = new CustomLesson();
        lesson.setDayOfWeek(DayOfWeek.MONDAY);
        lesson.setStart(LocalTime.of(8, 0));
        lesson.setEnd(LocalTime.of(10, 0));
        lessons.add(lesson);
        adapter.notifyDataSetChanged();
    }

    private synchronized void removeLesson(CustomLesson lesson, int position) {
        lessons.remove(lesson);
        adapter.notifyItemRemoved(position);
        new Handler(Looper.getMainLooper()).postDelayed(() -> adapter.notifyDataSetChanged(), 250);
    }


    private synchronized boolean areClassroomsEmpty() {
        for (CustomLesson lesson : lessons) {
            if (lesson.getWhere() == null || lesson.getWhere().trim().isEmpty()) return true;
        }
        return false;
    }
}
