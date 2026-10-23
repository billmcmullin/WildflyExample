package com.app.calendar;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class CalendarSecurity {
    private static final String FIELD_CSRF = "csrf";

    private static final String TOKEN_ATTRIBUTE = "calendar.csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CalendarSecurity() { }

    public static String token(HttpSession session) {
        synchronized (session) {
            Object existing = session.getAttribute(TOKEN_ATTRIBUTE);
            if (existing instanceof String existingToken) {
                return existingToken;
            }
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String generatedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(TOKEN_ATTRIBUTE, generatedToken);
            return generatedToken;
        }
    }

    public static boolean valid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String supplied = request.getParameter(CalendarSecurity.FIELD_CSRF);
        Object existing = session == null ? null : session.getAttribute(TOKEN_ATTRIBUTE);
        return existing instanceof String expected && supplied != null && supplied.length() <= 100
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }
}