package net.sapienzastudents.matypist.openstud.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import net.sapienzastudents.matypist.openstud.R;

/**
 * The configuration screen for the {@link ExamsWidget ExamsWidget} AppWidget.
 */
public class ExamsWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "net.sapienzastudents.matypist.openstud.widgets.openstud.ExamsWidget";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Switch availableExamSwitch;
    Switch countdownSwitch;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = ExamsWidgetConfigureActivity.this;

            saveAvailableExamSwitchStatus(context, mAppWidgetId, availableExamSwitch.isChecked());
            saveCountdownSwitchStatus(context, mAppWidgetId, countdownSwitch.isChecked());
            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ExamsWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public ExamsWidgetConfigureActivity() {
        super();
    }

    static void saveAvailableExamSwitchStatus(Context context, int appWidgetId, boolean enabled) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putBoolean("includeDoable" + appWidgetId, enabled);
        prefs.apply();
    }

    static void saveCountdownSwitchStatus(Context context, int appWidgetId, boolean enabled) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putBoolean("showCountdown" + appWidgetId, enabled);
        prefs.apply();
    }

    static boolean getAvaialableExamSwitchStatus(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean("includeDoable" + appWidgetId, true);

    }

    static boolean getCountdownSwitchStatus(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean("showCountdown" + appWidgetId, true);
    }

    static void deletePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove("includeDoable" + appWidgetId);
        prefs.remove("showCountdown" + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.exams_events_widget_configure);
        availableExamSwitch = findViewById(R.id.include_exam_available_switch);
        countdownSwitch = findViewById(R.id.show_countdown_switch);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        availableExamSwitch.setChecked(getAvaialableExamSwitchStatus(ExamsWidgetConfigureActivity.this, mAppWidgetId));
        countdownSwitch.setChecked(getCountdownSwitchStatus(ExamsWidgetConfigureActivity.this, mAppWidgetId));
    }
}

