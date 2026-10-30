package com.app.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import com.app.calendar.CalendarRecurrence.Occurrence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class CalendarRecurrenceTest
{

    @Test
    void expandsMonthlyRecurrenceWithoutInventingMonthEndDates()
    {
        CalendarEvent event = event("2026-01-31T10:00", "FREQ=MONTHLY;COUNT=3");
        List<CalendarRecurrence.Occurrence> dates = CalendarRecurrence.expand(event, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), ZoneId.of("UTC"), Set.of());
        assertEquals(List.of(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 5, 31)), dates.stream().map(occurrence -> occurrence.start().toLocalDate()).toList());
    }

    @Test
    void respectsExceptionsAndPreservesWallClockAcrossDaylightSaving()
    {
        CalendarEvent event = event("2026-03-07T10:00", "FREQ=DAILY;COUNT=3");
        List<CalendarRecurrence.Occurrence> dates = CalendarRecurrence.expand(event, LocalDate.of(2026, 3, 7), LocalDate.of(2026, 3, 10), ZoneId.of("UTC"), Set.of(LocalDateTime.of(2026, 3, 8, 10, 0)));
        assertEquals(2, dates.size());
        assertEquals(15, dates.get(0).start().getHour());
        assertEquals(14, dates.get(1).start().getHour());
    }

    @Test
    void rejectsUnboundedAndUnsupportedRulesAndSearchRanges()
    {
        assertEquals("FREQ=DAILY;COUNT=3", CalendarRecurrence.canonicalize("FREQ=DAILY;COUNT=3"));
        assertThrows(IllegalArgumentException.class, () -> CalendarRecurrence.canonicalize("\uFF26REQ=DAILY;COUNT=3"));
        assertThrows(IllegalArgumentException.class, () -> CalendarRecurrence.validate("FREQ=DAILY", LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> CalendarRecurrence.validate("FREQ=HOURLY;COUNT=3", LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> CalendarRecurrence.validate("FREQ=DAILY;COUNT=1000000", LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> CalendarRecurrence.expand(event("2026-01-01T10:00", ""), LocalDate.of(2026, 1, 1), LocalDate.of(2028, 1, 1), ZoneId.of("UTC"), Set.of()));
    }

    private CalendarEvent event(String start, String rule)
    {
        LocalDateTime date = LocalDateTime.parse(start);
        return new CalendarEvent(1, "alice", "uid", "Meeting", "", date, date.plusHours(1), "America/New_York", false, rule, -1, 1);
    }

    /**
     * Parasoft Jtest UTA: Test for parts(String)
     *
     * @see com.app.calendar.CalendarRecurrence#parts(String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testParts() throws Throwable
    {
        // When
        String rule = "*"; // UTA: configured value
        Map<String, String> result = CalendarRecurrence.parts(rule);

    }

}