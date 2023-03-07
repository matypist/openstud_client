package net.sapienzastudents.matypist.openstud.data;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalTime;

public class CustomLesson {
    private DayOfWeek dayOfWeek;
    private String where;
    private LocalTime start;
    private LocalTime end;


    public CustomLesson() {

    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }
}