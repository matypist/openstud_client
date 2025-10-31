package net.sapienzastudents.matypist.openstud.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Environment;
import android.provider.CalendarContract;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.PermissionListener;
import net.sapienzastudents.matypist.openstud.BuildConfig;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.CustomCourse;
import net.sapienzastudents.matypist.openstud.data.CustomLesson;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.activities.AboutActivity;
import net.sapienzastudents.matypist.openstud.activities.CalendarActivity;
import net.sapienzastudents.matypist.openstud.activities.EventsActivity;
import net.sapienzastudents.matypist.openstud.activities.ExamsActivity;
import net.sapienzastudents.matypist.openstud.activities.LauncherActivity;
import net.sapienzastudents.matypist.openstud.activities.NewsActivity;
import net.sapienzastudents.matypist.openstud.activities.PaymentsActivity;
import net.sapienzastudents.matypist.openstud.activities.ProfileActivity;
import net.sapienzastudents.matypist.openstud.activities.SearchClassroomActivity;
import net.sapienzastudents.matypist.openstud.activities.SettingsPrefActivity;
import net.sapienzastudents.matypist.openstud.activities.StatsActivity;
import net.sapienzastudents.matypist.openstud.activities.WebViewActivity;
import net.sapienzastudents.matypist.openstud.widgets.ExamsWidget;
import net.sapienzastudents.matypist.openstud.widgets.GradesWidget;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import matypist.openstud.driver.core.OpenstudHelper;
import matypist.openstud.driver.core.models.Event;
import matypist.openstud.driver.core.models.EventType;
import matypist.openstud.driver.core.models.ExamDone;
import matypist.openstud.driver.core.models.ExamReservation;
import matypist.openstud.driver.core.models.Lesson;
import matypist.openstud.driver.exceptions.OpenstudBaseLoginException;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;

public class ClientHelper {

    public static Date getDateWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static void orderByStartTime(List<Event> events, boolean ascending) {
        Collections.sort(events, (o1, o2) -> {
            if (o1.getStart() == null && o2.getStart() == null) return 0;
            if (ascending)
                if (o1.getStart() == null) return 1;
                else if (o2.getStart() == null) return -1;
                else return o1.getStart().compareTo(o2.getStart());
            else {
                if (o1.getStart() == null) return -1;
                else if (o2.getStart() == null) return 1;
                else return o2.getStart().compareTo(o1.getStart());
            }
        });
    }

    public static void orderEventByDate(List<Event> events, boolean ascending) {
        Collections.sort(events, (o1, o2) -> {
            if (o1.getEventDate() == null && o2.getEventDate() == null) return 0;
            if (ascending)
                if (o1.getEventDate() == null) return 1;
                else if (o2.getEventDate() == null) return -1;
                else return o1.getEventDate().compareTo(o2.getEventDate());
            else {
                if (o1.getEventDate() == null) return -1;
                else if (o2.getEventDate() == null) return 1;
                else return o2.getEventDate().compareTo(o1.getEventDate());
            }
        });
    }

    public static ArrayList<Entry> generateMarksPoints(List<ExamDone> exams, int laude) {
        LinkedList<ExamDone> temp = new LinkedList<>(exams);
        Collections.reverse(temp);
        ArrayList<Entry> ret = new ArrayList<>();
        for (ExamDone exam : temp) {
            if (exam.getDate() == null) continue;
            int result = exam.getResult();
            if (!(result >= 18 && exam.isPassed())) continue;
            if (result > 30) result = laude;
            ZoneId zoneId = ZoneId.systemDefault();
            Timestamp timestamp = new Timestamp(exam.getDate().atStartOfDay(zoneId).toEpochSecond());
            ret.add(new Entry(timestamp.getTime() * 1000L, result));
        }
        return ret;
    }

    public static ArrayList<Entry> generateWeightPoints(List<ExamDone> exams, int laude) {
        LinkedList<ExamDone> temp = new LinkedList<>(exams);
        Collections.reverse(temp);
        List<ExamDone> placeholder = new LinkedList<>();
        ArrayList<Entry> ret = new ArrayList<>();
        for (ExamDone exam : temp) {
            if (exam.getDate() == null) continue;
            if (!(exam.getResult() >= 18 && exam.isPassed())) continue;
            placeholder.add(exam);
            float average = (float) OpenstudHelper.computeWeightedAverage(placeholder, laude);
            ZoneId zoneId = ZoneId.systemDefault();
            Timestamp timestamp = new Timestamp(exam.getDate().atStartOfDay(zoneId).toEpochSecond());
            ret.add(new Entry(timestamp.getTime() * 1000L, average));
        }
        return ret;
    }

    public static void updateGradesWidget(Activity activity, boolean cached) {
        Intent intent = new Intent(activity, GradesWidget.class);
        intent.setAction("MANUAL_UPDATE");
        intent.putExtra("cached", cached);
        int[] ids = AppWidgetManager.getInstance(activity.getApplication()).getAppWidgetIds(new ComponentName(activity.getApplication(), GradesWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        activity.sendBroadcast(intent);
    }

    public static void updateExamWidget(Activity activity, boolean cached) {
        Intent intent = new Intent(activity, ExamsWidget.class);
        intent.setAction("MANUAL_UPDATE");
        intent.putExtra("cached", cached);
        int[] ids = AppWidgetManager.getInstance(activity.getApplication()).getAppWidgetIds(new ComponentName(activity.getApplication(), ExamsWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        activity.sendBroadcast(intent);
    }

    public static ArrayList<BarEntry> generateMarksBar(List<ExamDone> exams) {
        @SuppressLint("UseSparseArrays") Map<Integer, Integer> map = new HashMap<>();
        for (ExamDone exam : exams) {
            if (!(exam.getResult() >= 18 && exam.isPassed())) continue;
            int result = exam.getResult();
            if (result > 30) result = 31;
            if (map.containsKey(result)) {
                map.put(result, map.get(result) + 1);
            } else map.put(result, 1);
        }
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (Integer key : map.keySet()) {
            entries.add(new BarEntry(key, map.get(key)));
        }
        return entries;
    }

    public static void createCustomTab(Context context, String url) {
        Bitmap closeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_arrow_back);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.redSapienza));
        builder.setCloseButtonIcon(closeIcon);
        CustomTabsIntent customTabsIntent = builder.build();
        try {
            customTabsIntent.launchUrl(context, Uri.parse(url));
        } catch (ActivityNotFoundException e) {
            //No browser that supports custom tabs
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    public static void createWebViewActivity(Context context, String title, String subtitle, String url, ClientHelper.WebViewType type) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("subtitle", subtitle);
        intent.putExtra("webviewType", type.getValue());
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    public static void createConfirmDeleteReservationDialog(Activity activity, final ExamReservation res, Runnable action) {
        if (activity == null) return;
        int themeId = ThemeEngine.getAlertDialogTheme(activity);
        activity.runOnUiThread(() -> new AlertDialog.Builder(new ContextThemeWrapper(activity, themeId))
                .setTitle(activity.getResources().getString(R.string.delete_res_dialog_title))
                .setMessage(activity.getResources().getString(R.string.delete_res_dialog_description, res.getExamSubject()))
                .setPositiveButton(activity.getResources().getString(R.string.delete_ok), (dialog, which) -> new Thread(action).start())
                .setNegativeButton(activity.getResources().getString(R.string.delete_abort), (dialog, which) -> {
                })
                .show());
    }

    public static boolean hasPassedExams(List<ExamDone> exams) {
        if (exams == null || exams.isEmpty()) return false;
        for (ExamDone exam : exams) {
            if (exam.getResult() >= 18 && exam.isPassed()) return true;
        }
        return false;
    }

    public static void addReservationToCalendar(Activity activity, final ExamReservation res) {
        // Determine the event title based on the device's default language
        final String title;
        if (Locale.getDefault().getLanguage().equals("it")) {
            title = "Esame: " + res.getExamSubject();
        } else {
            title = "Exam: " + res.getExamSubject();
        }

        // Get the start of the exam day (00:00:00) in the user's local timezone
        final ZoneId zoneId = ZoneId.systemDefault();
        final long startTime = res.getExamDate().atStartOfDay(zoneId).toInstant().toEpochMilli();

        // Calculate the end time. For an ALL_DAY event, the end time is exclusive.
        // To make an event span one full day (e.g., all of October 31st),
        // the start time must be 00:00 on Oct 31st and the end time
        // must be 00:00 on November 1st. This is why we add exactly 24 hours.
        //
        // See official Android documentation:
        // https://developer.android.com/guide/topics/providers/calendar-provider#all-day-events
        final long endTime = startTime + (24 * 60 * 60 * 1000L); // Add one full day in milliseconds

        // Create the intent to open the user's calendar application to the "edit event" screen
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");

        // Populate the intent with the event details
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
        intent.putExtra(CalendarContract.Events.ALL_DAY, true);
        intent.putExtra(CalendarContract.Events.TITLE, title);
        if (res.getNote() != null || !res.getNote().trim().isEmpty()) {
            intent.putExtra(CalendarContract.Events.DESCRIPTION, res.getNote());
        }

        // Launch the calendar app. The user will be able to confirm or cancel the event.
        activity.startActivity(intent);
    }

    public static String getSapienzaStudentsNetUrl(int codeCourse) {
        String ssnUrl;
        switch(codeCourse) {
            // "Applied Computer Science and Artificial Intelligence"
            case 30786:
            case 33502:
                ssnUrl = "https://sapienzastudents.net/acsai/";
                break;

            // "Informatica" and "Informatica - erogato in modalità prevalentemente a distanza"
            case 29923:
            case 29400:
            case 33503:
            case 33504:
                ssnUrl = "https://sapienzastudents.net/it/";
                break;

            // "Computer Science"
            case 29932:
            case 33508:
                ssnUrl = "https://sapienzastudents.net/compsci/";
                break;

            // "Cybersecurity"
            case 29389:
            case 33516:
                ssnUrl = "https://sapienzastudents.net/cybersec/";
                break;

            // "Filosofia"
            case 30850:
            case 33525:
                ssnUrl = "https://sapienzastudents.net/33525/";
                break;

            // "Filosofia e Intelligenza Artificiale"
            case 31774:
            case 33526:
                ssnUrl = "https://sapienzastudents.net/33526/";
                break;

            default:
                ssnUrl = "https://sapienzastudents.net";
                break;
        }
        return ssnUrl;
    }

    public static void startDrawerActivity(long item, Activity activity, int codeCourse) {
        if (item == LayoutHelper.Selection.TAX.getValue()) {
            if (activity instanceof PaymentsActivity) return;
            Intent intent = new Intent(activity, PaymentsActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.SAPIENZASTUDENTSNET.getValue()) {
            String ssnUrl = ClientHelper.getSapienzaStudentsNetUrl(codeCourse);
            ClientHelper.createWebViewActivity(activity, "sapienzastudents.net", activity.getResources().getString(R.string.sapienzastudentsnet_description), ssnUrl, ClientHelper.WebViewType.SAPIENZASTUDENTSNET);
        } else if (item == LayoutHelper.Selection.CALENDAR.getValue()) {
            if (activity instanceof CalendarActivity) return;
            Intent intent = new Intent(activity, CalendarActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.PROFILE.getValue()) {
            if (activity instanceof ProfileActivity) return;
            Intent intent = new Intent(activity, ProfileActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.EXIT.getValue()) {
            InfoManager.clearSharedPreferences(activity);
            PreferenceManager.setBiometricsEnabled(activity, false);
            Intent i = new Intent(activity, LauncherActivity.class);
            activity.startActivity(i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else if (item == LayoutHelper.Selection.CLASSROOMS.getValue()) {
            if (activity instanceof SearchClassroomActivity) return;
            Intent intent = new Intent(activity, SearchClassroomActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.ABOUT.getValue()) {
            Intent intent = new Intent(activity, AboutActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.SETTINGS.getValue()) {
            Intent intent = new Intent(activity, SettingsPrefActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.STATS.getValue()) {
            if (activity instanceof StatsActivity) return;
            Intent intent = new Intent(activity, StatsActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.EXAMS.getValue()) {
            if (activity instanceof ExamsActivity) return;
            Intent intent = new Intent(activity, ExamsActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.NEWS.getValue()) {
            if (activity instanceof NewsActivity) return;
            Intent intent = new Intent(activity, NewsActivity.class);
            activity.startActivity(intent);
        } else if (item == LayoutHelper.Selection.EVENTS.getValue()) {
            if (activity instanceof EventsActivity) return;
            Intent intent = new Intent(activity, EventsActivity.class);
            activity.startActivity(intent);
        }
    }

    public static void rebirthApp(Activity activity, Integer errorCode) {
        InfoManager.clearSharedPreferences(activity);
        PreferenceManager.setBiometricsEnabled(activity, false);
        Intent i = new Intent(activity, LauncherActivity.class);
        if (errorCode != null) i.putExtra("error", errorCode);
        activity.startActivity(i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        activity.finish();
    }

    public static void addEventToCalendar(Activity activity, final Event ev) {
        switch (ev.getEventType()) {
            case THEATRE:
            case LESSON: {
                final ZoneId zoneId = ZoneId.systemDefault();
                final Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");

                final String title = ev.getTitle();
                intent.putExtra(CalendarContract.Events.TITLE, title);

                // Use java.time directly
                final long startTime = ev.getStart().atZone(zoneId).toInstant().toEpochMilli();
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);

                if (ev.getEventType() == EventType.LESSON && ev.getEnd() != null) {
                    final long endTime = ev.getEnd().atZone(zoneId).toInstant().toEpochMilli();
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
                }

                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, ev.getWhere());
                intent.putExtra(CalendarContract.Events.ALL_DAY, false);

                activity.startActivity(intent);
                break;
            }
            case DOABLE:
            case RESERVED: {
                final ExamReservation res = ev.getReservation();
                if (res == null || res.getExamDate() == null) {
                    return;
                }

                ClientHelper.addReservationToCalendar(activity, res);
                break;
            }
        }
    }

    public static List<Lesson> generateLessonsForCustomCourses(List<CustomCourse> courses) {
        List<Lesson> lessons = new LinkedList<>();
        if (courses == null) return lessons;
        LocalDate maxDate = null;
        LocalDate minDate = null;
        for (CustomCourse course : courses) {
            if (maxDate == null) maxDate = course.getEndCourse();
            else if (course.getEndCourse().isAfter(maxDate)) maxDate = course.getEndCourse();
            if (minDate == null) minDate = course.getStartCourse();
            else if (course.getStartCourse().isBefore(minDate)) minDate = course.getStartCourse();
        }
        if (minDate == null || maxDate == null) return lessons;
        List<LocalDate> totalDates = new LinkedList<>();
        while (!minDate.isAfter(maxDate)) {
            totalDates.add(minDate);
            minDate = minDate.plusDays(1);
        }
        for (LocalDate date : totalDates) {
            for (CustomCourse course : courses) {
                for (CustomLesson lesson : course.getLessons()) {
                    if (lesson.getDayOfWeek() == date.getDayOfWeek()) {
                        Lesson newLesson = new Lesson();
                        newLesson.setName(course.getTitle());
                        newLesson.setTeacher(course.getTeacher());
                        newLesson.setWhere(lesson.getWhere());
                        newLesson.setStart(LocalDateTime.of(date, lesson.getStart()));
                        newLesson.setEnd(LocalDateTime.of(date, lesson.getEnd()));
                        lessons.add(newLesson);
                    }
                }
            }
        }
        return lessons;
    }

    public static void setDialogView(View v, Dialog dialog, int state) {
        if (dialog == null) return;
        dialog.setContentView(v);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) v.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        ((BottomSheetBehavior) behavior).setState(state);
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File file : fileOrDirectory.listFiles()) {
                deleteRecursive(file);
            }
        }
        fileOrDirectory.delete();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkCapabilities activeNetworkInfo = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (activeNetworkInfo == null) return false;
        return activeNetworkInfo.hasCapability(NET_CAPABILITY_INTERNET);
    }

    public static boolean canPlaceReservation(ExamReservation res) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDate zonedDate = now.atOffset(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.of("+1")).toLocalDate();
        return (ChronoUnit.DAYS.between(res.getStartDate(), zonedDate)) >= 0 && (ChronoUnit.DAYS.between(res.getEndDate(), zonedDate) <= 0);
    }

    public static boolean canDeleteReservation(ExamReservation res) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDate zonedDate = now.atOffset(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.of("+1")).toLocalDate();
        return !(ChronoUnit.DAYS.between(res.getEndDate(), zonedDate) >= 1);
    }

    public static void hideKeyboard(View v, Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    public static void requestReadWritePermissions(Activity activity, PermissionListener listener) {
        Dexter.withActivity(activity).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(listener).check();
    }

    public static void openActionViewPDF(Activity activity, File pdfFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            Uri uri = FileProvider.getUriForFile(activity, "net.sapienzastudents.matypist.openstud.provider", pdfFile);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (intent.resolveActivity(activity.getPackageManager()) != null)
                activity.startActivity(intent);
            else {
                if (activity instanceof ExamsActivity) {
                    ExamsActivity examsActivity = (ExamsActivity) activity;
                    examsActivity.createTextSnackBar(R.string.no_pdf_app, Snackbar.LENGTH_LONG);
                } else
                    activity.runOnUiThread(() -> Toasty.error(activity, R.string.no_pdf_app).show());
            }
        } catch (IllegalArgumentException e) {
            ClientHelper.reportException(e);
            if (activity instanceof ExamsActivity) {
                ExamsActivity examsActivity = (ExamsActivity) activity;
                examsActivity.createTextSnackBar(R.string.failed_get_io, Snackbar.LENGTH_LONG);
            } else
                activity.runOnUiThread(() -> Toasty.error(activity, R.string.failed_get_io).show());
        }

    }

    public static void reportException(Exception e) {
        if (BuildConfig.FLAVOR.equals("full")) {
            try {
                Class crashlytics = Class.forName("com.crashlytics.android.Crashlytics");
                Method logException = crashlytics.getMethod("logException", Throwable.class);
                logException.invoke(crashlytics, e);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Status getStatusFromLoginException(OpenstudBaseLoginException e) {
        if (e.isPasswordExpired()) return Status.EXPIRED_CREDENTIALS;
        else if (e.isAccountBlocked()) return Status.ACCOUNT_BLOCKED;
        else return Status.INVALID_CREDENTIALS;
    }

    private static String simpleStringXOR(String input, String key) {
        if (key == null || key.isEmpty()) return input;
        char[] k = key.toCharArray();
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ k[i % k.length]));
        }
        return output.toString();
    }

    private static String fromHexToString(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    private static String fromStringToHex(String text) {
        return String.format("%040x", new BigInteger(1, text.getBytes()));
    }

    public static Map<String, String> generateKeyMap() {
        Map<String, String> map = new HashMap<>();
        if (!BuildConfig.TIMETABLE_KEY_HEX.isEmpty())
            map.put("gomp", ClientHelper.simpleStringXOR(ClientHelper.fromHexToString(BuildConfig.TIMETABLE_KEY_HEX), BuildConfig.DEC_SECRET));
        return map;
    }

    public enum Status {
        OK(0), CONNECTION_ERROR(1), INVALID_RESPONSE(2), INVALID_CREDENTIALS(3), USER_NOT_ENABLED(4), UNEXPECTED_VALUE(5),
        EXPIRED_CREDENTIALS(6), FAILED_DELETE(7), OK_DELETE(8), FAILED_GET(9), FAILED_GET_IO(10), PLACE_RESERVATION_OK(11), PLACE_RESERVATION_CONNECTION(12),
        PLACE_RESERVATION_INVALID_RESPONSE(13), ALREADY_PLACED(14), CLOSED_RESERVATION(15), FAIL_LOGIN(16), ENABLE_BUTTONS(17), RECOVERY_OK(18), INVALID_ANSWER(19),
        INVALID_STUDENT_ID(20), NO_RECOVERY(21), CONNECTION_ERROR_RECOVERY(22), RATE_LIMIT(23), MAINTENANCE(24), NO_BIOMETRICS(25), LOCKOUT_BIOMETRICS(26), NO_BIOMETRIC_HW(27),
        BIOMETRIC_UNAVAILABLE(28), ACCOUNT_BLOCKED(29);
        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }


    public enum WebViewType {
        EMAIL(0), SAPIENZASTUDENTSNET(1);
        private final int value;

        WebViewType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Sort {
        Date(0), Mark(1);
        private int value;

        Sort(int value) {
            this.value = value;
        }

        public static Sort getSort(int type) {
            if (type == 0) return Date;
            else if (type == 1) return Mark;
            return null;
        }

        public int getValue() {
            return value;
        }

    }

}