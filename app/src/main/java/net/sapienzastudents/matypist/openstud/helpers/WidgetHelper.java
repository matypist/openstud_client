package net.sapienzastudents.matypist.openstud.helpers;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.LinkedList;
import java.util.List;

import matypist.openstud.driver.core.models.Event;
import matypist.openstud.driver.core.models.EventType;

public class WidgetHelper {


    private static boolean isOldEvent(Event event) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDate zonedDate = now.atOffset(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.of("+1")).toLocalDate();
        return zonedDate.isBefore(event.getEventDate());
    }

    public static long getRemainingDays(Event event) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDate zonedDate = now.atOffset(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.of("+1")).toLocalDate();
        return ChronoUnit.DAYS.between(zonedDate, event.getEventDate());
    }

    public static List<Event> filterValidExamsEvents(List<Event> events, boolean includeDoable) {
        List<Event> ignored = new LinkedList<>();
        for (Event event : events) {
            if (getRemainingDays(event) > 95) ignored.add(event);
            else if (event.getEventType() != EventType.RESERVED && event.getEventType() != EventType.DOABLE)
                ignored.add(event);
            else if (!includeDoable && event.getEventType() == EventType.DOABLE) ignored.add(event);
            else if (!WidgetHelper.isOldEvent(event)) ignored.add(event);
        }
        List<Event> newEvents = new LinkedList<>(events);
        newEvents.removeAll(ignored);
        return newEvents;
    }

    public static List<Event> mergeExamEvents(List<Event> events) {
        LinkedList<Event> output = new LinkedList<>();
        for (Event event : events) {
            if (event == null || event.getTitle() == null || event.getReservation() == null)
                continue;
            LinkedList<Event> doableCollisions = new LinkedList<>();
            boolean addedToOutput = false;
            for (Event event2 : events) {
                if (event2 == null || event2.getTitle() == null || event2.getReservation() == null)
                    continue;
                if (event != event2 && event.getTitle().equals(event2.getTitle()) && event.getReservation().getCourseCode() == event2.getReservation().getCourseCode()
                        && event.getEventDate().equals(event2.getEventDate())) {
                    if (event.getEventType() == EventType.DOABLE && event2.getEventType() == EventType.RESERVED) {
                        if (!output.contains(event2)) output.add(event2);
                        addedToOutput = true;
                    } else if (event2.getEventType() == EventType.DOABLE && event.getEventType() == EventType.RESERVED) {
                        if (!output.contains(event)) output.add(event);
                        addedToOutput = true;
                    } else if (!addedToOutput && !doableCollisions.contains(event2)) {
                        doableCollisions.add(event2);
                    }
                }
            }
            if (doableCollisions.isEmpty()) {
                if (!addedToOutput) output.add(event);
            } else {
                boolean alreadyInOutput = false;
                for (Event eventOutput : output) {
                    if (eventOutput.getTitle().equals(event.getTitle())
                            && eventOutput.getReservation().getCourseCode() == event.getReservation().getCourseCode()
                            && eventOutput.getEventDate().equals(event.getEventDate()))
                        alreadyInOutput = true;
                }

                if (!alreadyInOutput) {
                    StringBuilder sb = new StringBuilder();
                    for (Event collision : doableCollisions) {
                        boolean skipTeacher = collision.getTeacher() == null || sb.toString().contains(collision.getTeacher());
                        if (sb.length() != 0) {
                            if (!skipTeacher) sb.append(", ");
                        }
                        if (!skipTeacher) sb.append(collision.getTeacher());
                    }
                    Event newEvent = new Event(EventType.DOABLE);
                    newEvent.setTeacher(sb.toString().trim());
                    newEvent.setTitle(event.getTitle());
                    newEvent.setReservation(event.getReservation());
                    if (!output.contains(newEvent)) output.add(newEvent);
                }
            }
        }
        ClientHelper.orderEventByDate(output, true);
        return output;
    }

}
