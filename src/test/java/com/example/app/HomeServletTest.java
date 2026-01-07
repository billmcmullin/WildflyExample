package com.example.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class HomeServletTest
{

    private HomeServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private ServletContext ctx;
    private StringWriter sw;
    private PrintWriter pw;

    @BeforeEach
    void setup() throws Exception
    {
        servlet = new HomeServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        ctx = mock(ServletContext.class);

        // Mock request -> servlet context and context path
        when(req.getServletContext()).thenReturn(ctx);
        when(req.getContextPath()).thenReturn("/app");

        // Provide minimal template directly (no file needed)
        String homeTpl = "<!doctype html><html><body><h1>Welcome to App</h1><nav><a href=\"{{ctx}}/calculator\">Open Calculator</a></nav></body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(homeTpl.getBytes(StandardCharsets.UTF_8));
        when(ctx.getResourceAsStream("/WEB-INF/templates/home.html")).thenReturn(is);

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);
    }

    @Test
    void doGetContainsCalculatorLink() throws Exception
    {
        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("Welcome to App"));
        assertTrue(out.contains("/app/calculator"));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.HomeServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        HomeServlet underTest = new HomeServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getServletContextResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);

        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

}
