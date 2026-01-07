package com.example.app.calculator;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for CalculatorUIServlet
 *
 * @see com.example.app.calculator.CalculatorUIServlet
 * @author bmcmullin
 */
public class CalculatorUIServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.calculator.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = "add"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.example.app.calculator.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.example.app.calculator.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.example.app.calculator.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.example.app.calculator.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.example.app.calculator.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.example.app.calculator.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
