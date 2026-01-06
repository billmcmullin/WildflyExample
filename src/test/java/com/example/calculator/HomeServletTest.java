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

class HomeServletTest {

    private HomeServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private StringWriter sw;
    private PrintWriter pw;

    @BeforeEach
    void setup() throws Exception {
        servlet = new HomeServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        // Mock the context path used by the servlet to build URLs
        when(req.getContextPath()).thenReturn("/app");
    }

    @Test
    void doGetContainsCalculatorLink() throws Exception {
        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("Welcome to App"));
        // Now check for the context-aware calculator link
        assertTrue(out.contains("/app/calculator"));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

}
