package com.example.app;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.json.Json;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class CalculatorApiServletTest {

    private CalculatorApiServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private StringWriter sw;
    private PrintWriter pw;

    @BeforeEach
    void setup() throws Exception {
        servlet = new CalculatorApiServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);
    }

    @Test
    void doGetSuccess() throws Exception {
        when(req.getParameter("a")).thenReturn("2");
        when(req.getParameter("b")).thenReturn("3");
        when(req.getParameter("op")).thenReturn("add");

        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("\"success\""));
        assertTrue(out.contains("\"result\""));
        assertTrue(out.contains("5"));
        verify(resp).setContentType("application/json;charset=UTF-8");
    }

    @Test
    void doPostJsonSuccess() throws Exception {
        String json = Json.createObjectBuilder()
                .add("a", "10")
                .add("b", "4")
                .add("op", "div")
                .build()
                .toString();

        when(req.getContentType()).thenReturn("application/json");
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPost(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("\"success\""));
        assertTrue(out.contains("\"result\""));
        assertTrue(out.contains("2.5"));
        verify(resp).setContentType("application/json;charset=UTF-8");
    }

    @Test
    void doPostInvalidJson() throws Exception {
        String bad = "{ this is : not json }";
        when(req.getContentType()).thenReturn("application/json");
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(bad)));

        servlet.doPost(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("\"success\""));
        assertTrue(out.contains("\"error\""));
        assertTrue(out.contains("Invalid JSON body"));
        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doGetMissingOp() throws Exception {
        when(req.getParameter("a")).thenReturn("1");
        when(req.getParameter("b")).thenReturn("2");
        when(req.getParameter("op")).thenReturn(null);

        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("\"success\""));
        assertTrue(out.contains("\"error\""));
        assertTrue(out.toLowerCase().contains("missing"));
        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doGetInvalidNumber() throws Exception {
        when(req.getParameter("a")).thenReturn("notanumber");
        when(req.getParameter("b")).thenReturn("2");
        when(req.getParameter("op")).thenReturn("add");

        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("\"success\""));
        assertTrue(out.contains("\"error\""));
        assertTrue(out.toLowerCase().contains("invalid"));
        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

}
