package net.sapienzastudents.matypist.openstud.data;

import org.threeten.bp.LocalDate;

import java.util.List;

public class CustomCourse {

    private String title;
    private String teacher;
    private LocalDate startCourse;
    private LocalDate endCourse;
    private List<CustomLesson> lessons;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public List<CustomLesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<CustomLesson> lessons) {
        this.lessons = lessons;
    }

    public LocalDate getStartCourse() {
        return startCourse;
    }

    public void setStartCourse(LocalDate startCourse) {
        this.startCourse = startCourse;
    }

    public LocalDate getEndCourse() {
        return endCourse;
    }

    public void setEndCourse(LocalDate endCourse) {
        this.endCourse = endCourse;
    }

}
