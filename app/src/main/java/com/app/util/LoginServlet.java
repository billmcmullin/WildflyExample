package com.app.util;

import java.math.BigInteger;
import java.time.Instant;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60; // 30 minutes
    private static final int REMEMBER_DAYS = 7; // remember-me token valid for 7 days
    private static final String LOGIN_TEMPLATE = "/WEB-INF/templates/login.html";
    private static final String REMEMBER_ON = "on";
    private static final String COOKIE_NAME_REMEMBER = "remember";
    private static final String ERROR_REDIRECT_FAILED = "Redirect failed";
    private static final String ERROR_TEMPLATE_RENDER = "Template render error";

    private static final SecureRandom secureRandom = new SecureRandom();

    private String generateToken() {
        return new BigInteger(160, secureRandom).toString(32);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        RequestParamContext requestContext = new RequestParamContext(req);
        String safeRedirect = requestContext.localRedirectOr(requestContext.value("redirect"), "");
        renderLogin(req, resp, safeRedirect, "");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        RequestParamContext requestContext = new RequestParamContext(req);

        String user = requestContext.value("username");
        String pass = requestContext.value("password");
        String redirect = requestContext.value("redirect");
        String remember = requestContext.value("remember"); // "on" if checked
        String safeRedirect = requestContext.localRedirectOr(redirect, requestContext.appPath(AppWebConstants.HOME_PATH));

        try {
            String storedHash = UserDataService.findPasswordHashByUsername(req.getServletContext(), user);
            if (storedHash != null && PasswordUtil.verify(pass, storedHash)) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

                // if remember checked -> create token
                if (REMEMBER_ON.equalsIgnoreCase(remember)) {
                    String token = generateToken();
                    long nowMs = Instant.now().toEpochMilli();
                    long expiry = nowMs + REMEMBER_DAYS * 24L * 3600L * 1000L;
                    UserDataService.createRememberToken(req.getServletContext(), token, user, expiry);

                    Cookie cookie = new Cookie(COOKIE_NAME_REMEMBER, token);
                    cookie.setHttpOnly(true);
                    cookie.setPath(requestContext.contextPath().isEmpty() ? AppWebConstants.ROOT_PATH : requestContext.contextPath());
                    long maxAgeSeconds = Math.max(0L, (expiry - nowMs) / 1000L);
                    cookie.setMaxAge(toCookieMaxAgeSeconds(maxAgeSeconds));
                    cookie.setSecure(req.isSecure());
                    resp.addCookie(cookie);
                }

                try {
                    resp.sendRedirect(safeRedirect);
                } catch (IOException ex) {
                    ServletResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ERROR_REDIRECT_FAILED);
                }
                return;
            } else {
                renderLogin(req, resp, requestContext.localRedirectOr(redirect, ""), "Invalid username or password.");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void renderLogin(HttpServletRequest req, HttpServletResponse resp, String redirect, String message) {
        Map<String, String> values = new HashMap<>();
        values.put("ctx", TemplateRenderer.escapeHtml(req.getContextPath()));
        values.put("message", TemplateRenderer.escapeHtml(message));
        values.put("redirect", TemplateRenderer.escapeHtml(redirect == null ? "" : redirect));

        try {
            String html = TemplateRenderer.render(req.getServletContext(), LOGIN_TEMPLATE, values);
            ServletResponseUtil.writeHtml(resp, html);
        } catch (Exception ex) {
            ServletResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ERROR_TEMPLATE_RENDER);
        }
    }

    private static int toCookieMaxAgeSeconds(long seconds) {
        if (seconds >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.toIntExact(seconds);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException("LoginServlet");
    }
}
