package com.app.admin;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;

import com.app.support.JdbcTestSupport;
import com.app.util.PasswordUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class CreateUserServletBehaviorTest {

    @Test
    void doPostCreatesUserAndRedirectsForAdmin() throws Exception {
        CreateUserServlet servlet = new CreateUserServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext ctx = mock(ServletContext.class);
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        JdbcTestSupport.insertUser(ds, "admin", PasswordUtil.hashPassword("admin"), "ADMIN");

        when(req.getContextPath()).thenReturn("/app");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getServletContext()).thenReturn(ctx);
        when(ctx.getAttribute("datasource")).thenReturn(ds);

        when(req.getParameterValues("username")).thenReturn(new String[] { "new-user" });
        when(req.getParameterValues("password")).thenReturn(new String[] { "pw" });
        when(req.getParameterValues("role")).thenReturn(new String[] { "USER" });

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/home");
        assertTrue(JdbcTestSupport.countUsersByName(ds, "new-user") > 0);
    }

    @Test
    void doPostRendersValidationMessageForMissingFields() throws Exception {
        CreateUserServlet servlet = new CreateUserServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext ctx = mock(ServletContext.class);
        RecordingOutputStream os = new RecordingOutputStream();
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        JdbcTestSupport.insertUser(ds, "admin", PasswordUtil.hashPassword("admin"), "ADMIN");

        when(req.getContextPath()).thenReturn("/app");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getServletContext()).thenReturn(ctx);
        when(ctx.getAttribute("datasource")).thenReturn(ds);
        when(ctx.getResourceAsStream("/WEB-INF/templates/createuser.html"))
                .thenReturn(new ByteArrayInputStream("<p>{{message}}</p>".getBytes(StandardCharsets.UTF_8)));
        when(resp.getOutputStream()).thenReturn(os);

        when(req.getParameterValues("username")).thenReturn(new String[] { "" });
        when(req.getParameterValues("password")).thenReturn(new String[] { "pw" });
        when(req.getParameterValues("role")).thenReturn(new String[] { "USER" });

        servlet.doPost(req, resp);

        assertTrue(os.content().contains("Username and password are required."));
    }

    @Test
    void doGetRejectsNonAdminUser() throws Exception {
        CreateUserServlet servlet = new CreateUserServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext ctx = mock(ServletContext.class);
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        JdbcTestSupport.insertUser(ds, "bob", PasswordUtil.hashPassword("bob"), "USER");

        when(req.getContextPath()).thenReturn("/app");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("bob");
        when(req.getServletContext()).thenReturn(ctx);
        when(ctx.getAttribute("datasource")).thenReturn(ds);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
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
        public void write(int b) {
            buffer.write(b);
        }

        String content() {
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
