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
    private static final String LOG_WRITE_FAILED = "Failed to write HTML response";
    private static final String LOG_SEND_ERROR_FAILED = "Failed to send error response";
    private static final String ERROR_RESPONSE_WRITE = "Response write error";

    private ServletResponseUtil() {
    }

    public static void writeHtml(HttpServletResponse response, String body) {
        response.setContentType(HTML_CONTENT_TYPE);
        try {
            response.getOutputStream().write((body == null ? EMPTY : body).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, LOG_WRITE_FAILED, ex);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ERROR_RESPONSE_WRITE);
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
            LOGGER.log(Level.WARNING, LOG_SEND_ERROR_FAILED, ex);
            resp.setStatus(statusCode);
        }
    }
}