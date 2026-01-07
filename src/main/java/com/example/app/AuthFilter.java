package com.example.app;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sres;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI(); // full path (e.g. /app/home)
        String path = uri.substring(ctx.length());

        // Allow login page, static resources, and HSQLDB console if needed
        if (path.startsWith("/login") || path.startsWith("/static/") || path.startsWith("/mvnw") || path.startsWith("/static")) {
            chain.doFilter(sreq, sres);
            return;
        }

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("user") != null;

        if (!loggedIn) {
            // redirect to login preserving original URL
            String redirect = uri;
            String target = ctx + "/login?redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8.name());
            resp.sendRedirect(target);
            return;
        }

        chain.doFilter(sreq, sres);
    }

}
