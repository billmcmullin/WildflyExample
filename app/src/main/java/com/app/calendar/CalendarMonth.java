package com.app.calendar;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

record CalendarMonth(YearMonth month, LocalDate today) {
    private static final String TEXT_MONTH_MUST_BE_BETWEEN_0001_01_AND_9999_12 = "Month must be between 0001-01 and 9999-12";
    private static final String TEXT_MMMM_UUUU = "MMMM uuuu";


    static CalendarMonth from(String requestedMonth, LocalDate today) {
        YearMonth month = requestedMonth == null ? YearMonth.from(today) : YearMonth.parse(requestedMonth);
        if (month.getYear() < 1 || month.getYear() > 9999) {
            throw new IllegalArgumentException(CalendarMonth.TEXT_MONTH_MUST_BE_BETWEEN_0001_01_AND_9999_12);
        }
        return new CalendarMonth(month, today);
    }

    String label() {
        return month.format(DateTimeFormatter.ofPattern(CalendarMonth.TEXT_MMMM_UUUU, Locale.ENGLISH));
    }

    int leadingDays() {
        return month.atDay(1).getDayOfWeek().getValue() % 7;
    }

    int dayCount() {
        return month.lengthOfMonth();
    }

    boolean isToday(int day) {
        return month.atDay(day).equals(today);
    }
}