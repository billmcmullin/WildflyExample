package com.app.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;

import com.app.support.JdbcTestSupport;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class LoginServletBehaviorTest {

    @Test
    void doPostSuccessfulLoginWithRememberCookie() throws Exception {
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
        verify(resp).sendRedirect("/home");
        assertTrue(JdbcTestSupport.countTokensForUser(ds, "alice") > 0);
    }

    @Test
    void doPostInvalidPasswordRendersLoginTemplate() throws Exception {
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
    void doPostWrapsDataAccessFailureInServletException() throws Exception {
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

    private static class RecordingOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // no-op for test stream
        }

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }

        String content() {
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
