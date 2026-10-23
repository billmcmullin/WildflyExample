package com.app.calendar;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CalendarEventTest {

    @Test
    void validatesTitleDurationAndReminderBounds() {
        assertEquals("Meeting", event(" Meeting ", "2026-10-23T10:00", "2026-10-23T11:00", "UTC", false, 15).title());
        assertThrows(IllegalArgumentException.class, () -> event("", "2026-10-23T10:00", "2026-10-23T11:00", "UTC", false, 15));
        assertThrows(IllegalArgumentException.class, () -> event("Meeting", "2026-10-23T10:00", "2026-10-23T09:00", "UTC", false, 15));
        assertThrows(IllegalArgumentException.class, () -> event("Meeting", "2026-10-23T10:00", "2026-10-23T11:00", "UTC", false, 10081));
    }

    @Test
    void rejectsDaylightSavingGapsAndPartialAllDayEvents() {
        assertThrows(IllegalArgumentException.class, () -> event("Meeting", "2026-03-08T02:30", "2026-03-08T03:30", "America/New_York", false, -1));
        assertThrows(IllegalArgumentException.class, () -> event("Holiday", "2026-10-23T10:00", "2026-10-24T00:00", "UTC", true, -1));
        assertTrue(event("Holiday", "2026-10-23T00:00", "2026-10-24T00:00", "UTC", true, -1).allDay());
    }

    @Test
    void validatesPreferences() {
        assertEquals("agenda", new CalendarPreferences("Europe/London", 1, "agenda").defaultView());
        assertThrows(IllegalArgumentException.class, () -> new CalendarPreferences("UTC", 3, "month"));
        assertThrows(IllegalArgumentException.class, () -> new CalendarPreferences("UTC", 7, "other"));
    }

    private CalendarEvent event(String title, String start, String end, String zone, boolean allDay, int reminder) {
        return new CalendarEvent(0, "alice", "test-uid", title, "", LocalDateTime.parse(start),
                LocalDateTime.parse(end), zone, allDay, "", reminder, 1);
    }
}