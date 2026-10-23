package com.app.calendar;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public record CalendarEvent(long id, String username, String uid, String title, String description,
        LocalDateTime start, LocalDateTime end, String zoneId, boolean allDay,
        String recurrenceRule, int reminderMinutes, long version) implements Serializable {
    private static final String EMPTY_TEXT = "";
    private static final String TEXT_INVALID_CALENDAR_OWNER_OR_EVENT_IDENTIFIER = "Invalid calendar owner or event identifier";
    private static final String TEXT_TITLE_MUST_CONTAIN_1_TO_200_CHARACTERS_WITHOUT_CONTROL_CHARACTERS = "Title must contain 1 to 200 characters without control characters";
    private static final String TEXT_DESCRIPTION_CANNOT_EXCEED_5000_CHARACTERS = "Description cannot exceed 5000 characters";
    private static final String TEXT_EVENTS_NEED_A_POSITIVE_DURATION_OF_AT_MOST_366_DAYS_WITHIN_1900_T = "Events need a positive duration of at most 366 days, within 1900 to 9998";
    private static final String TEXT_ALL_DAY_EVENTS_MUST_USE_WHOLE_CALENDAR_DAYS = "All-day events must use whole calendar days";
    private static final String TEXT_CHOOSE_VALID_TIMES_OUTSIDE_DAYLIGHT_SAVING_CLOCK_GAPS = "Choose valid times outside daylight-saving clock gaps";
    private static final String TEXT_INVALID_RECURRENCE_OR_REMINDER_REMINDERS_SUPPORT_AT_MOST_SEVEN_DA = "Invalid recurrence or reminder; reminders support at most seven days";


    public CalendarEvent {
        username = Objects.requireNonNull(username);
        uid = Objects.requireNonNull(uid);
        start = Objects.requireNonNull(start);
        end = Objects.requireNonNull(end);
        title = title == null ? CalendarEvent.EMPTY_TEXT : title.strip();
        description = description == null ? CalendarEvent.EMPTY_TEXT : description;
        recurrenceRule = recurrenceRule == null ? CalendarEvent.EMPTY_TEXT : recurrenceRule;
        if (username.isBlank() || username.length() > 100 || uid.isBlank() || uid.length() > 255) {
            throw new IllegalArgumentException(CalendarEvent.TEXT_INVALID_CALENDAR_OWNER_OR_EVENT_IDENTIFIER);
        }
        if (title.isEmpty() || title.length() > 200 || title.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(CalendarEvent.TEXT_TITLE_MUST_CONTAIN_1_TO_200_CHARACTERS_WITHOUT_CONTROL_CHARACTERS);
        }
        if (description.length() > 5000) {
            throw new IllegalArgumentException(CalendarEvent.TEXT_DESCRIPTION_CANNOT_EXCEED_5000_CHARACTERS);
        }
        start = start.withNano(0);
        end = end.withNano(0);
        ZoneId zone = ZoneId.of(zoneId);
        if (start.getYear() < 1900 || end.getYear() > 9998 || !end.isAfter(start)
                || end.isAfter(start.plusDays(366))) {
            throw new IllegalArgumentException(CalendarEvent.TEXT_EVENTS_NEED_A_POSITIVE_DURATION_OF_AT_MOST_366_DAYS_WITHIN_1900_T);
        }
        if (allDay && (!start.toLocalTime().equals(java.time.LocalTime.MIDNIGHT)
                || !end.toLocalTime().equals(java.time.LocalTime.MIDNIGHT))) {
            throw new IllegalArgumentException(CalendarEvent.TEXT_ALL_DAY_EVENTS_MUST_USE_WHOLE_CALENDAR_DAYS);
        }
        if (!allDay && (zone.getRules().getValidOffsets(start).isEmpty()
                || zone.getRules().getValidOffsets(end).isEmpty()
                || !end.atZone(zone).toInstant().isAfter(start.atZone(zone).toInstant()))) {
            throw new IllegalArgumentException(CalendarEvent.TEXT_CHOOSE_VALID_TIMES_OUTSIDE_DAYLIGHT_SAVING_CLOCK_GAPS);
        }
        if (recurrenceRule.length() > 256 || reminderMinutes < -1 || reminderMinutes > 10080) {
            throw new IllegalArgumentException(CalendarEvent.TEXT_INVALID_RECURRENCE_OR_REMINDER_REMINDERS_SUPPORT_AT_MOST_SEVEN_DA);
        }
    }
}