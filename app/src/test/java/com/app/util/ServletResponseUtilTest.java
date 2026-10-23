package com.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;

class ServletResponseUtilTest {

    @Test
    void missingTemplateReturnsControlledError() throws Exception {
        jakarta.servlet.ServletContext context = mock(jakarta.servlet.ServletContext.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletResponseUtil.renderHtml(response, context, "/missing-template", java.util.Map.of());
        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Response write error");
    }

    @Test
    void writeHtmlWritesUtf8Payload() throws Exception {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RecordingOutputStream os = new RecordingOutputStream();
        when(resp.getOutputStream()).thenReturn(os);

        ServletResponseUtil.writeHtml(resp, "<h1>ok</h1>");

        verify(resp).setContentType("text/html;charset=UTF-8");
        assertEquals("<h1>ok</h1>", os.content());
    }

    @Test
    void writeHtmlSendErrorWhenWriteFails() throws Exception {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getOutputStream()).thenReturn(new FailingOutputStream());

        ServletResponseUtil.writeHtml(resp, "x");

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Response write error");
    }

    @Test
    void sendErrorWithoutMessageCallsStatusOnly() throws Exception {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        ServletResponseUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void sendErrorWithMessageFallsBackToSetStatusOnIOException() throws Exception {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        doThrow(new IOException("write failed")).when(resp)
                .sendError(HttpServletResponse.SC_BAD_REQUEST, "bad");

        ServletResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "bad");

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    private static class RecordingOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // no-op for test stream
        }

        @Override
        public void write(int b) {
            buffer.write(b);
        }

        String content() {
            return buffer.toString(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static class FailingOutputStream extends ServletOutputStream {
        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // no-op for test stream
        }

        @Override
        public void write(int b) throws IOException {
            throw new IOException("boom");
        }
    }
}
