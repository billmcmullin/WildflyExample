package com.app.admin;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.app.util.AppWebConstants;
import com.app.util.UserDataService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(AppWebConstants.FILTER_ALL)
public class AuthFilter implements Filter {

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60; // must match LoginServlet
    private static final long serialVersionUID = 1L;
    private static final String PATH_LOGIN = "/login";
    private static final String PATH_STATIC = "/static";
    private static final String PATH_MVNW = "/mvnw";
    private static final String REMEMBER_COOKIE = "remember";
    private static final String CLASS_NAME = "AuthFilter";
    private static final char PATH_SEPARATOR = '/';

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sres;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI(); // full path (e.g. /app/home)
        String path = uri.substring(ctx.length());

        // Allow login page, static resources, and HSQLDB console if needed
        if (path.startsWith(PATH_LOGIN)
            || path.startsWith(PATH_STATIC + PATH_SEPARATOR)
            || path.startsWith(PATH_MVNW)
            || path.startsWith(PATH_STATIC)) {
            chain.doFilter(sreq, sres);
            return;
        }

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute(UserDataService.SESSION_USER) != null;

        if (!loggedIn) {
            // try "remember" cookie
            String rememberToken = null;
            if (req.getCookies() != null) {
                for (jakarta.servlet.http.Cookie c : req.getCookies()) {
                    if (REMEMBER_COOKIE.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                        rememberToken = c.getValue();
                        break;
                    }
                }
            }

            if (rememberToken != null) {
                try {
                    String username = UserDataService.findUsernameByToken(req.getServletContext(), rememberToken);
                    if (username != null) {
                        // auto-login
                        HttpSession newSession = req.getSession(true);
                        newSession.setAttribute(UserDataService.SESSION_USER, username);
                        newSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
                        // optionally refresh token expiry (not done here)
                        chain.doFilter(sreq, sres);
                        return;
                    }
                } catch (Exception ignored) {
                }
            }

            // redirect to login
            resp.sendRedirect(ctx + PATH_LOGIN);
            return;
        }

        chain.doFilter(sreq, sres);
    }

    private final void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException(CLASS_NAME);
    }

    private final void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException(CLASS_NAME);
    }
}
