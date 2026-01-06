package com.example.calculator;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class CalculatorUIServletTest {

    private CalculatorUIServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private StringWriter sw;
    private PrintWriter pw;

    @BeforeEach
    void setup() throws Exception {
        servlet = new CalculatorUIServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);
    }

    @Test
    void doGetRendersForm() throws Exception {
        when(req.getParameter("op")).thenReturn(null); // no params
        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("<form"));
        assertTrue(out.contains("name=\"a\""));
        assertTrue(out.contains("name=\"b\""));
        assertTrue(out.contains("name=\"op\""));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

    @Test
    void doGetComputeSuccess() throws Exception {
        when(req.getParameter("a")).thenReturn("6");
        when(req.getParameter("b")).thenReturn("7");
        when(req.getParameter("op")).thenReturn("mul");

        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("Result:"));
        assertTrue(out.contains("42"));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

    @Test
    void doGetInvalidNumberShowsError() throws Exception {
        when(req.getParameter("a")).thenReturn("bad");
        when(req.getParameter("b")).thenReturn("1");
        when(req.getParameter("op")).thenReturn("add");

        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("Error:"));
        assertTrue(out.toLowerCase().contains("invalid number"));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

}
