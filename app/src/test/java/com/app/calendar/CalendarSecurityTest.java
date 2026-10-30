package com.app.calendar;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.MockMakers.PROXY;
import static org.mockito.Mockito.*;
class CalendarSecurityTest
{
    @Test
    void tokensAreStablePerSessionAndRejectMissingOrOtherSessionTokens()
    {
        HttpSession session = session();
        String token = CalendarSecurity.token(session);
        assertEquals(token, CalendarSecurity.token(session));
        HttpServletRequest request = mock(HttpServletRequest.class, withSettings().mockMaker(PROXY));
        when(request.getSession(false)).thenReturn(session);
        assertFalse(CalendarSecurity.valid(request));
        when(request.getParameter("csrf")).thenReturn(token);
        assertTrue(CalendarSecurity.valid(request));
        String otherToken = CalendarSecurity.token(session());
        when(request.getParameter("csrf")).thenReturn(otherToken);
        assertFalse(CalendarSecurity.valid(request));
        when(request.getSession(false)).thenReturn(null);
        assertFalse(CalendarSecurity.valid(request));
    }

    @Test
    void rejectsOversizedAndDuplicateInputsRatherThanTruncatingThem()
    {
        HttpServletRequest request = mock(HttpServletRequest.class, withSettings().mockMaker(PROXY));
        when(request.getParameter("title")).thenReturn("a".repeat(201));
        assertThrows(IllegalArgumentException.class, () -> CalendarEventInput.text(request, "title", "", 200));
        when(request.getParameterValues("title")).thenReturn(new String[]{"one", "two"});
        assertThrows(IllegalArgumentException.class, () -> CalendarEventInput.text(request, "title", "", 200));
    }

    private HttpSession session()
    {
        HttpSession session = mock(HttpSession.class, withSettings().mockMaker(PROXY));
        Map<String, Object> attributes = new HashMap<>();
        when(session.getAttribute(anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            attributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());
        return session;
    }

    /**
     * Parasoft Jtest UTA: Test for token(HttpSession)
     *
     * @see com.app.calendar.CalendarSecurity#token(HttpSession)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testToken() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        String getAttributeResult = "getAttributeResult"; // UTA: default value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        String result = CalendarSecurity.token(session);

        // Then - assertions for result of method token(HttpSession)
        assertEquals("getAttributeResult", result);

    }

}