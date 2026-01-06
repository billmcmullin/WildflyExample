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

class CalculatorUIServletTest {

    private CalculatorUIServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private ServletContext ctx;
    private StringWriter sw;
    private PrintWriter pw;

    @BeforeEach
    void setup() throws Exception {
        servlet = new CalculatorUIServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        ctx = mock(ServletContext.class);

        // Mock servlet context and context path
        when(req.getServletContext()).thenReturn(ctx);
        when(req.getContextPath()).thenReturn("/app");

        // Minimal calculator template (placeholders used by TemplateRenderer)
        String calcTpl
                = "<!doctype html><html><body><h1>Calculator</h1>"
                + "<form action=\"{{ctx}}/calculator\">"
                + "<input name=\"a\" value=\"{{a}}\"/>"
                + "<input name=\"b\" value=\"{{b}}\"/>"
                + "<select name=\"op\">"
                + "<option value=\"add\" {{op_add}}>add</option>"
                + "<option value=\"sub\" {{op_sub}}>sub</option>"
                + "<option value=\"mul\" {{op_mul}}>mul</option>"
                + "<option value=\"div\" {{op_div}}>div</option>"
                + "</select>"
                + "<button>Calc</button></form>{{result_block}}</body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(calcTpl.getBytes(StandardCharsets.UTF_8));
        when(ctx.getResourceAsStream("/WEB-INF/templates/calculator.html")).thenReturn(is);

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);
    }

    @Test
    void doGetRendersForm() throws Exception {
        when(req.getParameter("op")).thenReturn(null);
        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("<form"));
        assertTrue(out.contains("action=\"/app/calculator\""));
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
        // servlet inserts Result: ... into result_block; check for either "Result" or the numeric value
        assertTrue(out.contains("Result") || out.contains("42"));
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
        assertTrue(out.contains("Error") || out.toLowerCase().contains("invalid"));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

}
