package com.app.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Parasoft Jtest UTA: Test class for CalendarPreferences
 *
 * @see com.app.calendar.CalendarPreferences
 * @author bmcmullin
 */
public class CalendarPreferencesTest
{

    /**
     * Parasoft Jtest UTA: Test for defaults()
     *
     * @see com.app.calendar.CalendarPreferences#defaults()
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testDefaults() throws Throwable
    {
        // When
        CalendarPreferences result = CalendarPreferences.defaults();

        // Then - assertions for result of method defaults()
        assertNotNull(result);

    }

}
