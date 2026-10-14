package com.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

class RequestParamContextTest {

    @Test
    void valueUsesParameterValuesAndSanitizes() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("/app");
        when(req.getParameterValues("name")).thenReturn(new String[] { "  a\n\rb\t  " });

        RequestParamContext context = new RequestParamContext(req);
        assertEquals("ab", context.value("name"));
    }

    @Test
    void valueFallsBackToGetParameter() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("");
        when(req.getParameterValues("k")).thenReturn(null);
        when(req.getParameter("k")).thenReturn("  value  ");

        RequestParamContext context = new RequestParamContext(req);
        assertEquals("value", context.value("k"));
    }

    @Test
    void valueTruncatesLongInputAndRejectsBlankNames() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("");
        String longValue = "x".repeat(3000);
        when(req.getParameterValues("long")).thenReturn(new String[] { longValue });

        RequestParamContext context = new RequestParamContext(req);
        assertEquals(2048, context.value("long").length());
        assertNull(context.value(" "));
        assertNull(context.value(null));
    }

    @Test
    void appPathBuildsExpectedPaths() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("/app");

        RequestParamContext context = new RequestParamContext(req);
        assertEquals("/app/home", context.appPath("home"));
        assertEquals("/app/home", context.appPath("/home"));
        assertEquals("/app", context.appPath(""));
    }

    @Test
    void localRedirectOrAcceptsOnlySafeLocalPaths() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("/app");

        RequestParamContext context = new RequestParamContext(req);
        assertEquals("/app/home", context.localRedirectOr("/app/home", "/fallback"));
        assertEquals("/fallback", context.localRedirectOr("https://evil.test", "/fallback"));
        assertEquals("/fallback", context.localRedirectOr("//evil", "/fallback"));
    }

    @Test
    void contextPathDefaultsToEmpty() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn(null);

        RequestParamContext context = new RequestParamContext(req);
        assertTrue(context.contextPath().isEmpty());
        assertEquals("/", context.appPath(null));
    }
}
