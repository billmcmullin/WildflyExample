package com.app.util;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
/**
 * Parasoft Jtest UTA: Test class for HomeServlet
 *
 * @see com.example.app.util.HomeServlet
 * @author bmcmullin
 */
public class HomeServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);

        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

}
