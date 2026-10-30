package com.app.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class RequestParamContextTest
{

    @Test
    void valueUsesParameterValuesAndSanitizes()
    {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("/app");
        when(req.getParameterValues("name")).thenReturn(new String[]{"  a\n\rb\t  "});

        RequestParamContext context = new RequestParamContext(req);
        assertEquals("ab", context.value("name"));
    }

    @Test
    void valueFallsBackToGetParameter()
    {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("");
        when(req.getParameterValues("k")).thenReturn(null);
        when(req.getParameter("k")).thenReturn("  value  ");

        RequestParamContext context = new RequestParamContext(req);
        assertEquals("value", context.value("k"));
    }

    @Test
    void valueTruncatesLongInputAndRejectsBlankNames()
    {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("");
        String longValue = "x".repeat(3000);
        when(req.getParameterValues("long")).thenReturn(new String[]{longValue});

        RequestParamContext context = new RequestParamContext(req);
        assertEquals(2048, context.value("long").length());
        assertNull(context.value(" "));
        assertNull(context.value(null));
    }

    @Test
    void appPathBuildsExpectedPaths()
    {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("/app");

        RequestParamContext context = new RequestParamContext(req);
        assertEquals("/app/home", context.appPath("home"));
        assertEquals("/app/home", context.appPath("/home"));
        assertEquals("/app", context.appPath(""));
    }

    @Test
    void localRedirectOrAcceptsOnlySafeLocalPaths()
    {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("/app");

        RequestParamContext context = new RequestParamContext(req);
        assertEquals("/app/home", context.localRedirectOr("/app/home", "/fallback"));
        assertEquals("/fallback", context.localRedirectOr("https://evil.test", "/fallback"));
        assertEquals("/fallback", context.localRedirectOr("//evil", "/fallback"));
    }

    @Test
    void contextPathDefaultsToEmpty()
    {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn(null);

        RequestParamContext context = new RequestParamContext(req);
        assertTrue(context.contextPath().isEmpty());
        assertEquals("/", context.appPath(null));
    }

    /**
     * Parasoft Jtest UTA: Test for localRedirectOr(String, String)
     *
     * @see com.app.util.RequestParamContext#localRedirectOr(String, String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testLocalRedirectOr() throws Throwable
    {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(request.getContextPath()).thenReturn(getContextPathResult);
        RequestParamContext underTest = new RequestParamContext(request);

        // When
        String candidate = null; // UTA: configured value
        String fallback = "fallback"; // UTA: default value
        String result = underTest.localRedirectOr(candidate, fallback);

        // Then - assertions for result of method localRedirectOr(String, String)
        assertEquals("fallback", result);

        // Then - assertions for this instance of RequestParamContext
        assertEquals("getContextPathResult", underTest.contextPath());

    }

    /**
     * Parasoft Jtest UTA: Test for value(String)
     *
     * @see com.app.util.RequestParamContext#value(String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testValue() throws Throwable
    {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(request.getContextPath()).thenReturn(getContextPathResult);
        RequestParamContext underTest = new RequestParamContext(request);

        // When
        String name = "name"; // UTA: configured value
        String result = underTest.value(name);

        // Then - assertions for result of method value(String)
        assertNull(result);

        // Then - assertions for this instance of RequestParamContext
        assertEquals("getContextPathResult", underTest.contextPath());

    }

    /**
     * Parasoft Jtest UTA: Test for value(String)
     *
     * @see com.app.util.RequestParamContext#value(String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testValue2() throws Throwable
    {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(request.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = ""; // UTA: configured value
        when(request.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        String[] getParameterValuesResult = null; // UTA: configured value
        when(request.getParameterValues(nullable(String.class))).thenReturn(getParameterValuesResult);
        RequestParamContext underTest = new RequestParamContext(request);

        // When
        String name = "name"; // UTA: configured value
        String result = underTest.value(name);

        // Then - assertions for result of method value(String)
        assertNull(result);

        // Then - assertions for this instance of RequestParamContext
        assertEquals("getContextPathResult", underTest.contextPath());

    }

}
