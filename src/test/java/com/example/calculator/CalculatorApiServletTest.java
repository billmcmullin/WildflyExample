package com.example.calculator;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CalculatorApiServletTest {

    private CalculatorApiServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new CalculatorApiServlet();
    }

    @Test
    void testDoGetAdd() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getParameter("a")).thenReturn("2");
        when(req.getParameter("b")).thenReturn("3");
        when(req.getParameter("op")).thenReturn("add");

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(req, resp);

        try (var jr = Json.createReader(new StringReader(sw.toString()))) {
            JsonObject json = jr.readObject();
            assertTrue(json.getBoolean("success"));
            assertEquals("5", json.getString("result"));
        }
    }

    @Test
    void testDoGetSub() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getParameter("a")).thenReturn("10");
        when(req.getParameter("b")).thenReturn("4");
        when(req.getParameter("op")).thenReturn("sub");

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(req, resp);

        try (var jr = Json.createReader(new StringReader(sw.toString()))) {
            JsonObject json = jr.readObject();
            assertTrue(json.getBoolean("success"));
            assertEquals("6", json.getString("result"));
        }
    }

    @Test
    void testDoPostJsonMul() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        String jsonBody = "{\"a\":\"2.5\",\"b\":4,\"op\":\"mul\"}";
        when(req.getContentType()).thenReturn("application/json");
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(req, resp);

        try (var jr = Json.createReader(new StringReader(sw.toString()))) {
            JsonObject json = jr.readObject();
            assertTrue(json.getBoolean("success"));
            assertEquals("10.0", json.getString("result"));
        }
    }

    @Test
    void testDoPostFormDiv() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        // Simulate form params (contentType not JSON)
        when(req.getContentType()).thenReturn(null);
        when(req.getParameter("a")).thenReturn("5");
        when(req.getParameter("b")).thenReturn("2");
        when(req.getParameter("op")).thenReturn("div");

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(req, resp);

        try (var jr = Json.createReader(new StringReader(sw.toString()))) {
            JsonObject json = jr.readObject();
            assertTrue(json.getBoolean("success"));
            // 5 / 2 = 2.5
            assertEquals("2.5", json.getString("result"));
        }
    }
}
