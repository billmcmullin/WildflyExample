package com.app.calendar;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import com.app.calendar.CalendarStore.AuditEntry;
import com.app.calendar.CalendarStore.Notification;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for CalendarPage
 *
 * @see com.app.calendar.CalendarPage
 * @author bmcmullin
 */
public class CalendarPageTest
{

    /**
     * Parasoft Jtest UTA: Test for esc(String)
     *
     * @see com.app.calendar.CalendarPage#esc(String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testEsc() throws Throwable
    {
        // When
        String value = null; // UTA: configured value
        String result = CalendarPage.esc(value);

        // Then - assertions for result of method esc(String)
        assertEquals("", result);

    }

}
