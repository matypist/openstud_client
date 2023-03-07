package net.sapienzastudents.matypist.openstud.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.adapters.EventAdapter;
import net.sapienzastudents.matypist.openstud.fragments.BottomSheetFilterEventFragment;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import com.mikepenz.materialdrawer.Drawer;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.apache.commons.lang3.tuple.Pair;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import matypist.openstud.driver.core.models.EventType;
import matypist.openstud.driver.core.models.ExamReservation;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class CalendarActivity extends BaseDataActivity implements DialogInterface.OnDismissListener {


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
    public boolean refreshAfterDismiss = false;
    @BindView(R.id.main_layout)
    RelativeLayout mainLayout;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsingToolbarLayout)
    ExpandableLayout expandableLayout;
    @BindView(R.id.compactcalendar_view)
    CompactCalendarView compactCalendarView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.date_picker_arrow)
    ImageView arrow;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.lessons_rv)
    RecyclerView lessons_rv;
    @BindView(R.id.exams_doable_rv)
    RecyclerView exams_rv;
    @BindView(R.id.reservations_rv)
    RecyclerView reservations_rv;
    @BindView(R.id.empty_layout)
    LinearLayout emptyView;
    @BindView(R.id.empty_text)
    TextView emptyText;
    @BindView(R.id.empty_container)
    LinearLayout emptyContainer;
    @BindView(R.id.empty_button_reload)
    Button emptyButton;
    private boolean isExpanded = false;
    private CalendarEventHandler h = new CalendarEventHandler(this);
    private Drawer drawer;
    private EventAdapter adapter_lessons;
    private EventAdapter adapter_exams;
    private EventAdapter adapter_reservations;
    private List<matypist.openstud.driver.core.models.Event> events = new LinkedList<>();
    private List<matypist.openstud.driver.core.models.Event> lessons = new LinkedList<>();
    private List<matypist.openstud.driver.core.models.Event> exams = new LinkedList<>();
    private List<matypist.openstud.driver.core.models.Event> reservations = new LinkedList<>();
    private Date currentDate;
    private boolean lessonOptionsEnabled;
    private boolean lessonsEnabled;
    private boolean firstStart = true;
    private LocalDateTime lastUpdate;

    @OnClick(R.id.empty_button_reload)
    void onEmptyButton() {
        if (swipeRefreshLayout.isRefreshing()) return;
        refreshEvents();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        ThemeEngine.applyCalendarTheme(this);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);
        setTitle(getResources().getString(R.string.calendar));
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        drawer = LayoutHelper.applyDrawer(this, toolbar, student);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        lessonOptionsEnabled = PreferenceManager.isLessonOptionEnabled(this);
        // lessonsEnabled = PreferenceManager.isLessonEnabled(this);
        compactCalendarView.setLocale(TimeZone.getDefault(), Locale.getDefault());
        compactCalendarView.setShouldDrawDaysHeader(true);
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                setSubtitle(dateFormat.format(dateClicked));
                synchronized (this) {
                    currentDate = dateClicked;
                    getEventsByDate(events, dateClicked);
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                setSubtitle(dateFormat.format(firstDayOfNewMonth));
                synchronized (this) {
                    currentDate = firstDayOfNewMonth;
                    getEventsByDate(events, firstDayOfNewMonth);
                }
            }
        });
        setupRecyclerLayouts();
        List<matypist.openstud.driver.core.models.Event> events_cached = InfoManager.getEventsCached(this, os);

        if (events_cached != null && !events_cached.isEmpty()) events.addAll(events_cached);
        // Set current date to today
        if (savedInstanceState == null) currentDate = ClientHelper.getDateWithoutTime();
        else currentDate = (Date) savedInstanceState.getSerializable("currentDate");
        setCurrentDate(currentDate);
        updateCalendar(events);
        RelativeLayout datePickerButton = findViewById(R.id.date_picker_button);
        datePickerButton.setOnClickListener(v -> animateExpansion());
        emptyText.setText(getResources().getString(R.string.no_events));
        int refreshColorId = ThemeEngine.getSpinnerColorId(this);
        swipeRefreshLayout.setColorSchemeResources(refreshColorId, refreshColorId, refreshColorId);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeEngine.resolveColorFromAttribute(this, R.attr.SwipeSpinnerBackgroundColor, R.color.white));
        if (savedInstanceState == null) refreshEvents();
        swipeRefreshLayout.setOnRefreshListener(this::refreshEvents);
        if (expandableLayout.isExpanded()) {
            ViewCompat.animate(arrow).rotation(180).start();
        }

    }

    private void setupRecyclerLayouts() {

        lessons_rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        lessons_rv.setLayoutManager(llm);
        Activity activity = this;
        EventAdapter.EventAdapterListener eal = new EventAdapter.EventAdapterListener() {
            @Override
            public void addCalendarOnClick(matypist.openstud.driver.core.models.Event ev) {
                ClientHelper.addEventToCalendar(activity, ev);
            }

            @Override
            public void placeReservation(matypist.openstud.driver.core.models.Event ev, ExamReservation res) {
                confirmReservation(res);
            }

            @Override
            public void deleteReservation(matypist.openstud.driver.core.models.Event ev, ExamReservation res) {
                ClientHelper.createConfirmDeleteReservationDialog(activity, res, () -> CalendarActivity.this.deleteReservation(res));
            }
        };

        adapter_lessons = new EventAdapter(this, lessons, eal);
        lessons_rv.setAdapter(adapter_lessons);

        exams_rv.setHasFixedSize(true);
        LinearLayoutManager llm2 = new LinearLayoutManager(this);
        exams_rv.setLayoutManager(llm2);
        adapter_exams = new EventAdapter(this, exams, eal);
        exams_rv.setAdapter(adapter_exams);

        reservations_rv.setHasFixedSize(true);
        LinearLayoutManager llm3 = new LinearLayoutManager(this);
        reservations_rv.setLayoutManager(llm3);
        adapter_reservations = new EventAdapter(this, reservations, eal);
        reservations_rv.setAdapter(adapter_reservations);
    }

    private void refreshEvents() {

        runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(true);
            emptyButton.setEnabled(false);
        });
        new Thread(() -> {
            try {
                List<matypist.openstud.driver.core.models.Event> newEvents = InfoManager.getEvents(this, os, student);
                updateEventList(newEvents);
                h.sendEmptyMessage(ClientHelper.Status.OK.getValue());
                showCalendarNotification();
                updateTimer();
            } catch (OpenstudConnectionException e) {
                h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR.getValue());
                e.printStackTrace();
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) h.sendEmptyMessage(ClientHelper.Status.RATE_LIMIT.getValue());
                if (e.isMaintenance())
                    h.sendEmptyMessage(ClientHelper.Status.MAINTENANCE.getValue());
                else h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
                e.printStackTrace();
            } catch (OpenstudInvalidCredentialsException e) {
                h.sendEmptyMessage(ClientHelper.getStatusFromLoginException(e).getValue());
                e.printStackTrace();
            } finally {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    emptyButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void updateCalendar(List<matypist.openstud.driver.core.models.Event> events) {
        if (events == null) return;
        List<Event> calendarEvents = new LinkedList<>();
        List<Long> withLesson = new LinkedList<>();
        for (matypist.openstud.driver.core.models.Event event : events) {
            if (event.getEventType() == EventType.LESSON && InfoManager.filterContains(this, event.getTitle()))
                continue;
            ZoneId zoneId = ZoneId.systemDefault();
            Timestamp timestamp = event.getTimestamp(zoneId);
            if (timestamp == null) continue;
            Event ev;
            if (event.getEventType() == EventType.DOABLE) {
                ev = new Event(Color.YELLOW, timestamp.getTime());
            } else if (event.getEventType() == EventType.LESSON) {
                if (withLesson.contains(timestamp.getTime())) continue;
                withLesson.add(timestamp.getTime());
                ev = new Event(Color.GREEN, timestamp.getTime());
            } else {
                ev = new Event(Color.RED, timestamp.getTime());
            }
            calendarEvents.add(ev);
        }
        runOnUiThread(() -> {
            compactCalendarView.removeAllEvents();
            compactCalendarView.addEvents(calendarEvents);
        });

        synchronized (this) {
            getEventsByDate(events, currentDate);
        }
    }

    private void getEventsByDate(List<matypist.openstud.driver.core.models.Event> events, Date date) {
        List<matypist.openstud.driver.core.models.Event> newLessons = new LinkedList<>();
        List<matypist.openstud.driver.core.models.Event> newReservations = new LinkedList<>();
        List<matypist.openstud.driver.core.models.Event> newDoable = new LinkedList<>();
        ZoneId zoneId = ZoneId.systemDefault();
        for (matypist.openstud.driver.core.models.Event event : events) {
            if (event.getEventDate() == null || (event.getEventType() == EventType.LESSON && InfoManager.filterContains(this, event.getTitle())))
                continue;
            Instant instant = Instant.ofEpochMilli(date.getTime());
            instant.atZone(zoneId);
            long eventDateInMilli;
            eventDateInMilli = event.getEventDate().atStartOfDay(zoneId).toInstant().toEpochMilli();

            if (instant.toEpochMilli() != eventDateInMilli)
                continue;
            if (event.getEventType() == EventType.DOABLE) {
                newDoable.add(event);
            } else if (event.getEventType() == EventType.LESSON) {
                newLessons.add(event);
            } else {
                newReservations.add(event);
            }
        }
        lessons.clear();
        reservations.clear();
        exams.clear();
        exams.addAll(newDoable);
        reservations.addAll(newReservations);
        lessons.addAll(newLessons);
        ClientHelper.orderByStartTime(lessons, true);
        runOnUiThread(() -> {
            adapter_reservations.notifyDataSetChanged();
            adapter_exams.notifyDataSetChanged();
            adapter_lessons.notifyDataSetChanged();
        });
        if (exams.isEmpty() && reservations.isEmpty() && lessons.isEmpty()) {
            invalidateOptionsMenu();
            runOnUiThread(() -> {
                emptyContainer.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.VISIBLE);
                findViewById(R.id.lessons_container).setVisibility(View.GONE);
                findViewById(R.id.exams_container).setVisibility(View.GONE);
                findViewById(R.id.reservations_container).setVisibility(View.GONE);
            });

        } else {
            invalidateOptionsMenu();
            runOnUiThread(() -> {
                emptyContainer.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
                if (!lessons.isEmpty())
                    findViewById(R.id.lessons_container).setVisibility(View.VISIBLE);
                else findViewById(R.id.lessons_container).setVisibility(View.GONE);
                if (!exams.isEmpty())
                    findViewById(R.id.exams_container).setVisibility(View.VISIBLE);
                else findViewById(R.id.exams_container).setVisibility(View.GONE);
                if (!reservations.isEmpty())
                    findViewById(R.id.reservations_container).setVisibility(View.VISIBLE);
                else findViewById(R.id.reservations_container).setVisibility(View.GONE);
            });

        }

    }

    private void setCurrentDate(Date date) {
        setSubtitle(dateFormat.format(date));
        if (compactCalendarView != null) {
            runOnUiThread(() -> compactCalendarView.setCurrentDate(date));

        }
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView tvTitle = findViewById(R.id.title);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    private void setSubtitle(String subtitle) {
        TextView datePickerTextView = findViewById(R.id.date_picker_text_view);

        if (datePickerTextView != null) {
            datePickerTextView.setText(subtitle);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                showFilterDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    void animateExpansion() {
        float rotation = isExpanded ? 0 : 180;
        ViewCompat.animate(arrow).rotation(rotation).start();
        isExpanded = !isExpanded;
        expandableLayout.setExpanded(!expandableLayout.isExpanded(), true);
        appBarLayout.setExpanded(isExpanded, true);
    }

    public void onSaveInstanceState(Bundle savedInstance) {
        savedInstance.putBoolean("loadOnStart", false);
        savedInstance.putSerializable("currentDate", currentDate);
        super.onSaveInstanceState(savedInstance);
    }

    private boolean confirmReservation(ExamReservation res) {
        try {
            Pair<Integer, String> pair = os.insertReservation(res);
            InfoManager.setReservationUpdateFlag(this, true);
            if (pair == null) {
                h.sendEmptyMessage(ClientHelper.Status.UNEXPECTED_VALUE.getValue());
                return false;
            } else if (pair.getRight() == null && pair.getLeft() == -1) {
                h.sendEmptyMessage(ClientHelper.Status.ALREADY_PLACED.getValue());
                return true;
            }
            if (pair.getRight() != null) ClientHelper.createCustomTab(this, pair.getRight());
            else {
                refreshEvents();
                h.sendEmptyMessage(ClientHelper.Status.PLACE_RESERVATION_OK.getValue());
                return true;
            }
        } catch (OpenstudInvalidResponseException e) {
            e.printStackTrace();
            h.sendEmptyMessage(ClientHelper.Status.PLACE_RESERVATION_INVALID_RESPONSE.getValue());
        } catch (OpenstudConnectionException e) {
            e.printStackTrace();
            h.sendEmptyMessage(ClientHelper.Status.PLACE_RESERVATION_CONNECTION.getValue());
        } catch (OpenstudInvalidCredentialsException e) {
            e.printStackTrace();
            h.sendEmptyMessage(ClientHelper.Status.INVALID_CREDENTIALS.getValue());
        }
        return false;
    }

    private void deleteReservation(ExamReservation res) {
        try {
            int ret = os.deleteReservation(res);
            if (ret != -1) {
                synchronized (this) {
                    refreshEvents();
                }
                h.sendEmptyMessage(ClientHelper.Status.OK_DELETE.getValue());
            } else h.sendEmptyMessage(ClientHelper.Status.FAILED_DELETE.getValue());
        } catch (OpenstudInvalidResponseException | OpenstudConnectionException e) {
            h.sendEmptyMessage(ClientHelper.Status.FAILED_DELETE.getValue());
        } catch (OpenstudInvalidCredentialsException e) {
            h.sendEmptyMessage(ClientHelper.Status.INVALID_CREDENTIALS.getValue());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar_bar, menu);
        Drawable drawable = menu.findItem(R.id.filter).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.filter).setIcon(drawable);
        return true;
    }

    private synchronized List<String> generateListLessonsNames() {
        List<String> names = new LinkedList<>();
        for (matypist.openstud.driver.core.models.Event event : events) {
            if (!names.contains(event.getTitle()) && event.getEventType() == EventType.LESSON)
                names.add(event.getTitle());
        }
        return names;
    }

    private void showFilterDialog() {
        BottomSheetFilterEventFragment filterFrag = BottomSheetFilterEventFragment.newInstance(generateListLessonsNames());
        filterFrag.show(getSupportFragmentManager(), filterFrag.getTag());
    }

    private synchronized void updateEventList(List<matypist.openstud.driver.core.models.Event> newEvents) {
        if (newEvents != null && events != null && !events.equals(newEvents)) {
            events.clear();
            events.addAll(newEvents);
            updateCalendar(events);
            ClientHelper.updateExamWidget(this, true);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        synchronized (this) {
            if (refreshAfterDismiss) updateCalendar(events);
        }
    }

    public void onResume() {
        super.onResume();
        LocalDateTime time = getTimer();
        if (firstStart) firstStart = false;
        /* else if (lessonsEnabled != PreferenceManager.isLessonEnabled(this)) {
            refreshEvents();
            lessonsEnabled = !lessonsEnabled;
        } */ else if (lessonOptionsEnabled != PreferenceManager.isLessonOptionEnabled(this)) {
            if (adapter_lessons != null) adapter_lessons.notifyDataSetChanged();
            lessonOptionsEnabled = !lessonOptionsEnabled;
        } else if (time == null || Duration.between(time, LocalDateTime.now()).toMinutes() > 240)
            refreshEvents();
    }

    private synchronized void updateTimer() {
        lastUpdate = LocalDateTime.now();
    }

    private synchronized LocalDateTime getTimer() {
        return lastUpdate;
    }

    private void showCalendarNotification() {
        if (PreferenceManager.getCalendarNotificationEnabled(this)) {
            LayoutHelper.createActionSnackBar(mainLayout, R.string.lesson_notification, R.string.edit, 4000, v -> {
                Intent i = new Intent(CalendarActivity.this, SettingsPrefActivity.class);
                startActivity(i);
            });
            PreferenceManager.setCalendarNotificationEnabled(this, false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (events == null || events.isEmpty()) {
            MenuItem item = menu.findItem(R.id.filter);
            item.setVisible(false);
        } else {
            boolean enable = false;
            for (matypist.openstud.driver.core.models.Event event : events) {
                if (event.getEventType() == EventType.LESSON) {
                    enable = true;
                    break;
                }
            }
            MenuItem item = menu.findItem(R.id.filter);
            item.setVisible(enable);
        }
        return true;
    }

    private static class CalendarEventHandler extends Handler {
        private final WeakReference<CalendarActivity> mActivity;

        CalendarEventHandler(CalendarActivity activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CalendarActivity activity = mActivity.get();
            if (activity != null) {
                View.OnClickListener listener = v -> activity.refreshEvents();
                if (msg.what == ClientHelper.Status.CONNECTION_ERROR.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.INVALID_RESPONSE.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.invalid_response_error, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.MAINTENANCE.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.infostud_maintenance, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.RATE_LIMIT.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.rate_limit, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.INVALID_CREDENTIALS.getValue() || msg.what == ClientHelper.Status.EXPIRED_CREDENTIALS.getValue()
                        || msg.what == ClientHelper.Status.ACCOUNT_BLOCKED.getValue()) {
                    ClientHelper.rebirthApp(activity, msg.what);
                } else if (msg.what == ClientHelper.Status.PLACE_RESERVATION_OK.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.reservation_ok, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.PLACE_RESERVATION_INVALID_RESPONSE.getValue() || msg.what == ClientHelper.Status.PLACE_RESERVATION_CONNECTION.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.reservation_error, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.ALREADY_PLACED.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.already_placed_reservation, Snackbar.LENGTH_LONG);
                } else if (msg.what == (ClientHelper.Status.FAILED_DELETE).getValue()) {
                    LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.failed_delete, Snackbar.LENGTH_LONG);
                } else if (msg.what == (ClientHelper.Status.OK_DELETE).getValue()) {
                    LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.ok_delete, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.FAILED_GET.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.failed_get_network, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.CLOSED_RESERVATION.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.closed_reservation, Snackbar.LENGTH_LONG);
                }

            }
        }
    }

}