package com.example.app.game.numberguess;

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
 * Parasoft Jtest UTA: Test class for NumberGuessServlet
 *
 * @see com.example.app.game.numberguess.NumberGuessServlet
 * @author bmcmullin
 */
public class NumberGuessServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Integer getAttributeResult = 1; // UTA: default value
        Integer getAttributeResult2 = 1; // UTA: default value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = null; // UTA: configured value
        Integer getAttributeResult5 = null; // UTA: configured value
        Integer getAttributeResult6 = null; // UTA: configured value
        Integer getAttributeResult7 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Integer getAttributeResult = 1; // UTA: default value
        Integer getAttributeResult2 = 1; // UTA: default value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = null; // UTA: configured value
        String getAttributeResult5 = "getAttributeResult5"; // UTA: default value
        Integer getAttributeResult6 = null; // UTA: configured value
        Integer getAttributeResult7 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Integer getAttributeResult = 1; // UTA: default value
        Integer getAttributeResult2 = 1; // UTA: default value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = null; // UTA: configured value
        Integer getAttributeResult5 = null; // UTA: configured value
        Integer getAttributeResult6 = null; // UTA: configured value
        Integer getAttributeResult7 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Integer getAttributeResult = 1; // UTA: default value
        Integer getAttributeResult2 = 1; // UTA: default value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = null; // UTA: configured value
        Integer getAttributeResult5 = null; // UTA: configured value
        Integer getAttributeResult6 = null; // UTA: configured value
        String getAttributeResult7 = "getAttributeResult7"; // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Integer getAttributeResult = 1; // UTA: default value
        Integer getAttributeResult2 = 1; // UTA: default value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = null; // UTA: configured value
        Integer getAttributeResult5 = null; // UTA: configured value
        Integer getAttributeResult6 = null; // UTA: configured value
        Integer getAttributeResult7 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult8 = null; // UTA: configured value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult8);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = ""; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        Integer getAttributeResult2 = 1; // UTA: default value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = 1; // UTA: default value
        Object getAttributeResult5 = null; // UTA: configured value
        Object getAttributeResult6 = null; // UTA: configured value
        Object getAttributeResult7 = null; // UTA: configured value
        Object getAttributeResult8 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7, getAttributeResult8);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);

        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Integer getAttributeResult2 = 1; // UTA: default value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = 1; // UTA: default value
        Integer getAttributeResult5 = null; // UTA: configured value
        Integer getAttributeResult6 = null; // UTA: configured value
        Integer getAttributeResult7 = null; // UTA: configured value
        Integer getAttributeResult8 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7, getAttributeResult8);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Integer getAttributeResult = 1; // UTA: default value
        Integer getAttributeResult2 = 1; // UTA: default value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = null; // UTA: configured value
        Integer getAttributeResult5 = null; // UTA: configured value
        Integer getAttributeResult6 = 1; // UTA: default value
        Boolean getAttributeResult7 = false; // UTA: default value
        Integer getAttributeResult8 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7, getAttributeResult8);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = ""; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = 1; // UTA: default value
        Integer getAttributeResult5 = 1; // UTA: default value
        Integer getAttributeResult6 = 1; // UTA: default value
        Object getAttributeResult7 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.game.numberguess.NumberGuessServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = ""; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = 1; // UTA: default value
        Integer getAttributeResult5 = 1; // UTA: default value
        Integer getAttributeResult6 = 1; // UTA: default value
        Object getAttributeResult7 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

}
