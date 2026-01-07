package com.example.app.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

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

@WebServlet(name = "CalculatorApiServlet", urlPatterns = {"/api/calc"})
public class CalculatorApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String a = req.getParameter("a");
        String b = req.getParameter("b");
        String op = req.getParameter("op");
        processAndWriteJson(a, b, op, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contentType = req.getContentType();
        String a = null, b = null, op = null;

        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            try (BufferedReader reader = req.getReader();
                 JsonReader jsonReader = Json.createReader(reader)) {
                JsonObject body = jsonReader.readObject();
                a = getJsonValueAsString(body, "a");
                b = getJsonValueAsString(body, "b");
                op = body.containsKey("op") ? body.getString("op", null) : null;
            } catch (JsonException e) {
                sendError(resp, "Invalid JSON body");
                return;
            }
        } else {
            a = req.getParameter("a");
            b = req.getParameter("b");
            op = req.getParameter("op");
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

    private void processAndWriteJson(String aStr, String bStr, String op, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JsonObjectBuilder jb = Json.createObjectBuilder();

        try {
            BigDecimal a = new BigDecimal((aStr != null) ? aStr.trim() : "0");
            BigDecimal b = new BigDecimal((bStr != null) ? bStr.trim() : "0");

            java.math.BigDecimal result = CalculatorService.calculate(a, b, op);

            jb.add("success", true)
              .add("result", result.toPlainString());

            try (PrintWriter out = resp.getWriter()) {
                Json.createWriter(out).writeObject(jb.build());
            }
        } catch (NumberFormatException e) {
            sendJsonFailure(resp, "Invalid number format");
        } catch (IllegalArgumentException | ArithmeticException e) {
            sendJsonFailure(resp, e.getMessage());
        } catch (Exception e) {
            sendJsonFailure(resp, "Server error");
        }
    }

    private void sendJsonFailure(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObjectBuilder jb = Json.createObjectBuilder()
                .add("success", false)
                .add("error", message == null ? "" : message);
        try (PrintWriter out = resp.getWriter()) {
            Json.createWriter(out).writeObject(jb.build());
        }
    }

    private void sendError(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json;charset=UTF-8");
        JsonObjectBuilder jb = Json.createObjectBuilder()
                .add("success", false)
                .add("error", message == null ? "" : message);
        try (PrintWriter out = resp.getWriter()) {
            Json.createWriter(out).writeObject(jb.build());
        }
    }
}
