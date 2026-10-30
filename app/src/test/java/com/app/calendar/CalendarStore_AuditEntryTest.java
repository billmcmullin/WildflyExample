package com.app.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import com.app.calendar.CalendarStore.AuditEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for AuditEntry
 *
 * @see com.app.calendar.CalendarStore.AuditEntry
 * @author bmcmullin
 */
public class CalendarStore_AuditEntryTest
{

    /**
     * Parasoft Jtest UTA: Test for action()
     *
     * @see com.app.calendar.CalendarStore.AuditEntry#action()
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testAction() throws Throwable
    {
        // Given
        long id = 1L; // UTA: default value
        long eventId = 1L; // UTA: default value
        String action = "action"; // UTA: default value
        String title = "title"; // UTA: default value
        String details = "details"; // UTA: default value
        long changedMillis = 1L; // UTA: default value
        AuditEntry underTest = new AuditEntry(id, eventId, action, title, details, changedMillis);

        // When
        String result = underTest.action();

        // Then - assertions for result of method action()
        assertEquals("action", result);

    }

}
