package com.app.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import com.app.calendar.CalendarStore.AuditEntry;
import com.app.calendar.CalendarStore.ImportResult;
import com.app.calendar.CalendarStore.Notification;
import com.app.database.DatabaseMigrations;
import com.app.support.JdbcTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class CalendarStoreTest
{
    private JDBCDataSource dataSource;
    private CalendarStore store;

    @BeforeEach
    void setUp() throws Exception
    {
        dataSource = JdbcTestSupport.newDataSource();
        DatabaseMigrations.migrate(dataSource);
        JdbcTestSupport.insertUser(dataSource, "alice", "hash", "USER");
        JdbcTestSupport.insertUser(dataSource, "bob", "hash", "USER");
        store = new CalendarStore(dataSource);
        store.preferences("alice");
        store.preferences("bob");
    }

    @AfterEach
    void close() throws Exception
    {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
    }

    @Test
    void isolatesAccountsAndAuditsCrud() throws Exception
    {
        CalendarEvent saved = store.save("alice", draft("Meeting"));
        assertEquals("Meeting", store.find("alice", saved.id()).title());
        assertThrows(NoSuchElementException.class, () -> store.find("bob", saved.id()));
        assertThrows(NoSuchElementException.class, () -> store.delete("bob", saved.id(), 1));
        assertTrue(store.events("bob").isEmpty());
        CalendarEvent changed = store.save("alice", copy(saved, "Updated", saved.version()));
        assertEquals(2, changed.version());
        store.delete("alice", changed.id(), changed.version());
        assertTrue(store.events("alice").isEmpty());
        assertEquals(3, store.history("alice", 0).size());
        assertTrue(store.history("bob", 0).isEmpty());
    }

    @Test
    void staleWritesDoNotChangeEventsOrHistory() throws Exception
    {
        CalendarEvent saved = store.save("alice", draft("Original"));
        store.save("alice", copy(saved, "First edit", 1));
        assertThrows(CalendarStore.ConflictException.class, () -> store.save("alice", copy(saved, "Stale edit", 1)));
        assertThrows(CalendarStore.ConflictException.class, () -> store.delete("alice", saved.id(), 1));
        assertEquals("First edit", store.find("alice", saved.id()).title());
        assertEquals(2, store.history("alice", 0).size());
    }

    @Test
    void preferencesPersistPerUser() throws Exception
    {
        CalendarPreferences preferences = new CalendarPreferences("America/New_York", 1, "agenda");
        store.savePreferences("alice", preferences);
        assertEquals(preferences, new CalendarStore(dataSource).preferences("alice"));
        assertEquals(CalendarPreferences.defaults(), store.preferences("bob"));
        store.savePreferences("alice", new CalendarPreferences("UTC", 7, "month"));
        assertEquals("UTC", store.preferences("alice").zoneId());
    }

    @Test
    void importsAtomicallyAndSkipsDuplicateUids() throws Exception
    {
        CalendarEvent draft = draft("Imported");
        assertEquals(new CalendarStore.ImportResult(1, 0), store.importEvents("alice", List.of(draft)));
        assertEquals(new CalendarStore.ImportResult(0, 1), store.importEvents("alice", List.of(draft)));
        assertEquals(1, store.history("alice", 0).size());
        assertThrows(IllegalArgumentException.class, () -> store.importEvents("bob", List.of(draft)));
    }

    @Test
    void notificationMaterializationIsIdempotentAndReadStateIsPrivate() throws Exception
    {
        CalendarEvent saved = store.save("alice", draft("Reminder"));
        store.addNotification(saved, saved.start(), 10);
        store.addNotification(saved, saved.start(), 10);
        assertEquals(1, store.notifications("alice").size());
        long notificationId = store.notifications("alice").get(0).id();
        assertThrows(NoSuchElementException.class, () -> store.markRead("bob", notificationId));
        store.markRead("alice", notificationId);
        assertTrue(store.notifications("alice").get(0).read());
    }

    private CalendarEvent draft(String title)
    {
        return new CalendarEvent(0, "alice", UUID.randomUUID().toString(), title, "Notes", LocalDateTime.of(2026, 10, 23, 10, 0), LocalDateTime.of(2026, 10, 23, 11, 0), "UTC", false, "", 15, 1);
    }

    @Test
    void rendersEscapedEventTitlesAndMondayFirstCalendar() throws Exception
    {
        store.save("alice", draft("<script>alert(1)</script>"));
        jakarta.servlet.http.HttpServletRequest request = org.mockito.Mockito.mock(jakarta.servlet.http.HttpServletRequest.class, org.mockito.Mockito.withSettings().mockMaker(org.mockito.MockMakers.PROXY));
        CalendarPage page = new CalendarPage(request, "/app", "alice", "csrf-token", new CalendarPreferences("UTC", 1, "month"), CalendarMonth.from("2026-10", java.time.LocalDate.of(2026, 10, 23)), store, new CalendarService(store));
        String html = page.render("month");
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertFalse(html.contains("<script>"));
        assertTrue(html.indexOf("title=\"Monday\"") < html.indexOf("title=\"Sunday\""));
        assertTrue(html.contains("/app/calendar/action"));
        assertTrue(html.contains("csrf-token"));
        CalendarPage hostileAttributes = new CalendarPage(request, "/app", "alice", "csrf-token\"><img src=x>", new CalendarPreferences("UTC", 1, "month"), CalendarMonth.from("2026-10", java.time.LocalDate.of(2026, 10, 23)), store, new CalendarService(store));
        String hostileHtml = hostileAttributes.render("month");
        org.jsoup.nodes.Document parsed = org.jsoup.Jsoup.parseBodyFragment(hostileHtml);
        assertTrue(parsed.select("img").isEmpty());
        assertEquals("csrf-token\"><img src=x>", parsed.selectFirst("input[name=csrf]").attr("value"));
    }

    private CalendarEvent copy(CalendarEvent event, String title, long version)
    {
        return new CalendarEvent(event.id(), event.username(), event.uid(), title, event.description(), event.start(), event.end(), event.zoneId(), event.allDay(), event.recurrenceRule(), event.reminderMinutes(), version);
    }

    @Test
    void dueRemindersRecoverOnceAndKeepReadStateAcrossRefreshes() throws Exception
    {
        CalendarEvent event = store.save("alice", draft("Due soon"));
        CalendarService early = new CalendarService(store, java.time.Clock.fixed(java.time.Instant.parse("2026-10-23T09:40:00Z"), java.time.ZoneOffset.UTC));
        early.refreshReminders("alice");
        assertTrue(store.notifications("alice").isEmpty());
        CalendarService due = new CalendarService(store, java.time.Clock.fixed(java.time.Instant.parse("2026-10-23T09:50:00Z"), java.time.ZoneOffset.UTC));
        due.refreshReminders("alice");
        assertEquals(1, store.notifications("alice").size());
        store.markRead("alice", store.notifications("alice").get(0).id());
        due.refreshReminders("alice");
        assertEquals(1, store.notifications("alice").size());
        assertTrue(store.notifications("alice").get(0).read());
        assertTrue(store.notifications("bob").isEmpty());
    }

    @Test
    void allDayUntilRecurrenceRoundTripsWithoutShiftingDates() throws Exception
    {
        CalendarEvent source = new CalendarEvent(0, "alice", "all-day-uid", "Holiday", "", LocalDateTime.of(2026, 10, 23, 0, 0), LocalDateTime.of(2026, 10, 24, 0, 0), "Pacific/Auckland", true, "FREQ=DAILY;UNTIL=20261026T235959", -1, 1);
        CalendarEvent saved = store.save("alice", source);
        new CalendarService(store).changeOccurrence("alice", saved.id(), 1, source.start().plusDays(1), null);
        byte[] data = new CalendarExchange().export("alice", store.events("alice"), store);
        CalendarExchange.Preview preview = new CalendarExchange().preview(new java.io.ByteArrayInputStream(data), "bob", "America/Los_Angeles");
        assertTrue(preview.events().get(0).allDay());
        assertEquals(source.start(), preview.events().get(0).start());
        assertEquals(source.end(), preview.events().get(0).end());
        store.importCalendar("bob", preview.events(), preview.exceptions());
        assertEquals(3, new CalendarService(store).search("bob", java.time.LocalDate.of(2026, 10, 23), java.time.LocalDate.of(2026, 10, 27), "holiday").size());
    }

    @Test
    void utcRecurringExportUsesUtcValuesWithoutTimezoneParameters() throws Exception
    {
        CalendarEvent source = draft("UTC meeting");
        CalendarEvent saved = store.save("alice", new CalendarEvent(0, "alice", source.uid(), source.title(), source.description(), source.start(), source.end(), "UTC", false, "FREQ=DAILY;COUNT=3", 15, 1));
        new CalendarService(store).changeOccurrence("alice", saved.id(), 1, saved.start(), null);
        byte[] bytes = new CalendarExchange().export("alice", store.events("alice"), store);
        String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(text.contains("DTSTART:20261023T100000Z"));
        assertFalse(text.contains("TZID=Z"));
        CalendarExchange.Preview preview = new CalendarExchange().preview(new java.io.ByteArrayInputStream(bytes), "bob", "UTC");
        assertEquals(saved.start(), preview.events().get(0).start());
        assertEquals(java.util.Set.of(saved.start()), preview.exceptions().get(saved.uid()));
    }

    @Test
    void deletingAnEntireSeriesAlsoDeletesEditedOccurrencesAndAuditsThem() throws Exception
    {
        CalendarEvent source = draft("Series");
        CalendarEvent saved = store.save("alice", new CalendarEvent(0, "alice", source.uid(), source.title(), "", source.start(), source.end(), "UTC", false, "FREQ=DAILY;COUNT=3", -1, 1));
        new CalendarService(store).changeOccurrence("alice", saved.id(), 1, saved.start(), draft("Edited occurrence"));
        assertEquals(2, store.events("alice").size());
        assertThrows(CalendarStore.ConflictException.class, () -> store.delete("alice", saved.id(), 1));
        assertEquals(2, store.events("alice").size());
        store.delete("alice", saved.id(), 2);
        assertTrue(store.events("alice").isEmpty());
        assertTrue(store.history("alice", 0).stream().anyMatch(entry -> entry.action().equals("DELETE_WITH_SERIES")));
    }

    @Test
    void calendarExchangeRoundTripsTimezoneRecurrenceAndExceptions() throws Exception
    {
        CalendarEvent source = new CalendarEvent(0, "alice", "roundtrip-uid", "Meeting, with notes", "First\nSecond", LocalDateTime.of(2026, 3, 7, 10, 0), LocalDateTime.of(2026, 3, 7, 11, 0), "America/New_York", false, "FREQ=DAILY;COUNT=3", 15, 1);
        CalendarEvent saved = store.save("alice", source);
        new CalendarService(store).changeOccurrence("alice", saved.id(), 1, saved.start().plusDays(1), null);
        byte[] exported = new CalendarExchange().export("alice", store.events("alice"), store);
        CalendarExchange.Preview preview = new CalendarExchange().preview(new java.io.ByteArrayInputStream(exported), "bob", "UTC");
        CalendarEvent imported = preview.events().get(0);
        assertEquals(source.start(), imported.start());
        assertEquals(source.zoneId(), imported.zoneId());
        assertEquals(source.description(), imported.description());
        assertEquals(java.util.Set.of(saved.start().plusDays(1)), preview.exceptions().get(source.uid()));
        assertEquals(1, store.importCalendar("bob", preview.events(), preview.exceptions()).added());
        assertEquals(2, new CalendarService(store).search("bob", java.time.LocalDate.of(2026, 3, 7), java.time.LocalDate.of(2026, 3, 10), "").size());
    }

    /**
     * Parasoft Jtest UTA: Test for exceptions(String, long)
     *
     * @see com.app.calendar.CalendarStore#exceptions(String, long)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testExceptions() throws Throwable
    {
        // Given
        DataSource dataSource2 = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(dataSource2.getConnection()).thenReturn(getConnectionResult);
        CalendarStore underTest = new CalendarStore(dataSource2);

        // When
        String owner = "owner"; // UTA: default value
        long id = 1L; // UTA: default value
        Set<LocalDateTime> result = underTest.exceptions(owner, id);

    }

}