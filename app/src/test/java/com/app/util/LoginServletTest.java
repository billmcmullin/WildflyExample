package com.app.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.support.JdbcTestSupport;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
/**
 * Parasoft Jtest UTA: Test class for LoginServlet
 *
 * @see com.example.app.util.LoginServlet
 * @author bmcmullin
 */
public class LoginServletTest
{

    @Test
    void doPostSuccessfulLoginWithRememberCookie() throws Exception
    {
        LoginServlet servlet = new LoginServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext ctx = mock(ServletContext.class);
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        JdbcTestSupport.insertUser(ds, "alice", PasswordUtil.hashPassword("pw"), "USER");

        when(req.getContextPath()).thenReturn("/app");
        when(req.getParameterValues("username")).thenReturn(new String[] { "alice" });
        when(req.getParameterValues("password")).thenReturn(new String[] { "pw" });
        when(req.getParameterValues("redirect")).thenReturn(null);
        when(req.getParameterValues("remember")).thenReturn(new String[] { "on" });
        when(req.getServletContext()).thenReturn(ctx);
        when(ctx.getAttribute("datasource")).thenReturn(ds);
        when(req.getSession(true)).thenReturn(session);
        when(req.isSecure()).thenReturn(true);

        servlet.doPost(req, resp);

        verify(session).setAttribute("user", "alice");
        verify(session).setMaxInactiveInterval(30 * 60);
        verify(resp).addCookie(any());
        verify(resp).sendRedirect("/app/home");
        assertTrue(JdbcTestSupport.countTokensForUser(ds, "alice") > 0);
    }

    @Test
    void doPostRedirectStaysWithinDeploymentContext() throws Exception
    {
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        JdbcTestSupport.insertUser(ds, "alice", PasswordUtil.hashPassword("pw"), "USER");
        String[][] scenarios = {
                { "/app", null, "/app/home" },
                { "/app", "", "/app/home" },
                { "", null, "/home" },
                { "/custom", null, "/custom/home" },
                { "/app", "/app/swagger-ui.html", "/app/swagger-ui.html" },
                { "/app", "/home", "/app/home" },
                { "/app", "https://example.org", "/app/home" },
                { "/app", "//example.org", "/app/home" }
        };

        for (String[] scenario : scenarios) {
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = mock(HttpSession.class);
            ServletContext ctx = mock(ServletContext.class);
            when(req.getContextPath()).thenReturn(scenario[0]);
            when(req.getParameter("username")).thenReturn("alice");
            when(req.getParameter("password")).thenReturn("pw");
            when(req.getParameter("redirect")).thenReturn(scenario[1]);
            when(req.getServletContext()).thenReturn(ctx);
            when(ctx.getAttribute("datasource")).thenReturn(ds);
            when(req.getSession(true)).thenReturn(session);

            new LoginServlet().doPost(req, resp);

            verify(resp).sendRedirect(scenario[2]);
        }
    }

    @Test
    void doPostInvalidPasswordRendersLoginTemplate() throws Exception
    {
        LoginServlet servlet = new LoginServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletContext ctx = mock(ServletContext.class);
        RecordingOutputStream os = new RecordingOutputStream();
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        JdbcTestSupport.insertUser(ds, "alice", PasswordUtil.hashPassword("right"), "USER");

        when(req.getContextPath()).thenReturn("/app");
        when(req.getParameterValues("username")).thenReturn(new String[] { "alice" });
        when(req.getParameterValues("password")).thenReturn(new String[] { "wrong" });
        when(req.getParameterValues("redirect")).thenReturn(new String[] { "/app/home" });
        when(req.getParameterValues("remember")).thenReturn(null);
        when(req.getServletContext()).thenReturn(ctx);
        when(ctx.getAttribute("datasource")).thenReturn(ds);
        when(ctx.getResourceAsStream("/WEB-INF/templates/login.html"))
                .thenReturn(new ByteArrayInputStream("<p>{{message}}</p>".getBytes(StandardCharsets.UTF_8)));
        when(resp.getOutputStream()).thenReturn(os);

        servlet.doPost(req, resp);

        assertTrue(os.content().contains("Invalid username or password."));
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    void doPostWrapsDataAccessFailureInServletException() throws Exception
    {
        LoginServlet servlet = new LoginServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletContext ctx = mock(ServletContext.class);

        when(req.getContextPath()).thenReturn("/app");
        when(req.getParameterValues("username")).thenReturn(new String[] { "alice" });
        when(req.getParameterValues("password")).thenReturn(new String[] { "pw" });
        when(req.getParameterValues("redirect")).thenReturn(null);
        when(req.getParameterValues("remember")).thenReturn(null);
        when(req.getServletContext()).thenReturn(ctx);
        when(ctx.getAttribute("datasource")).thenReturn(null);

        assertThrows(ServletException.class, () -> servlet.doPost(req, resp));
    }

    private static class RecordingOutputStream extends ServletOutputStream
    {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        @Override
        public boolean isReady()
        {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener)
        {
        }

        @Override
        public void write(int value) throws IOException
        {
            buffer.write(value);
        }

        String content()
        {
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LoginServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LoginServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LoginServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LoginServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.util.LoginServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        ServletContext getServletContextResult = mock(ServletContext.class);
        when(req.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

}
