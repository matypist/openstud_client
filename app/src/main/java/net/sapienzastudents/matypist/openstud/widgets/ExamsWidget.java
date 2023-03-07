package net.sapienzastudents.matypist.openstud.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;

import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.WidgetHelper;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.List;
import java.util.Objects;

import matypist.openstud.driver.core.Openstud;
import matypist.openstud.driver.core.models.Event;
import matypist.openstud.driver.core.models.Student;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;


public class ExamsWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        boolean includeDoable = ExamsWidgetConfigureActivity.getAvaialableExamSwitchStatus(context, appWidgetId);
        boolean showCountdown = ExamsWidgetConfigureActivity.getCountdownSwitchStatus(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.exams_events_widget);
        Openstud os = InfoManager.getOpenStud(context);
        if (os != null) {
            if (hasAtLeastOneEvent(os, context, includeDoable)) {
                views.setViewVisibility(R.id.content_layout, View.VISIBLE);
                views.setViewVisibility(R.id.no_results_layout, View.GONE);
            } else {
                views.setViewVisibility(R.id.content_layout, View.GONE);
                views.setViewVisibility(R.id.no_results_layout, View.VISIBLE);
            }
            views.setViewVisibility(R.id.empty_layout, View.GONE);
            updateView(context, views, appWidgetId, includeDoable, showCountdown);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
        } else {
            views.setViewVisibility(R.id.content_layout, View.GONE);
            views.setViewVisibility(R.id.empty_layout, View.VISIBLE);
            views.setViewVisibility(R.id.no_results_layout, View.GONE);
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static boolean hasAtLeastOneEvent(Openstud os, Context context, boolean includeDoable) {
        List<Event> newEvents = InfoManager.getEventsCached(context, os);
        if (newEvents == null) return true;
        return !WidgetHelper.filterValidExamsEvents(newEvents, includeDoable).isEmpty();
    }

    private static void updateView(Context context, RemoteViews views, int appWidgetId, boolean includeDoable, boolean showCountdown) {
        Intent intent = new Intent(context, ExamsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("includeDoable", includeDoable);
        intent.putExtra("showCountdown", showCountdown);
        views.setRemoteAdapter(R.id.list_view, intent);
    }

    private static void scheduleNextUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ExamsWidget.class);
        intent.setAction("ACTION_SCHEDULED_UPDATE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        // Get a ZonedDateTime instance for midnight tomorrow.
        ZonedDateTime ldtZoned = LocalDateTime.now().atZone(ZoneId.systemDefault());
        ldtZoned = ldtZoned.withHour(0).withMinute(1).plusDays(1).withNano(0);
        ZonedDateTime ldtUtc = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
        alarmManager.set(AlarmManager.RTC_WAKEUP, ldtUtc.toInstant().toEpochMilli(), pendingIntent);
    }

    private void getUpdates(Context context, boolean cached, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        LocalDateTime lastUpdateTime = InfoManager.getLastExamsWidgetUpdateTime(context);
        ZonedDateTime ldtZoned = LocalDateTime.now().atZone(ZoneId.systemDefault());
        LocalDateTime ldtUtc = ldtZoned.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        if (!cached && (lastUpdateTime == null || ChronoUnit.HOURS.between(lastUpdateTime, ldtUtc) >= 24)) {
            Handler mHandler = new Handler(Looper.getMainLooper());
            new Thread(() -> {
                Openstud os = InfoManager.getOpenStud(context);
                if (os != null) {
                    Student student = InfoManager.getInfoStudentCached(context, os);
                    if (student != null) {
                        try {
                            InfoManager.getEvents(context, os, student);
                            InfoManager.setLastExamsWidgetUpdateTime(context, ldtUtc);
                        } catch (OpenstudConnectionException | OpenstudInvalidResponseException e) {
                            e.printStackTrace();
                        } catch (OpenstudInvalidCredentialsException e) {
                            InfoManager.clearSharedPreferences(context);
                            e.printStackTrace();
                        }
                    }
                }
                mHandler.post(() -> {
                    synchronized (this) {
                        for (int appWidgetId : appWidgetIds) {
                            updateAppWidget(context, appWidgetManager, appWidgetId);
                        }
                    }
                });
            }).start();
        }

        synchronized (this) {
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    @Override
    public synchronized void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        getUpdates(context, false, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            ExamsWidgetConfigureActivity.deletePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public void onUpdateCustom(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, boolean cached) {
        // There may be multiple widgets active, so update all of them
        if (appWidgetIds.length > 0) {
            getUpdates(context, cached, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), ExamsWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            if (appWidgetIds.length > 0) scheduleNextUpdate(context);
            if (Objects.equals(intent.getAction(), "MANUAL_UPDATE"))
                onUpdateCustom(context, appWidgetManager, appWidgetIds, extras.getBoolean("cached", true));
            if (Objects.equals(intent.getAction(), "ACTION_SCHEDULED_UPDATE"))
                onUpdateCustom(context, appWidgetManager, appWidgetIds, false);
        }
    }
}

