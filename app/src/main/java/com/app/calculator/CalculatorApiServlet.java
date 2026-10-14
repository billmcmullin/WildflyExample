package com.app.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.app.util.RequestParamContext;

@WebServlet(name = "CalculatorApiServlet", urlPatterns = {"/api/calc"})
public class CalculatorApiServlet extends HttpServlet {

    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String MESSAGE_INVALID_JSON = "Invalid JSON body";
    private static final String MESSAGE_INVALID_NUMBER = "Invalid number format";
    private static final String MESSAGE_SERVER_ERROR = "Server error";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        RequestParamContext requestContext = new RequestParamContext(req);
        String a = requestContext.value("a");
        String b = requestContext.value("b");
        String op = requestContext.value("op");
        processAndWriteJson(a, b, op, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        RequestParamContext requestContext = new RequestParamContext(req);
        String contentType = req.getContentType();
        String a = null, b = null, op = null;

        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            BufferedReader reader;
            try {
                reader = req.getReader();
            } catch (IOException e) {
                sendError(resp, MESSAGE_INVALID_JSON);
                return;
            }
            try (BufferedReader safeReader = reader;
                 JsonReader jsonReader = Json.createReader(safeReader)) {
                JsonObject body = jsonReader.readObject();
                a = getJsonValueAsString(body, "a");
                b = getJsonValueAsString(body, "b");
                op = body.containsKey("op") ? body.getString("op", null) : null;
            } catch (JsonException | IOException e) {
                sendError(resp, MESSAGE_INVALID_JSON);
                return;
            }
        } else {
            a = requestContext.value("a");
            b = requestContext.value("b");
            op = requestContext.value("op");
        }

        processAndWriteJson(a, b, op, resp);
    }

    private static String getJsonValueAsString(JsonObject obj, String key) {
        if (!obj.containsKey(key)) return null;
        JsonValue v = obj.get(key);
        switch (v.getValueType()) {
            case STRING:
                return ((JsonString) v).getString();
            case NUMBER:
                return obj.getJsonNumber(key).toString();
            case TRUE:
                return "true";
            case FALSE:
                return "false";
            case NULL:
                return null;
            default:
                return v.toString();
        }
    }

    private void processAndWriteJson(String aStr, String bStr, String op, HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE_JSON);
        JsonObjectBuilder jb = Json.createObjectBuilder();

        try {
            BigDecimal a = new BigDecimal((aStr != null) ? aStr.trim() : "0");
            BigDecimal b = new BigDecimal((bStr != null) ? bStr.trim() : "0");

            java.math.BigDecimal result = CalculatorService.calculate(a, b, op);

            jb.add("success", true)
              .add("result", result.toPlainString());

            writeJson(resp, jb.build());
        } catch (NumberFormatException e) {
            sendJsonFailure(resp, MESSAGE_INVALID_NUMBER);
        } catch (IllegalArgumentException | ArithmeticException e) {
            sendJsonFailure(resp, e.getMessage());
        } catch (Exception e) {
            sendJsonFailure(resp, MESSAGE_SERVER_ERROR);
        }
    }

    private void sendJsonFailure(HttpServletResponse resp, String message) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObjectBuilder jb = Json.createObjectBuilder()
                .add("success", false)
                .add("error", message == null ? "" : message);
        writeJson(resp, jb.build());
    }

    private void sendError(HttpServletResponse resp, String message) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType(CONTENT_TYPE_JSON);
        JsonObjectBuilder jb = Json.createObjectBuilder()
                .add("success", false)
                .add("error", message == null ? "" : message);
        writeJson(resp, jb.build());
    }

    private void writeJson(HttpServletResponse resp, JsonObject payload) {
        try {
            if (resp.getOutputStream() != null) {
                resp.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));
                return;
            }
        } catch (IOException ignored) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        try (PrintWriter out = resp.getWriter()) {
            out.write(payload.toString());
        } catch (IOException ignored) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
