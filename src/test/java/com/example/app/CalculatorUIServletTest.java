package com.example.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class CalculatorUIServletTest
{

    private CalculatorUIServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private ServletContext ctx;
    private StringWriter sw;
    private PrintWriter pw;

    @BeforeEach
    void setup() throws Exception
    {
        servlet = new CalculatorUIServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        ctx = mock(ServletContext.class);

        // Mock servlet context and context path
        when(req.getServletContext()).thenReturn(ctx);
        when(req.getContextPath()).thenReturn("/app");

        // Minimal calculator template (placeholders used by TemplateRenderer)
        String calcTpl = "<!doctype html><html><body><h1>Calculator</h1>" + "<form action=\"{{ctx}}/calculator\">" + "<input name=\"a\" value=\"{{a}}\"/>" + "<input name=\"b\" value=\"{{b}}\"/>" + "<select name=\"op\">" + "<option value=\"add\" {{op_add}}>add</option>" + "<option value=\"sub\" {{op_sub}}>sub</option>" + "<option value=\"mul\" {{op_mul}}>mul</option>" + "<option value=\"div\" {{op_div}}>div</option>" + "</select>" + "<button>Calc</button></form>{{result_block}}</body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(calcTpl.getBytes(StandardCharsets.UTF_8));
        when(ctx.getResourceAsStream("/WEB-INF/templates/calculator.html")).thenReturn(is);

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);
    }

    @Test
    void doGetRendersForm() throws Exception
    {
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
    void doGetComputeSuccess() throws Exception
    {
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
    void doGetInvalidNumberShowsError() throws Exception
    {
        when(req.getParameter("a")).thenReturn("bad");
        when(req.getParameter("b")).thenReturn("1");
        when(req.getParameter("op")).thenReturn("add");

        servlet.doGet(req, resp);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("Error") || out.toLowerCase().contains("invalid"));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = "add"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = "add"; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.example.app.CalculatorUIServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet10() throws Throwable
    {
        // Given
        CalculatorUIServlet underTest = new CalculatorUIServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(IOException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

}
