package com.example.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * Updated unit tests for NumberGuessServlet using mocked templates and a simple in-memory session map.
 */
public class NumberGuessServletTest
{

    private NumberGuessServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private ServletContext ctx;
    private HttpSession session;
    private Map<String, Object> sessionAttrs;
    private StringWriter sw;
    private PrintWriter pw;
    private final String template = "<!doctype html><html><body>" + "<h1>Number Guess</h1>" + "<form method=\"post\" action=\"{{ctx}}/numberguess\">" + "<input name=\"guess\" value=\"{{guess_value}}\"/>" + "<button type=\"submit\">Submit</button></form>" + "<div>{{message}}</div>" + "<div>{{candidates}}</div>" + "<div>{{history}}</div>" + "</body></html>";

    @BeforeEach
    public void setUp() throws Exception
    {
        servlet = new NumberGuessServlet();

        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        ctx = mock(ServletContext.class);
        session = mock(HttpSession.class);

        // Simple in-memory session attribute store so servlet set/getAttribute works
        sessionAttrs = new HashMap<>();
        when(session.getAttribute(anyString())).thenAnswer(inv -> sessionAttrs.get(inv.getArgument(0)));
        doAnswer(inv -> {
            sessionAttrs.put((String) inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());
        doAnswer(inv -> {
            sessionAttrs.remove(inv.getArgument(0));
            return null;
        }).when(session).removeAttribute(anyString());

        when(req.getSession(anyBoolean())).thenReturn(session);
        when(req.getServletContext()).thenReturn(ctx);

        // Provide the minimal template via servletContext
        ByteArrayInputStream is = new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8));
        when(ctx.getResourceAsStream("/WEB-INF/templates/numberguess.html")).thenReturn(is);

        // Provide writer to capture servlet output
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        // Default context path
        when(req.getContextPath()).thenReturn("/app");
    }

    @Test
    public void testDoGet_startsNewGame_whenNewParam() throws Exception
    {
        // Simulate request to start a new game
        when(req.getParameter("new")).thenReturn("1");

        servlet.doGet(req, resp);
        pw.flush();

        // Verify output contains expected bits from template and context path
        String out = sw.toString();
        assertTrue(out.contains("Number Guess"));
        assertTrue(out.contains("action=\"/app/numberguess\""));

        // Verify session initialized with expected attributes
        assertNotNull(sessionAttrs.get("ng.secret"));
        assertEquals(1, sessionAttrs.get("ng.low"));   // initial low should be 1
        assertEquals(100, sessionAttrs.get("ng.high")); // initial high should be 100
        assertEquals(10, sessionAttrs.get("ng.remaining"));
    }

    @Test
    public void testDoPost_validGuess_updatesBoundsAndHistory_forTooLow() throws Exception
    {
        // Initialize session with known secret so behavior is deterministic
        sessionAttrs.put("ng.secret", 50);
        sessionAttrs.put("ng.low", 1);
        sessionAttrs.put("ng.high", 100);
        sessionAttrs.put("ng.remaining", 10);
        sessionAttrs.put("ng.history", new java.util.ArrayList<String>());

        // Submit a guess lower than secret
        when(req.getParameter("guess")).thenReturn("30");

        servlet.doPost(req, resp);

        // As servlet redirects after processing, verify redirect target
        verify(resp).sendRedirect("/app/numberguess");

        // Check session updates: low should become guess+1 = 31, remaining decremented
        assertEquals(31, sessionAttrs.get("ng.low"));
        assertEquals(100, sessionAttrs.get("ng.high"));
        assertEquals(9, sessionAttrs.get("ng.remaining"));

        @SuppressWarnings("unchecked") java.util.List<String> history = (java.util.List<String>) sessionAttrs.get("ng.history");
        assertNotNull(history);
        assertFalse(history.isEmpty());
        // Expect a "Too low" entry
        boolean hasTooLow = history.stream().anyMatch(s -> s.contains("Too low") || s.toLowerCase().contains("low"));
        assertTrue(hasTooLow);
    }

    @Test
    public void testDoPost_validGuess_updatesBoundsAndHistory_forTooHigh() throws Exception
    {
        // Initialize session with known secret
        sessionAttrs.put("ng.secret", 40);
        sessionAttrs.put("ng.low", 1);
        sessionAttrs.put("ng.high", 100);
        sessionAttrs.put("ng.remaining", 10);
        sessionAttrs.put("ng.history", new java.util.ArrayList<String>());

        // Submit a guess higher than secret
        when(req.getParameter("guess")).thenReturn("70");

        servlet.doPost(req, resp);

        // Verify redirect
        verify(resp).sendRedirect("/app/numberguess");

        // Check session updates: high should become guess-1 = 69, remaining decremented
        assertEquals(1, sessionAttrs.get("ng.low"));
        assertEquals(69, sessionAttrs.get("ng.high"));
        assertEquals(9, sessionAttrs.get("ng.remaining"));

        @SuppressWarnings("unchecked") java.util.List<String> history = (java.util.List<String>) sessionAttrs.get("ng.history");
        assertNotNull(history);
        assertFalse(history.isEmpty());
        boolean hasTooHigh = history.stream().anyMatch(s -> s.contains("Too high") || s.toLowerCase().contains("high"));
        assertTrue(hasTooHigh);
    }

    @Test
    public void testDoPost_invalidNumber_setsMessageAndRedirects() throws Exception
    {
        // Initialize session
        sessionAttrs.put("ng.secret", 10);
        sessionAttrs.put("ng.low", 1);
        sessionAttrs.put("ng.high", 100);
        sessionAttrs.put("ng.remaining", 10);

        // Submit invalid (non-numeric) guess
        when(req.getParameter("guess")).thenReturn("notanumber");

        servlet.doPost(req, resp);

        // Expect redirect back to the game page
        verify(resp).sendRedirect("/app/numberguess");

        // The servlet stores a message in session for invalid input
        Object msg = sessionAttrs.get("ng.message");
        assertNotNull(msg);
        assertTrue(msg.toString().toLowerCase().contains("invalid"));
    }

    @Test
    public void testDoPost_outOfRange_setsMessageAndRedirects() throws Exception
    {
        // Initialize session
        sessionAttrs.put("ng.secret", 10);
        sessionAttrs.put("ng.low", 1);
        sessionAttrs.put("ng.high", 100);
        sessionAttrs.put("ng.remaining", 10);

        // Guess outside allowed bounds
        when(req.getParameter("guess")).thenReturn("200");

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/app/numberguess");

        Object msg = sessionAttrs.get("ng.message");
        assertNotNull(msg);
        assertTrue(msg.toString().toLowerCase().contains("between"));
    }

    @Test
    public void testDoPost_newGameParameter_startsNewGameAndRedirects() throws Exception
    {
        // existing session state
        sessionAttrs.put("ng.secret", 5);
        sessionAttrs.put("ng.low", 1);
        sessionAttrs.put("ng.high", 100);
        sessionAttrs.put("ng.remaining", 3);

        // Request to start a new game
        when(req.getParameter("new")).thenReturn("1");

        servlet.doPost(req, resp);

        // Should redirect to the game page
        verify(resp).sendRedirect("/app/numberguess");

        // After new game, remaining should be reset to MAX_GUESSES (10)
        assertEquals(10, sessionAttrs.get("ng.remaining"));
        assertEquals(1, sessionAttrs.get("ng.low"));
        assertEquals(100, sessionAttrs.get("ng.high"));
        assertNotNull(sessionAttrs.get("ng.secret"));
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = ""; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);

        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "1"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        underTest.doPost(req2, resp2);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = ""; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = 1; // UTA: default value
        Integer getAttributeResult5 = 1; // UTA: default value
        Integer getAttributeResult6 = 1; // UTA: default value
        Object getAttributeResult7 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        underTest.doPost(req2, resp2);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.NumberGuessServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        NumberGuessServlet underTest = new NumberGuessServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getParameterResult = ""; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        Integer getAttributeResult3 = 1; // UTA: default value
        Integer getAttributeResult4 = 1; // UTA: default value
        Integer getAttributeResult5 = 1; // UTA: default value
        Integer getAttributeResult6 = 1; // UTA: default value
        Object getAttributeResult7 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3, getAttributeResult4, getAttributeResult5, getAttributeResult6, getAttributeResult7);
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        underTest.doPost(req2, resp2);

    }

}