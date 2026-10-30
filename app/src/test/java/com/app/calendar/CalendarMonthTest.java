package com.app.calendar;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
class CalendarMonthTest
{

    @Test
    void defaultsToCurrentMonthAndMarksOnlyToday()
    {
        CalendarMonth calendar = CalendarMonth.from(null, LocalDate.of(2026, 10, 23));
        assertEquals(YearMonth.of(2026, 10), calendar.month());
        assertEquals("October 2026", calendar.label());
        assertEquals(4, calendar.leadingDays());
        assertEquals(31, calendar.dayCount());
        assertTrue(calendar.isToday(23));
        assertFalse(calendar.isToday(22));
    }

    @Test
    void supportsLeapYearsAndSundayStarts()
    {
        LocalDate today = LocalDate.of(2026, 10, 23);
        assertEquals(29, CalendarMonth.from("2024-02", today).dayCount());
        assertEquals(28, CalendarMonth.from("2025-02", today).dayCount());
        assertEquals(0, CalendarMonth.from("2026-02", today).leadingDays());
        assertFalse(CalendarMonth.from("2024-02", today).isToday(23));
    }

    @Test
    void handlesYearTransitionsAndRejectsInvalidMonths()
    {
        LocalDate today = LocalDate.of(2026, 1, 1);
        CalendarMonth calendar = CalendarMonth.from("2026-01", today);
        assertEquals("2025-12", calendar.month().minusMonths(1).toString());
        assertEquals("2027-01", CalendarMonth.from("2026-12", today).month().plusMonths(1).toString());
        assertThrows(DateTimeParseException.class, () -> CalendarMonth.from("2026-13", today));
        assertThrows(DateTimeParseException.class, () -> CalendarMonth.from("<script>", today));
        assertThrows(IllegalArgumentException.class, () -> CalendarMonth.from("0000-01", today));
    }

}