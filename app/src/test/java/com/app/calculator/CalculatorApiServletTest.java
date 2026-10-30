package com.app.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class CalculatorApiServletTest
{

    @Test
    void doGetReturnsSuccessfulJson() throws Exception
    {
        CalculatorApiServlet servlet = new CalculatorApiServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();

        when(req.getContextPath()).thenReturn("");
        when(req.getParameterValues("a")).thenReturn(new String[]{"2"});
        when(req.getParameterValues("b")).thenReturn(new String[]{"3"});
        when(req.getParameterValues("op")).thenReturn(new String[]{"add"});
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doGet(req, resp);

        JsonObject json = Json.createReader(new StringReader(body.toString())).readObject();
        assertTrue(json.getBoolean("success"));
        assertEquals("5", json.getString("result"));
        verify(resp).setContentType("application/json;charset=UTF-8");
    }

    @Test
    void doPostReturnsBadRequestForInvalidJson() throws Exception
    {
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
    void doPostReturnsBadRequestForInvalidNumber() throws Exception
    {
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
    void writeJsonFailureSetsInternalServerError() throws Exception
    {
        CalculatorApiServlet servlet = new CalculatorApiServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getContextPath()).thenReturn("");
        when(req.getParameterValues("a")).thenReturn(new String[]{"2"});
        when(req.getParameterValues("b")).thenReturn(new String[]{"3"});
        when(req.getParameterValues("op")).thenReturn(new String[]{"add"});
        when(resp.getWriter()).thenThrow(new IOException("no writer"));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void invalidOperationsReturnControlledPublicMessages() throws Exception
    {
        String[][] cases = {{"", "Missing 'op' parameter (add, sub, mul, div)"}, {"unknown", "Unknown operation: unknown"}, {"div", "Division by zero"}};
        for (String[] entry : cases) {
            CalculatorApiServlet servlet = new CalculatorApiServlet();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            StringWriter body = new StringWriter();
            when(request.getContextPath()).thenReturn("");
            when(request.getParameterValues("a")).thenReturn(new String[]{"2"});
            when(request.getParameterValues("b")).thenReturn(new String[]{"0"});
            when(request.getParameterValues("op")).thenReturn(new String[]{entry[0]});
            when(response.getWriter()).thenReturn(new PrintWriter(body));
            servlet.doGet(request, response);
            JsonObject json = Json.createReader(new StringReader(body.toString())).readObject();
            assertEquals(entry[1], json.getString("error"));
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Test
    void servletDeserializationIsRejected() throws Exception
    {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream output = new java.io.ObjectOutputStream(bytes)) {
            output.writeObject(new CalculatorApiServlet());
        }
        try (java.io.ObjectInputStream input = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray()))) {
            org.junit.jupiter.api.Assertions.assertThrows(java.io.NotSerializableException.class, input::readObject);
        }
    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.app.calculator.CalculatorApiServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testDoPost() throws Throwable
    {
        // Given
        CalculatorApiServlet underTest = new CalculatorApiServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContentTypeResult = null; // UTA: configured value
        when(req.getContentType()).thenReturn(getContentTypeResult);

        String getContextPathResult = null; // UTA: configured value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = null; // UTA: configured value
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doPost(req, resp);

    }

}
