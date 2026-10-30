package com.app.calendar;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for CalendarActionsServlet
 *
 * @see com.app.calendar.CalendarActionsServlet
 * @author bmcmullin
 */
public class CalendarActionsServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.app.calendar.CalendarActionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testDoPost() throws Throwable
    {
        // Given
        CalendarActionsServlet underTest = new CalendarActionsServlet();

        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(request.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse response = mock(HttpServletResponse.class);
        underTest.doPost(request, response);

    }

}
