package com.app.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import org.hsqldb.jdbc.JDBCDataSource;

import com.app.support.JdbcTestSupport;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class AuthFilterTest {

    @Test
    void allowsLoginPathWithoutAuthentication() throws Exception {
        AuthFilter filter = new AuthFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getContextPath()).thenReturn("/app");
        when(req.getRequestURI()).thenReturn("/app/login");

        filter.doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
    }

    @Test
    void redirectsAnonymousUserToLoginWhenNoRememberCookie() throws Exception {
        AuthFilter filter = new AuthFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getContextPath()).thenReturn("/app");
        when(req.getRequestURI()).thenReturn("/app/home");
        when(req.getSession(false)).thenReturn(null);
        when(req.getCookies()).thenReturn(null);

        filter.doFilter(req, resp, chain);

        verify(resp).sendRedirect("/app/login");
    }

    @Test
    void keepsLoggedInUser() throws Exception {
        AuthFilter filter = new AuthFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getContextPath()).thenReturn("/app");
        when(req.getRequestURI()).thenReturn("/app/home");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");

        filter.doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
    }

    @Test
    void autoLogsInFromRememberCookie() throws Exception {
        AuthFilter filter = new AuthFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        ServletContext servletContext = mock(ServletContext.class);
        HttpSession session = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        JdbcTestSupport.insertToken(ds, "tok123", "remembered-user", Instant.now().toEpochMilli() + 60_000L);

        when(req.getContextPath()).thenReturn("/app");
        when(req.getRequestURI()).thenReturn("/app/home");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);
        when(req.getCookies()).thenReturn(new Cookie[] { new Cookie("remember", "tok123") });
        when(req.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("datasource")).thenReturn(ds);

        when(req.getSession(true)).thenReturn(newSession);

        filter.doFilter(req, resp, chain);

        verify(newSession).setAttribute("user", "remembered-user");
        verify(newSession).setMaxInactiveInterval(30 * 60);
        verify(chain).doFilter(req, resp);
    }

    @Test
    void rememberTokenFailureFallsBackToLoginRedirect() throws Exception {
        AuthFilter filter = new AuthFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        ServletContext servletContext = mock(ServletContext.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getContextPath()).thenReturn("/app");
        when(req.getRequestURI()).thenReturn("/app/home");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);
        when(req.getCookies()).thenReturn(new Cookie[] { new Cookie("remember", "tok123") });
        when(req.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("datasource")).thenReturn(null);

        filter.doFilter(req, resp, chain);

        verify(resp).sendRedirect("/app/login");
    }
}
