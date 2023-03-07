package net.sapienzastudents.matypist.openstud.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.sapienzastudents.matypist.openstud.BuildConfig;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;

import org.threeten.bp.LocalDateTime;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import matypist.openstud.driver.core.Openstud;
import matypist.openstud.driver.core.OpenstudBuilder;
import matypist.openstud.driver.core.OpenstudHelper;
import matypist.openstud.driver.core.models.Event;
import matypist.openstud.driver.core.models.ExamDoable;
import matypist.openstud.driver.core.models.ExamDone;
import matypist.openstud.driver.core.models.ExamReservation;
import matypist.openstud.driver.core.models.Isee;
import matypist.openstud.driver.core.models.Lesson;
import matypist.openstud.driver.core.models.News;
import matypist.openstud.driver.core.models.Student;
import matypist.openstud.driver.core.models.StudentCard;
import matypist.openstud.driver.core.models.Tax;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class InfoManager {
    private static Openstud os;
    private static SharedPreferences pref;
    private static Student student;
    private static Isee isee;
    private static List<Tax> paidTaxes;
    private static List<Tax> unpaidTaxes;
    private static List<ExamDone> examsDone;
    private static List<ExamDoable> examsDoable;
    private static List<ExamReservation> reservations;
    private static List<News> news;
    private static List<ExamDone> fakeExams;
    private static List<Event> events;
    private static List<String> filter;
    private static List<Event> theatre_events;
    private static StudentCard card;

    private static synchronized void setupSharedPreferences(Context context) {
        if (pref != null) return;
        pref = context.getSharedPreferences("OpenStudPref", 0); // 0 - for private mode
    }

    public static void clearCookies() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
    }

    public static Openstud getOpenStud(Context context) {
        setupSharedPreferences(context);
        if (getStudentId(context) == null || getPassword(context) == null) return null;
        synchronized (InfoManager.class) {
            if (os != null) return os;
            OpenstudBuilder osb = new OpenstudBuilder().setStudentID(getStudentId(context)).setPassword(getPassword(context)).setRetryCounter(3).forceReadyState();
            if (BuildConfig.DEBUG) osb.setLogger(Logger.getLogger("OpenStud_client"));
            os = osb.build();
            return os;
        }
    }


    public static Openstud getOpenStudRecovery(Context context, String studentId) {
        setupSharedPreferences(context);
        if (studentId == null) return null;
        OpenstudBuilder osb = new OpenstudBuilder().setStudentID(studentId).setRetryCounter(3);
        if (BuildConfig.DEBUG) osb.setLogger(Logger.getLogger("OpenStud_client"));
        return osb.build();
    }

    public static Openstud getOpenStud(Context context, String studentId, String password) {
        setupSharedPreferences(context);
        if (studentId == null || password == null || password.isEmpty()) return null;
        OpenstudBuilder osb = new OpenstudBuilder().setStudentID(studentId).setPassword(password).setRetryCounter(3);
        if (BuildConfig.DEBUG) osb.setLogger(Logger.getLogger("OpenStud_client"));
        return osb.build();
    }

    public static void saveOpenStud(Context context, Openstud openstud, String studentId, String password, boolean save) {
        setupSharedPreferences(context);
        setNamePassword(context, studentId, password);
        setSaveFlag(context, save);
        synchronized (InfoManager.class) {
            os = openstud;
        }
    }

    public static Student getInfoStudentCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (student != null) return student;
            oldObj = pref.getString("student", "null");
        }
        Student ret = null;
        try {
            ret = gson.fromJson(oldObj, Student.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }


    public static Student getInfoStudent(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        Student newStudent = os.getInfoStudent();
        synchronized (InfoManager.class) {
            student = newStudent;
            SharedPreferences.Editor prefsEditor = pref.edit();
            String json = gson.toJson(student);
            prefsEditor.putString("student", json);
            prefsEditor.commit();
        }
        return newStudent;
    }


    public static StudentCard getStudentCardCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (card != null) return card;
            oldObj = pref.getString("studentCard", null);
        }
        StudentCard ret = null;
        try {
            ret = gson.fromJson(oldObj, StudentCard.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static StudentCard getStudentCard(Context context, Openstud os, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        StudentCard newCard = os.getStudentCard(student, true);
        synchronized (InfoManager.class) {
            card = newCard;
            SharedPreferences.Editor prefsEditor = pref.edit();
            String json = gson.toJson(card);
            prefsEditor.putString("studentCard", json);
            prefsEditor.commit();
        }
        return newCard;
    }
/*
    public static Map<String, List<Lesson>> getTimetableCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        if (!hasLogin(context)) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (timetable != null) return timetable;
            oldObj = pref.getString("timetable", "null");
        }
        Type listType = new TypeToken<Map<String, List<Lesson>>>() {
        }.getType();
        return gson.fromJson(oldObj, listType);
    }


    public static Map<String, List<Lesson>> getTimetable(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        if (!hasLogin(context)) return null;
        Gson gson = new Gson();
        List<ExamDoable> doable = os.getExamsDoable();
        Map<String, List<Lesson>> newTimetable = os.getTimetable(doable);
        synchronized (InfoManager.class) {
            timetable = newTimetable;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<Map<String, List<Lesson>>>() {
            }.getType();
            String json = gson.toJson(timetable, listType);
            prefsEditor.putString("timetable", json);
            prefsEditor.apply();
        }
        return newTimetable;
    }
*/

    public static List<Event> getEventsCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (events != null) return new LinkedList<>(events);
            oldObj = pref.getString("events", "null");
        }
        Type listType = new TypeToken<List<Event>>() {
        }.getType();
        List<Event> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }


    public static List<Event> getEvents(Context context, Openstud os, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        Map<String, List<Lesson>> newTimetable = new HashMap<>();
        if (PreferenceManager.isLessonEnabled(context))
            newTimetable = os.getTimetable(os.getExamsDoable());
        List<matypist.openstud.driver.core.models.Event> newEvents = os.getCalendarEvents(student);
        if (newEvents == null) return null;
        if (newTimetable != null && !newTimetable.isEmpty())
            newEvents.addAll(OpenstudHelper.generateEventsFromTimetable(newTimetable));
        newEvents.addAll(OpenstudHelper.generateEventsFromTimetable(ClientHelper.generateLessonsForCustomCourses(PreferenceManager.getCustomCourses(context))));
        synchronized (InfoManager.class) {
            events = newEvents;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<List<Event>>() {
            }.getType();
            String json = gson.toJson(events, listType);
            prefsEditor.putString("events", json);
            prefsEditor.apply();
        }
        return new LinkedList<>(newEvents);
    }


    public static void saveFakeExams(Context context, List<ExamDone> exams) {
        setupSharedPreferences(context);
        Gson gson = new Gson();
        String obj;
        synchronized (InfoManager.class) {
            if (exams != null) fakeExams = new LinkedList<>(exams);
            else fakeExams = new LinkedList<>();
            Type listType = new TypeToken<List<ExamDone>>() {
            }.getType();
            obj = gson.toJson(fakeExams, listType);
            SharedPreferences.Editor prefsEditor = pref.edit();
            prefsEditor.putString("fakeExams", obj);
            prefsEditor.commit();
        }
    }

    public static List<ExamDone> getFakeExams(Context context, Openstud os) {
        return _getFakeExams(context, os, true);
    }

    private static synchronized List<ExamDone> _getFakeExams(Context context, Openstud os, boolean removeDuplicates) {
        setupSharedPreferences(context);
        if (os == null) return null;
        if (removeDuplicates) InfoManager.removeDuplicatesFakeExams(context, os);
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (fakeExams != null) return new LinkedList<>(fakeExams);
            oldObj = pref.getString("fakeExams", null);
        }
        Type listType = new TypeToken<List<ExamDone>>() {
        }.getType();
        List<ExamDone> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        if (ret == null) return new LinkedList<>();
        return ret;
    }


    public static Isee getIseeCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (isee != null) return isee;
            oldObj = pref.getString("isee", "null");
        }
        Isee ret = null;
        try {
            ret = gson.fromJson(oldObj, Isee.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static Isee getIsee(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        Isee newIsee = os.getCurrentIsee();
        synchronized (InfoManager.class) {
            isee = newIsee;
            SharedPreferences.Editor prefsEditor = pref.edit();
            String json = gson.toJson(isee);
            prefsEditor.putString("isee", json);
            prefsEditor.commit();
        }
        return isee;
    }

    public static List<Tax> getPaidTaxesCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (paidTaxes != null) return new LinkedList<>(paidTaxes);
            oldObj = pref.getString("paidTaxes", "null");
        }
        Type listType = new TypeToken<List<Tax>>() {
        }.getType();
        List<Tax> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<ExamDone> getExamsDoneCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (examsDone != null) return new LinkedList<>(examsDone);
            oldObj = pref.getString("examsDone", "null");
        }
        Type listType = new TypeToken<List<ExamDone>>() {
        }.getType();
        List<ExamDone> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<ExamDone> getExamsDone(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        List<ExamDone> newExamsDone = os.getExamsDone();
        synchronized (InfoManager.class) {
            examsDone = newExamsDone;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<List<ExamDone>>() {
            }.getType();
            String json = gson.toJson(examsDone, listType);
            prefsEditor.putString("examsDone", json);
            prefsEditor.apply();
        }
        return new LinkedList<>(newExamsDone);
    }

    public static List<ExamDoable> getExamsDoableCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (examsDoable != null) return new LinkedList<>(examsDoable);
            oldObj = pref.getString("examsDoable", "null");
        }
        Type listType = new TypeToken<List<ExamDoable>>() {
        }.getType();
        List<ExamDoable> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static List<ExamDoable> getExamsDoable(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        List<ExamDoable> newExamsDoable = os.getExamsDoable();
        synchronized (InfoManager.class) {
            examsDoable = newExamsDoable;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<List<ExamDoable>>() {
            }.getType();
            String json = gson.toJson(examsDoable, listType);
            prefsEditor.putString("examsDoable", json);
            prefsEditor.apply();
        }
        return new LinkedList<>(newExamsDoable);
    }


    public static List<ExamReservation> getActiveReservationsCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (reservations != null) return new LinkedList<>(reservations);
            oldObj = pref.getString("reservations", "null");
        }
        Type listType = new TypeToken<List<ExamReservation>>() {
        }.getType();
        List<ExamReservation> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<ExamReservation> getActiveReservations(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        List<ExamReservation> newExamsDone = os.getActiveReservations();
        synchronized (InfoManager.class) {
            reservations = newExamsDone;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<List<ExamReservation>>() {
            }.getType();
            String json = gson.toJson(reservations, listType);
            prefsEditor.putString("reservations", json);
            prefsEditor.apply();
        }
        return new LinkedList<>(newExamsDone);
    }

    public static List<Tax> getPaidTaxes(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        List<Tax> newPaidTaxes = os.getPaidTaxes();
        synchronized (InfoManager.class) {
            paidTaxes = newPaidTaxes;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<List<Tax>>() {
            }.getType();
            String json = gson.toJson(paidTaxes, listType);
            prefsEditor.putString("paidTaxes", json);
            prefsEditor.apply();
        }
        return new LinkedList<>(newPaidTaxes);
    }

    public static List<Tax> getUnpaidTaxesCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (unpaidTaxes != null) return new LinkedList<>(unpaidTaxes);
            oldObj = pref.getString("unpaidTaxes", "null");
        }
        Type listType = new TypeToken<List<Tax>>() {
        }.getType();
        List<Tax> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<Tax> getUnpaidTaxes(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        List<Tax> newUnpaidTaxes = os.getUnpaidTaxes();
        synchronized (InfoManager.class) {
            unpaidTaxes = newUnpaidTaxes;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<List<Tax>>() {
            }.getType();
            String json = gson.toJson(newUnpaidTaxes, listType);
            prefsEditor.putString("unpaidTaxes", json);
            prefsEditor.apply();
        }
        return new LinkedList<>(newUnpaidTaxes);
    }

    public static List<News> getNews(Context context, Openstud os, String locale) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        List<News> newNews = os.getNews(locale, true, null, 0, null, null);
        synchronized (InfoManager.class) {
            news = newNews;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<List<News>>() {
            }.getType();
            String json = gson.toJson(newNews, listType);
            prefsEditor.putString("news", json);
            prefsEditor.apply();
        }
        return new LinkedList<>(newNews);
    }

    public static List<News> getNewsCached(Context context, Openstud os, String locale) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (news != null) {
                if (!news.isEmpty() && !news.get(0).getLocale().equals(locale)) return null;
                return new LinkedList<>(news);
            }
            oldObj = pref.getString("news", "null");
        }
        Type listType = new TypeToken<List<News>>() {
        }.getType();
        List<News> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        if (ret != null && !ret.isEmpty() && !ret.get(0).getLocale().equals(locale)) return null;
        return ret;
    }


    public static List<Event> getEventsUniversity(Context context, Openstud os) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        setupSharedPreferences(context);
        if (os == null) return null;
        Gson gson = new Gson();
        List<Event> newEvents = os.getNewsletterEvents();
        synchronized (InfoManager.class) {
            theatre_events = newEvents;
            SharedPreferences.Editor prefsEditor = pref.edit();
            Type listType = new TypeToken<List<Event>>() {
            }.getType();
            String json = gson.toJson(newEvents, listType);
            prefsEditor.putString("eventsUniversity", json);
            prefsEditor.apply();
        }
        return new LinkedList<>(newEvents);
    }

    public static List<Event> getEventsUniversityCached(Context context, Openstud os) {
        setupSharedPreferences(context);
        if (os == null) return null;
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            if (theatre_events != null) {
                return new LinkedList<>(theatre_events);
            }
            oldObj = pref.getString("eventsUniversity", "null");
        }
        Type listType = new TypeToken<List<Event>>() {
        }.getType();
        List<Event> ret = null;
        try {
            ret = gson.fromJson(oldObj, listType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static LocalDateTime getLastExamsWidgetUpdateTime(Context context) {
        setupSharedPreferences(context);
        String oldObj;
        Gson gson = new Gson();
        synchronized (InfoManager.class) {
            oldObj = pref.getString("lastUpdateWidget", "null");
        }
        LocalDateTime ret = null;
        try {
            ret = gson.fromJson(oldObj, org.threeten.bp.LocalDateTime.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void setLastExamsWidgetUpdateTime(Context context, LocalDateTime time) {
        setupSharedPreferences(context);
        if (time == null) return;
        Gson gson = new Gson();
        String json = gson.toJson(time, LocalDateTime.class);
        synchronized (InfoManager.class) {
            pref.edit().putString("lastUpdateWidget", json).apply();
        }
    }

    public static boolean hasLogin(Context context) {
        setupSharedPreferences(context);
        return getStudentId(context) != null && getPassword(context) != null;
    }

    public static synchronized void clearSharedPreferences(Context context) {
        setupSharedPreferences(context);
        pref.edit().clear().commit();
        os = null;
        student = null;
        paidTaxes = null;
        unpaidTaxes = null;
        examsDone = null;
        examsDoable = null;
        reservations = null;
        news = null;
        fakeExams = null;
        events = null;
        filter = null;
        theatre_events = null;
        card = null;
        clearCookies();
    }

    public static synchronized String getStudentId(Context context) {
        setupSharedPreferences(context);
        String id = null;
        try {
            id = pref.getString("studentId", null);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return id;
    }

    private static synchronized String getPassword(Context context) {
        setupSharedPreferences(context);
        return pref.getString("password", null);
    }

    private static synchronized void setNamePassword(Context context, String id, String password) {
        setupSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("studentId", id);
        editor.putString("password", password);
        editor.apply();
    }

    private static synchronized void setSaveFlag(Context context, boolean saveFlag) {
        setupSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("remember", saveFlag);
        editor.apply();
    }

    public static synchronized void setSortType(Context context, int type) {
        setupSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("sort", type);
        editor.apply();
    }

    public static synchronized int getSortType(Context context) {
        setupSharedPreferences(context);
        return pref.getInt("sort", 0);
    }


    public static synchronized void setReservationUpdateFlag(Context context, boolean updateFlag) {
        setupSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("updateReservations", updateFlag);
        editor.apply();
    }

    public static synchronized boolean getReservationUpdateFlag(Context context) {
        setupSharedPreferences(context);
        return pref.getBoolean("updateReservations", false);
    }

    public static synchronized boolean getSaveFlag(Context context) {
        setupSharedPreferences(context);
        return pref.getBoolean("remember", false);
    }


    public static synchronized boolean filterContains(Context context, String name) {
        List<String> tmp_filter = getExceptionFilter(context);
        return tmp_filter.contains(name);
    }

    public static synchronized void addExceptionToFilter(Context context, String name) {
        List<String> tmp_filter = getExceptionFilter(context);
        if (!tmp_filter.contains(name)) tmp_filter.add(name);
        updateFilter(tmp_filter);
    }

    public static synchronized void removeExceptionFromFilter(Context context, String name) {
        List<String> tmp_filter = getExceptionFilter(context);
        tmp_filter.remove(name);
        updateFilter(new LinkedList<>(tmp_filter));
    }

    public static synchronized void removeOldEntriesFilter(Context context, List<String> names) {
        List<String> tmp_filter = getExceptionFilter(context);
        List<String> to_remove = new LinkedList<>();
        for (String exception : tmp_filter) {
            if (!names.contains(exception)) to_remove.add(exception);
        }
        tmp_filter.removeAll(to_remove);
        updateFilter(tmp_filter);
    }

    private static void updateFilter(List<String> update_filter) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        SharedPreferences.Editor editor = pref.edit();
        String toJson = gson.toJson(update_filter, listType);
        editor.putString("filter_calendar", toJson);
        editor.apply();
        if (filter == null) filter = new LinkedList<>();
        filter.clear();
        filter.addAll(update_filter);
    }

    private static LinkedList<String> getExceptionFilter(Context context) {
        setupSharedPreferences(context);
        if (filter != null) return new LinkedList<>(filter);
        synchronized (InfoManager.class) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            String json = pref.getString("filter_calendar", null);
            if (json == null) {
                filter = new LinkedList<>();
                SharedPreferences.Editor editor = pref.edit();
                String toJson = gson.toJson(filter, listType);
                editor.putString("filter_calendar", toJson);
                editor.apply();
            } else filter = gson.fromJson(json, listType);
            return new LinkedList<>(filter);
        }
    }

    private static void removeDuplicatesFakeExams(Context context, Openstud os) {
        List<ExamDone> fake = InfoManager._getFakeExams(context, os, false);
        List<ExamDone> done = InfoManager.getExamsDoneCached(context, os);
        List<ExamDone> remove = new LinkedList<>();
        if (fake != null && done != null) {
            for (ExamDone ex : fake) {
                for (ExamDone ex2 : done) {
                    if (ex.getDescription().toLowerCase().trim().equals(ex2.getDescription().toLowerCase().trim())) {
                        remove.add(ex);
                        break;
                    }
                }
            }
            fake.removeAll(remove);
            InfoManager.saveFakeExams(context, fake);
        }
    }
}
