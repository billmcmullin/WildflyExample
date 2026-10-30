package com.app.calendar;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for CalendarReminderListener
 *
 * @see com.app.calendar.CalendarReminderListener
 * @author bmcmullin
 */
public class CalendarReminderListenerTest
{

    /**
     * Parasoft Jtest UTA: Test for contextDestroyed(ServletContextEvent)
     *
     * @see com.app.calendar.CalendarReminderListener#contextDestroyed(ServletContextEvent)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testContextDestroyed() throws Throwable
    {
        // Given
        CalendarReminderListener underTest = new CalendarReminderListener();

        // When
        ServletContextEvent event = mock(ServletContextEvent.class);
        underTest.contextDestroyed(event);

    }

    /**
     * Parasoft Jtest UTA: Test for refresh(ServletContext)
     *
     * @see com.app.calendar.CalendarReminderListener#refresh(ServletContext)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testRefresh() throws Throwable
    {
        // When
        ServletContext context = mock(ServletContext.class);
        Object getAttributeResult = null; // UTA: configured value
        when(context.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        CalendarReminderListener.refresh(context);

    }

}
