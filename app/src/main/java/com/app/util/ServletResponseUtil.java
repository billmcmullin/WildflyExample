package com.app.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletResponse;

public final class ServletResponseUtil {

    private static final Logger LOGGER = Logger.getLogger("com.app.util.ServletResponseUtil");
    private static final String HTML_CONTENT_TYPE = "text/html;charset=UTF-8";
    private static final String EMPTY = "";

    private ServletResponseUtil() {
    }

    public static void writeHtml(HttpServletResponse resp, String html) {
        resp.setContentType(HTML_CONTENT_TYPE);
        try {
            resp.getOutputStream().write((html == null ? EMPTY : html).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to write HTML response", ex);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Response write error");
        }
    }

    public static void sendError(HttpServletResponse resp, int statusCode) {
        sendError(resp, statusCode, null);
    }

    public static void sendError(HttpServletResponse resp, int statusCode, String message) {
        try {
            if (message == null || message.isBlank()) {
                resp.sendError(statusCode);
            } else {
                resp.sendError(statusCode, message);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to send error response", ex);
            resp.setStatus(statusCode);
        }
    }
}