package com.app.calendar;

import java.time.ZoneId;

public record CalendarPreferences(String zoneId, int weekStart, String defaultView) {
    private static final String TEXT_WEEK_MUST_START_ON_MONDAY_OR_SUNDAY = "Week must start on Monday or Sunday";
    private static final String FIELD_MONTH = "month";
    private static final String FIELD_AGENDA = "agenda";
    private static final String TEXT_CHOOSE_MONTH_OR_AGENDA_AS_THE_DEFAULT_VIEW = "Choose Month or Agenda as the default view";


    public CalendarPreferences {
        ZoneId.of(zoneId);
        if (weekStart != 1 && weekStart != 7) {
            throw new IllegalArgumentException(CalendarPreferences.TEXT_WEEK_MUST_START_ON_MONDAY_OR_SUNDAY);
        }
        if (!CalendarPreferences.FIELD_MONTH.equals(defaultView) && !CalendarPreferences.FIELD_AGENDA.equals(defaultView)) {
            throw new IllegalArgumentException(CalendarPreferences.TEXT_CHOOSE_MONTH_OR_AGENDA_AS_THE_DEFAULT_VIEW);
        }
    }

    public static CalendarPreferences defaults() {
        return new CalendarPreferences(ZoneId.systemDefault().getId(), 7, CalendarPreferences.FIELD_MONTH);
    }
}