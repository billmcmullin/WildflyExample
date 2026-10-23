package com.app.calendar;

import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.app.util.UserDataService;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class CalendarReminderListener implements ServletContextListener {
    private static final String TEXT_JAVA_COMP_DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE = "java:comp/DefaultManagedScheduledExecutorService";
    private static final String TEXT_CALENDAR_REMINDERS_USE_THE_CONTAINER_MANAGED_SCHEDULER = "Calendar reminders use the container-managed scheduler";
    private static final String TEXT_MANAGED_REMINDER_SCHEDULER_UNAVAILABLE_REMINDERS_WILL_REFRESH_ON = "Managed reminder scheduler unavailable; reminders will refresh on calendar requests";
    private static final String TEXT_UNABLE_TO_REFRESH_A_USER_S_CALENDAR_REMINDERS = "Unable to refresh a user's calendar reminders";
    private static final String TEXT_UNABLE_TO_LOAD_CALENDAR_REMINDER_OWNERS = "Unable to load calendar reminder owners";

    private ScheduledFuture<?> task;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        try {
            ScheduledExecutorService executor = InitialContext.doLookup(CalendarReminderListener.TEXT_JAVA_COMP_DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE);
            task = executor.scheduleWithFixedDelay(() -> refresh(context), 5, 60, TimeUnit.SECONDS);
            context.log(CalendarReminderListener.TEXT_CALENDAR_REMINDERS_USE_THE_CONTAINER_MANAGED_SCHEDULER);
        } catch (NamingException | ClassCastException exception) {
            context.log(CalendarReminderListener.TEXT_MANAGED_REMINDER_SCHEDULER_UNAVAILABLE_REMINDERS_WILL_REFRESH_ON);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (task != null) {
            task.cancel(false);
        }
    }

    static void refresh(ServletContext context) {
        Object configured = context.getAttribute(UserDataService.CONTEXT_DATASOURCE);
        if (!(configured instanceof DataSource dataSource)) {
            return;
        }
        CalendarStore store = new CalendarStore(dataSource);
        try {
            for (String owner : store.reminderOwners()) {
                try {
                    new CalendarService(store).refreshReminders(owner);
                } catch (SQLException | IllegalArgumentException | IllegalStateException exception) {
                    context.log(CalendarReminderListener.TEXT_UNABLE_TO_REFRESH_A_USER_S_CALENDAR_REMINDERS, exception);
                }
            }
        } catch (SQLException exception) {
            context.log(CalendarReminderListener.TEXT_UNABLE_TO_LOAD_CALENDAR_REMINDER_OWNERS, exception);
        }
    }
}