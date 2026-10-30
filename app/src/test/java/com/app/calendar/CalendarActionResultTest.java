package com.app.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import static org.junit.jupiter.api.Assertions.assertFalse;
/**
 * Parasoft Jtest UTA: Test class for CalendarActionResult
 *
 * @see com.app.calendar.CalendarActionResult
 * @author bmcmullin
 */
public class CalendarActionResultTest
{

    /**
     * Parasoft Jtest UTA: Test for equals(Object)
     *
     * @see com.app.calendar.CalendarActionResult#equals(Object)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testEquals() throws Throwable
    {
        // Given
        String notice = "notice"; // UTA: default value
        String view = "view"; // UTA: default value
        CalendarActionResult underTest = new CalendarActionResult(notice, view);

        // When
        Object arg0 = new Object(); // UTA: default value
        boolean result = underTest.equals(arg0);

        // Then - assertions for result of method equals(Object)
        assertFalse(result);

    }

}
