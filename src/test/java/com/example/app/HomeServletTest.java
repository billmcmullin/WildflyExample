package com.example.app;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class HomeServletTest {

    private HomeServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private ServletContext ctx;
    private StringWriter sw;
    private PrintWriter pw;

    @BeforeEach
    void setup() throws Exception {
        servlet = new HomeServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        ctx = mock(ServletContext.class);

        // Mock request -> servlet context and context path
        when(req.getServletContext()).thenReturn(ctx);
        when(req.getContextPath()).thenReturn("/app");

        // Provide minimal template directly (no file needed)
        String homeTpl = "<!doctype html><html><body><h1>Welcome to App</h1><nav><a href=\"{{ctx}}/calculator\">Open Calculator</a></nav></body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(homeTpl.getBytes(StandardCharsets.UTF_8));
        when(ctx.getResourceAsStream("/WEB-INF/templates/home.html")).thenReturn(is);

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);
    }

    @Test
    void doGetContainsCalculatorLink() throws Exception {
        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("Welcome to App"));
        assertTrue(out.contains("/app/calculator"));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

}
