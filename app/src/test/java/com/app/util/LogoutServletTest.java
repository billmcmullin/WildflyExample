package com.app.util;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for LogoutServlet
 *
 * @see com.example.app.util.LogoutServlet
 * @author bmcmullin
 */
public class LogoutServletTest
{

    @Test
    public void signOutRedirectPreservesDeploymentContext() throws Exception
    {
        for (String contextPath : new String[]{"/app", "", "/custom"}) {
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = mock(HttpSession.class);
            when(req.getContextPath()).thenReturn(contextPath);
            when(req.getSession(false)).thenReturn(session);

            new LogoutServlet().doGet(req, resp);

            verify(session).invalidate();
            verify(resp).sendRedirect(contextPath + "/login");
        }
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        Cookie[] getCookiesResult = null; // UTA: configured value
        when(req.getCookies()).thenReturn(getCookiesResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        Cookie[] getCookiesResult = null; // UTA: configured value
        when(req.getCookies()).thenReturn(getCookiesResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = ""; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "*"; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = ""; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "*"; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet10() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = ""; // UTA: configured value
        String getContextPathResult2 = "getContextPathResult2"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult, getContextPathResult2);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet11() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet12() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testDoGet13() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        Cookie[] getCookiesResult = null; // UTA: configured value
        when(req.getCookies()).thenReturn(getCookiesResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.app.util.LogoutServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testDoGet14() throws Throwable
    {
        // Given
        LogoutServlet underTest = new LogoutServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        Cookie[] getCookiesResult = null; // UTA: configured value
        when(req.getCookies()).thenReturn(getCookiesResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        doThrow(IOException.class).when(resp).sendError(anyInt(), nullable(String.class));

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

}
