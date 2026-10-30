package com.app.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import com.app.calendar.CalendarStore.ImportResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for ImportResult
 *
 * @see com.app.calendar.CalendarStore.ImportResult
 * @author bmcmullin
 */
public class CalendarStore_ImportResultTest
{

    /**
     * Parasoft Jtest UTA: Test for added()
     *
     * @see com.app.calendar.CalendarStore.ImportResult#added()
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testAdded() throws Throwable
    {
        // Given
        int added = 1; // UTA: default value
        int skipped = 1; // UTA: default value
        ImportResult underTest = new ImportResult(added, skipped);

        // When
        int result = underTest.added();

        // Then - assertions for result of method added()
        assertEquals(1, result);

    }

}
