package net.sapienzastudents.matypist.openstud.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.adapters.EventAdapter;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import matypist.openstud.driver.core.OpenstudHelper;
import matypist.openstud.driver.core.models.Event;
import matypist.openstud.driver.core.models.ExamReservation;
import matypist.openstud.driver.core.models.Lesson;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class ClassroomTimetableActivity extends BaseDataActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView rv;
    @BindView(R.id.empty_layout)
    LinearLayout emptyView;
    @BindView(R.id.empty_button_reload)
    Button emptyButton;
    @BindView(R.id.empty_text)
    TextView emptyText;
    @BindView(R.id.constraintLayout)
    ConstraintLayout constraintLayout;
    private HorizontalCalendar horizontalCalendar;
    private Calendar defaultDate;
    private List<Event> lessons;
    private Map<Long, List<Lesson>> cachedLessons;
    private EventAdapter adapter;
    private int roomId;
    private ClassroomTimetableHandler h = new ClassroomTimetableHandler(this);

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        ThemeEngine.applyClassroomTimetableTheme(this);
        setContentView(R.layout.activity_classroom_timetable);
        ButterKnife.bind(this);
        Activity activity = this;
        Bundle bundle = this.getIntent().getExtras();
        roomId = bundle.getInt("roomId", -1);
        cachedLessons = new HashMap<>();
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH, -2);
        /* ends after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);
        restoreInstance(savedInstanceState);
        horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .defaultSelectedDate(defaultDate)
                .build();
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        String name = bundle.getString("roomName", null);
        lessons = new LinkedList<>();
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        adapter = new EventAdapter(this, lessons, new EventAdapter.EventAdapterListener() {
            @Override
            public void addCalendarOnClick(Event ev) {
                ClientHelper.addEventToCalendar(activity, ev);
            }

            @Override
            public void placeReservation(Event ev, ExamReservation res) {

            }

            @Override
            public void deleteReservation(Event ev, ExamReservation res) {

            }
        });
        rv.setAdapter(adapter);

        if (name == null)
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.classroom_timetable);
        else Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        if (savedInstanceState == null) setTodayLessonFromBundle(bundle);
        else getLessons(defaultDate, false);
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                getLessons(date, false);
            }
        });
        int refreshColorId = ThemeEngine.getSpinnerColorId(this);
        swipeRefreshLayout.setColorSchemeResources(refreshColorId, refreshColorId, refreshColorId);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeEngine.resolveColorFromAttribute(this, R.attr.SwipeSpinnerBackgroundColor, R.color.white));
        swipeRefreshLayout.setOnRefreshListener(() -> getLessons(horizontalCalendar.getSelectedDate(), true));
        emptyButton.setOnClickListener(v -> {
            if (!swipeRefreshLayout.isRefreshing())
                getLessons(horizontalCalendar.getSelectedDate(), true);
        });
        emptyText.setText(getResources().getString(R.string.no_lesson_found));
    }

    private void getLessons(Calendar date, boolean refresh) {
        new Thread(() -> {
            try {
                if (!refresh && cachedLessons.containsKey(date.getTimeInMillis()))
                    applyLessons(date, cachedLessons.get(date.getTimeInMillis()));
                else {
                    setRefreshing(true);
                    List<Lesson> newLessons = os.getClassroomTimetable(roomId, Instant.ofEpochMilli(date.getTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
                    applyLessons(date, newLessons);
                }
            } catch (OpenstudConnectionException e) {
                e.printStackTrace();
                h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR.getValue());
            } catch (OpenstudInvalidResponseException e) {
                e.printStackTrace();
                h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
            }
            setRefreshing(false);
        }).start();
    }

    private synchronized void applyLessons(Calendar date, List<Lesson> lessonsUpdate) {
        List<Event> newEvents = OpenstudHelper.generateEventsFromTimetable(lessonsUpdate);
        if (!cachedLessons.containsKey(date.getTimeInMillis()))
            cachedLessons.put(date.getTimeInMillis(), lessonsUpdate);
        lessons.clear();
        lessons.addAll(newEvents);
        runOnUiThread(() -> adapter.notifyDataSetChanged());
        swapViews(newEvents.isEmpty());
    }

    private void setRefreshing(final boolean bool) {
        this.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(bool));
    }

    private void swapViews(boolean empty) {
        runOnUiThread(() -> {
            if (empty) {
                emptyView.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setTodayLessonFromBundle(Bundle bundle) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        String lessonsJson = bundle.getString("todayLessons", null);
        if (lessonsJson == null) return;
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Lesson>>() {
        }.getType();
        List<Lesson> newLessons = gson.fromJson(lessonsJson, listType);
        applyLessons(today, newLessons);
        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    public void onSaveInstanceState(Bundle savedInstance) {

        Gson gson = new Gson();
        Type typeMap = new TypeToken<Map<Long, List<Lesson>>>() {
        }.getType();
        Type typeCalendar = new TypeToken<Calendar>() {
        }.getType();
        String jsonMap = gson.toJson(cachedLessons, typeMap);
        String jsonCalendar = gson.toJson(horizontalCalendar.getSelectedDate(), typeCalendar);
        savedInstance.putString("cachedLessons", jsonMap);
        savedInstance.putString("currentDate", jsonCalendar);
        super.onSaveInstanceState(savedInstance);
    }

    @SuppressLint("UseSparseArrays")
    private void restoreInstance(Bundle savedInstance) {
        defaultDate = Calendar.getInstance();
        defaultDate.set(Calendar.HOUR_OF_DAY, 0);
        defaultDate.set(Calendar.MINUTE, 0);
        defaultDate.set(Calendar.SECOND, 0);
        defaultDate.set(Calendar.MILLISECOND, 0);
        if (savedInstance != null) {
            String jsonDate = savedInstance.getString("currentDate", null);
            Gson gson = new Gson();
            Type typeCalendar = new TypeToken<Calendar>() {
            }.getType();
            if (jsonDate != null) defaultDate = gson.fromJson(jsonDate, typeCalendar);
            String json = savedInstance.getString("cachedLessons", null);
            if (json == null) cachedLessons = new HashMap<>();
            else {
                Type type = new TypeToken<Map<Long, List<Lesson>>>() {
                }.getType();

                cachedLessons = gson.fromJson(json, type);
            }
        }
    }

    private static class ClassroomTimetableHandler extends Handler {
        private final WeakReference<ClassroomTimetableActivity> activity;

        private ClassroomTimetableHandler(ClassroomTimetableActivity activity) {
            super(Looper.getMainLooper());
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ClassroomTimetableActivity activity = this.activity.get();
            if (activity == null) return;
            View.OnClickListener ocl = v -> activity.getLessons(activity.horizontalCalendar.getSelectedDate(), true);
            if (msg.what == ClientHelper.Status.CONNECTION_ERROR.getValue()) {
                LayoutHelper.createActionSnackBar(activity.constraintLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, ocl);
            } else if (msg.what == ClientHelper.Status.INVALID_RESPONSE.getValue()) {
                LayoutHelper.createActionSnackBar(activity.constraintLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, ocl);
            }
        }
    }

}
