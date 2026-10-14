package com.app.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class CalculatorApiServletTest {

    @Test
    void doGetReturnsSuccessfulJson() throws Exception {
        CalculatorApiServlet servlet = new CalculatorApiServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();

        when(req.getContextPath()).thenReturn("");
        when(req.getParameterValues("a")).thenReturn(new String[] { "2" });
        when(req.getParameterValues("b")).thenReturn(new String[] { "3" });
        when(req.getParameterValues("op")).thenReturn(new String[] { "add" });
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doGet(req, resp);

        JsonObject json = Json.createReader(new StringReader(body.toString())).readObject();
        assertTrue(json.getBoolean("success"));
        assertEquals("5", json.getString("result"));
        verify(resp).setContentType("application/json;charset=UTF-8");
    }

    @Test
    void doPostReturnsBadRequestForInvalidJson() throws Exception {
        CalculatorApiServlet servlet = new CalculatorApiServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();

        when(req.getContextPath()).thenReturn("");
        when(req.getContentType()).thenReturn("application/json");
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{not-json")));
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doPost(req, resp);

        JsonObject json = Json.createReader(new StringReader(body.toString())).readObject();
        assertFalse(json.getBoolean("success"));
        assertEquals("Invalid JSON body", json.getString("error"));
        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doPostReturnsBadRequestForInvalidNumber() throws Exception {
        CalculatorApiServlet servlet = new CalculatorApiServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();

        when(req.getContextPath()).thenReturn("");
        when(req.getContentType()).thenReturn("application/json");
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{\"a\":\"x\",\"b\":2,\"op\":\"add\"}")));
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doPost(req, resp);

        JsonObject json = Json.createReader(new StringReader(body.toString())).readObject();
        assertFalse(json.getBoolean("success"));
        assertEquals("Invalid number format", json.getString("error"));
        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void writeJsonFailureSetsInternalServerError() throws Exception {
        CalculatorApiServlet servlet = new CalculatorApiServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getContextPath()).thenReturn("");
        when(req.getParameterValues("a")).thenReturn(new String[] { "2" });
        when(req.getParameterValues("b")).thenReturn(new String[] { "3" });
        when(req.getParameterValues("op")).thenReturn(new String[] { "add" });
        when(resp.getWriter()).thenThrow(new IOException("no writer"));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
