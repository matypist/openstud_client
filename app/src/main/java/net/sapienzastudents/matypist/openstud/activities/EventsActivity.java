package net.sapienzastudents.matypist.openstud.activities;

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
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.adapters.EventTheatreAdapter;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import com.mikepenz.materialdrawer.Drawer;

import org.threeten.bp.ZoneId;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import matypist.openstud.driver.core.models.Event;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class EventsActivity extends BaseDataActivity {

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
    private Drawer drawer;
    private HorizontalCalendar horizontalCalendar;
    private Calendar defaultDate;
    private List<Event> selectedDateEvents = new LinkedList<>();
    private List<Event> events = new LinkedList<>();
    private EventTheatreAdapter adapter;
    private EventHandler h = new EventHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        ThemeEngine.applyClassroomTimetableTheme(this);
        setContentView(R.layout.activity_classroom_timetable);
        ButterKnife.bind(this);
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_YEAR, 7);
        defaultDate = getDefaultDateFromBundle(savedInstanceState);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        emptyText.setText(getResources().getString(R.string.no_events));
        drawer = LayoutHelper.applyDrawer(this, toolbar, student);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        Activity activity = this;
        createCalendar(startDate, endDate);
        adapter = new EventTheatreAdapter(this, selectedDateEvents, new EventTheatreAdapter.EventAdapterListener() {
            @Override
            public void addCalendarOnClick(Event ev) {
                ClientHelper.addEventToCalendar(activity, ev);
            }

            @Override
            public void onItemClick(View v) {
                int itemPosition = rv.getChildLayoutPosition(v);
                if (itemPosition < selectedDateEvents.size()) {
                    Event ev = selectedDateEvents.get(itemPosition);
                    ClientHelper.createCustomTab(activity, ev.getUrl());
                }
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.events);
        rv.setAdapter(adapter);
        int refreshColorId = ThemeEngine.getSpinnerColorId(this);
        swipeRefreshLayout.setColorSchemeResources(refreshColorId, refreshColorId, refreshColorId);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeEngine.resolveColorFromAttribute(this, R.attr.SwipeSpinnerBackgroundColor, R.color.white));
        swipeRefreshLayout.setOnRefreshListener(() -> refreshEvents(true));
        emptyButton.setOnClickListener(v -> {
            if (!swipeRefreshLayout.isRefreshing())
                refreshEvents(true);
        });
        List<Event> cached_events = InfoManager.getEventsUniversityCached(this, os);
        swapViews(true);
        if (cached_events != null && !cached_events.isEmpty()) {
            events.clear();
            events.addAll(cached_events);
            applyEvents(defaultDate);
        }
        if (savedInstanceState == null) refreshEvents(true);
    }

    private void refreshEvents(boolean refresh) {
        new Thread(() -> {
            synchronized (this) {
                try {
                    if (refresh) {
                        setRefreshing(true);
                        List<Event> newEvents = InfoManager.getEventsUniversity(this, os);
                        if (newEvents != null && !newEvents.equals(events)) {
                            events.clear();
                            events.addAll(newEvents);
                        }
                    }
                } catch (OpenstudConnectionException e) {
                    e.printStackTrace();
                    h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR.getValue());
                } catch (OpenstudInvalidResponseException e) {
                    e.printStackTrace();
                    h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
                }
                applyEvents(horizontalCalendar.getSelectedDate());
                setRefreshing(false);
            }
        }).start();
    }

    private synchronized void applyEvents(Calendar date) {
        if (date == null) date = defaultDate;
        List<Event> eventDate = new LinkedList<>();
        for (Event ev : events) {
            if (ev.getStart() != null && date.getTimeInMillis() == ev.getStart().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                eventDate.add(ev);
        }
        selectedDateEvents.clear();
        selectedDateEvents.addAll(eventDate);
        if (selectedDateEvents.isEmpty()) swapViews(true);
        else swapViews(false);
        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private void setRefreshing(boolean bool) {
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(bool));
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

    private Calendar getDefaultDateFromBundle(Bundle savedInstance) {
        if (savedInstance != null) {
            long currentDateInMillis = savedInstance.getLong("currentDateInMillis", -1);
            Calendar today = Calendar.getInstance();
            today.setTimeInMillis(currentDateInMillis);
            return today;
        } else {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            return today;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();

    }

    public void onSaveInstanceState(Bundle savedInstance) {
        super.onSaveInstanceState(savedInstance);
        savedInstance.putLong("currentDateInMillis", horizontalCalendar.getSelectedDate().getTimeInMillis());
        System.out.println("saved"+horizontalCalendar.getSelectedDate());
    }


    private void createCalendar(Calendar start, Calendar end) {
        Activity activity = this;
        runOnUiThread(() -> {
            horizontalCalendar = new HorizontalCalendar.Builder(activity, R.id.calendarView)
                    .range(start, end)
                    .datesNumberOnScreen(5)
                    .defaultSelectedDate(defaultDate).build();
            horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
                @Override
                public void onDateSelected(Calendar date, int position) {
                    refreshEvents(false);
                }
            });
        });
    }

    private static class EventHandler extends Handler {
        private final WeakReference<EventsActivity> activity;

        private EventHandler(EventsActivity activity) {
            super(Looper.getMainLooper());
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final EventsActivity activity = this.activity.get();
            if (activity == null) return;
            View.OnClickListener ocl = v -> activity.refreshEvents(true);
            if (msg.what == ClientHelper.Status.CONNECTION_ERROR.getValue()) {
                LayoutHelper.createActionSnackBar(activity.constraintLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, ocl);
            } else if (msg.what == ClientHelper.Status.INVALID_RESPONSE.getValue()) {
                LayoutHelper.createActionSnackBar(activity.constraintLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, ocl);
            }
        }
    }

}
