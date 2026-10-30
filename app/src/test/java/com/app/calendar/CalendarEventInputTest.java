package com.app.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for CalendarEventInput
 *
 * @see com.app.calendar.CalendarEventInput
 * @author bmcmullin
 */
public class CalendarEventInputTest
{

    /**
     * Parasoft Jtest UTA: Test for parse(HttpServletRequest, String, CalendarEvent, boolean)
     *
     * @see com.app.calendar.CalendarEventInput#parse(HttpServletRequest, String, CalendarEvent, boolean)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testParse() throws Throwable
    {
        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        String getParameterResult4 = null; // UTA: configured value
        String getParameterResult5 = null; // UTA: configured value
        String getParameterResult6 = null; // UTA: configured value
        when(request.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3, getParameterResult4, getParameterResult5, getParameterResult6);

        String[] getParameterValuesResult = null; // UTA: configured value
        String[] getParameterValuesResult2 = null; // UTA: configured value
        String[] getParameterValuesResult3 = null; // UTA: configured value
        String[] getParameterValuesResult4 = null; // UTA: configured value
        String[] getParameterValuesResult5 = null; // UTA: configured value
        String[] getParameterValuesResult6 = null; // UTA: configured value
        String[] getParameterValuesResult7 = new String[1]; // UTA: default value
        getParameterValuesResult7[0] = "getParameterValuesResult7[0]"; // UTA: default value
        when(request.getParameterValues(nullable(String.class))).thenReturn(getParameterValuesResult, getParameterValuesResult2, getParameterValuesResult3, getParameterValuesResult4, getParameterValuesResult5, getParameterValuesResult6, getParameterValuesResult7);
        String owner = "owner"; // UTA: default value
        CalendarEvent previous = null; // UTA: configured value
        boolean singleOccurrence = true; // UTA: configured value
        CalendarEvent result = CalendarEventInput.parse(request, owner, previous, singleOccurrence);

    }

    /**
     * Parasoft Jtest UTA: Test for text(HttpServletRequest, String, String, int)
     *
     * @see com.app.calendar.CalendarEventInput#text(HttpServletRequest, String, String, int)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testText() throws Throwable
    {
        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(request.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        String[] getParameterValuesResult = null; // UTA: configured value
        when(request.getParameterValues(nullable(String.class))).thenReturn(getParameterValuesResult);
        String name = "name"; // UTA: default value
        String fallback = "fallback"; // UTA: default value
        int maximum = 1; // UTA: default value
        String result = CalendarEventInput.text(request, name, fallback, maximum);

        // Then - assertions for result of method text(HttpServletRequest, String, String, int)
        assertEquals("fallback", result);

    }

    /**
     * Parasoft Jtest UTA: Test for text(HttpServletRequest, String, String, int)
     *
     * @see com.app.calendar.CalendarEventInput#text(HttpServletRequest, String, String, int)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testText2() throws Throwable
    {
        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        String getParameterResult = ""; // UTA: configured value
        when(request.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        String[] getParameterValuesResult = null; // UTA: configured value
        when(request.getParameterValues(nullable(String.class))).thenReturn(getParameterValuesResult);
        String name = "name"; // UTA: default value
        String fallback = "fallback"; // UTA: default value
        int maximum = 0; // UTA: configured value
        String result = CalendarEventInput.text(request, name, fallback, maximum);

        // Then - assertions for result of method text(HttpServletRequest, String, String, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for text(HttpServletRequest, String, String, int)
     *
     * @see com.app.calendar.CalendarEventInput#text(HttpServletRequest, String, String, int)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testText3() throws Throwable
    {
        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        String getParameterResult = "*"; // UTA: configured value
        when(request.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        String[] getParameterValuesResult = null; // UTA: configured value
        when(request.getParameterValues(nullable(String.class))).thenReturn(getParameterValuesResult);
        String name = "name"; // UTA: default value
        String fallback = "fallback"; // UTA: default value
        int maximum = 0; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            CalendarEventInput.text(request, name, fallback, maximum);
        });

    }

}
