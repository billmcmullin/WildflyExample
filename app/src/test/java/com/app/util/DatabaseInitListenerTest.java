package com.app.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

class DatabaseInitListenerTest {

    @Test
    void contextInitializedSetsDatasourceOnServletContext() {
        DatabaseInitListener listener = new DatabaseInitListener();
        ServletContext ctx = mock(ServletContext.class);
        ServletContextEvent event = new ServletContextEvent(ctx);

        listener.contextInitialized(event);

        verify(ctx, atLeastOnce()).setAttribute(eq("datasource"), any());
    }

    @Test
    void contextDestroyedIsNoOp() {
        DatabaseInitListener listener = new DatabaseInitListener();
        ServletContextEvent event = new ServletContextEvent(mock(ServletContext.class));

        listener.contextDestroyed(event);
    }
}
