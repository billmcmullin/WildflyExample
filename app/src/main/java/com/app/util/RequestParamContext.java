package com.app.util;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestParamContext {

    private static final int MAX_VALUE_LENGTH = 2048;
    private static final String EMPTY = "";
    private static final String ROOT_PATH = "/";
    private static final String PATH_SEPARATOR = "//";
    private static final String URL_SCHEME_SEPARATOR = "://";

    private final HttpServletRequest request;
    private final String contextPath;

    public RequestParamContext(HttpServletRequest request) {
        this.request = request;
        this.contextPath = request.getContextPath() == null ? EMPTY : request.getContextPath();
    }

    public String value(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String[] values = request.getParameterValues(name);
        String raw;
        if (values != null && values.length > 0) {
            raw = values[0];
        } else {
            // Compatibility fallback for legacy tests and wrappers that only stub getParameter.
            raw = request.getParameter(name);
        }
        if (raw == null) {
            return null;
        }
        String trimmed = stripControlChars(raw.trim());
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > MAX_VALUE_LENGTH) {
            return truncate(trimmed, MAX_VALUE_LENGTH);
        }
        return trimmed;
    }

    public String contextPath() {
        return contextPath;
    }

    public String appPath(String route) {
        if (route == null || route.isBlank()) {
            return contextPath.isEmpty() ? ROOT_PATH : contextPath;
        }
        if (!route.isEmpty() && route.charAt(0) == '/') {
            return contextPath + route;
        }
        StringBuilder sb = new StringBuilder(contextPath.length() + route.length() + 1);
        sb.append(contextPath).append('/').append(route);
        return sb.toString();
    }

    public String localRedirectOr(String candidate, String fallback) {
        if (candidate == null || candidate.isBlank()) {
            return fallback;
        }
        String normalized = stripControlChars(candidate.trim());
        if (!isSafeLocalPath(normalized)) {
            return fallback;
        }
        return normalized;
    }

    private boolean isSafeLocalPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (path.charAt(0) != '/' || path.startsWith(PATH_SEPARATOR) || path.contains(URL_SCHEME_SEPARATOR)) {
            return false;
        }
        if (contextPath.isEmpty()) {
            return true;
        }
        String expectedPrefix = contextPath + '/';
        return path.equals(contextPath) || path.startsWith(expectedPrefix);
    }

    private static String truncate(String value, int length) {
        char[] chars = new char[length];
        value.getChars(0, length, chars, 0);
        return new String(chars);
    }

    private static String stripControlChars(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\r' || c == '\n') {
                continue;
            }
            if (Character.isISOControl(c)) {
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}