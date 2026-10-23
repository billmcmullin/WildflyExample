package com.app.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

final class CalendarEventInput {
    private static final String FIELD_ON = "on";
    private static final String FIELD_ALL_DAY = "allDay";
    private static final String EMPTY_TEXT = "";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_END_DATE = "endDate";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_END_TIME = "endTime";
    private static final String FIELD_COUNT = "count";
    private static final String FIELD_END_MODE = "endMode";
    private static final String FIELD_FREQUENCY = "frequency";
    private static final String TEXT_NONE = "NONE";
    private static final String FIELD_UNTIL = "until";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_ZONE_ID = "zoneId";
    private static final String TEXT_UTC = "UTC";
    private static final String FIELD_REMINDER = "reminder";
    private static final String TEXT_1 = "-1";
    private static final String FIELD_VERSION = "version";
    private static final String TEXT_DUPLICATE_FORM_FIELD = "Duplicate form field: ";
    private static final String TEXT_FIELD_IS_TOO_LONG = "Field is too long: ";

    private CalendarEventInput() { }

    static CalendarEvent parse(HttpServletRequest request, String owner, CalendarEvent previous, boolean singleOccurrence) {
        boolean allDay = CalendarEventInput.FIELD_ON.equals(text(request, CalendarEventInput.FIELD_ALL_DAY, CalendarEventInput.EMPTY_TEXT, 8));
        LocalDate startDate = LocalDate.parse(text(request, CalendarEventInput.FIELD_START_DATE, CalendarEventInput.EMPTY_TEXT, 10));
        LocalDate endDate = LocalDate.parse(text(request, CalendarEventInput.FIELD_END_DATE, CalendarEventInput.EMPTY_TEXT, 10));
        LocalDateTime start = startDate.atTime(allDay ? LocalTime.MIDNIGHT
                : LocalTime.parse(text(request, CalendarEventInput.FIELD_START_TIME, CalendarEventInput.EMPTY_TEXT, 8)));
        LocalDateTime end = allDay ? endDate.plusDays(1).atStartOfDay()
                : endDate.atTime(LocalTime.parse(text(request, CalendarEventInput.FIELD_END_TIME, CalendarEventInput.EMPTY_TEXT, 8)));
        boolean byCount = CalendarEventInput.FIELD_COUNT.equals(text(request, CalendarEventInput.FIELD_END_MODE, CalendarEventInput.FIELD_COUNT, 10));
        String recurrence = singleOccurrence ? CalendarEventInput.EMPTY_TEXT : CalendarRecurrence.rule(text(request, CalendarEventInput.FIELD_FREQUENCY, CalendarEventInput.TEXT_NONE, 16),
                byCount ? text(request, CalendarEventInput.FIELD_COUNT, CalendarEventInput.EMPTY_TEXT, 4) : CalendarEventInput.EMPTY_TEXT, byCount ? CalendarEventInput.EMPTY_TEXT : text(request, CalendarEventInput.FIELD_UNTIL, CalendarEventInput.EMPTY_TEXT, 10), start);
        return new CalendarEvent(previous == null || singleOccurrence ? 0 : previous.id(), owner,
                previous == null || singleOccurrence ? UUID.randomUUID().toString() : previous.uid(),
                text(request, CalendarEventInput.FIELD_TITLE, CalendarEventInput.EMPTY_TEXT, 200), text(request, CalendarEventInput.FIELD_DESCRIPTION, CalendarEventInput.EMPTY_TEXT, 5000), start, end,
                text(request, CalendarEventInput.FIELD_ZONE_ID, CalendarEventInput.TEXT_UTC, 100), allDay, recurrence,
                Integer.parseInt(text(request, CalendarEventInput.FIELD_REMINDER, CalendarEventInput.TEXT_1, 6)),
                previous == null || singleOccurrence ? 1 : Long.parseLong(text(request, CalendarEventInput.FIELD_VERSION, CalendarEventInput.EMPTY_TEXT, 20)));
    }

    static String text(HttpServletRequest request, String name, String fallback, int maximum) {
        String[] values = request.getParameterValues(name);
        if (values != null && values.length > 1) {
            throw new IllegalArgumentException(CalendarEventInput.TEXT_DUPLICATE_FORM_FIELD + name);
        }
        String value = request.getParameter(name);
        if (value == null) {
            return fallback;
        }
        if (value.length() > maximum) {
            throw new IllegalArgumentException(CalendarEventInput.TEXT_FIELD_IS_TOO_LONG + name);
        }
        return value;
    }
}