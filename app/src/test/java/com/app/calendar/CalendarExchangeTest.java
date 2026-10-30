package com.app.calendar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import com.app.calendar.CalendarExchange.Preview;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
class CalendarExchangeTest
{
    @Test
    void parsesSupportedEventsIntoTheSignedInAccount() throws Exception
    {
        CalendarExchange.Preview preview = preview("SUMMARY:Meeting\r\nDESCRIPTION:First\\nSecond\r\nRRULE:FREQ=WEEKLY;COUNT=3\r\n");
        assertEquals("alice", preview.events().get(0).username());
        assertEquals("First\nSecond", preview.events().get(0).description());
        assertTrue(preview.events().get(0).recurrenceRule().contains("COUNT=3"));
    }

    @Test
    void rejectsUnsupportedRecurrencePropertiesAndOversizeDocuments()
    {
        assertThrows(IllegalArgumentException.class, () -> preview("SUMMARY:Meeting\r\nRRULE:FREQ=HOURLY;COUNT=3\r\n"));
        assertThrows(IllegalArgumentException.class, () -> preview("SUMMARY:Meeting\r\nATTENDEE:mailto:user@example.com\r\n"));
        assertThrows(IllegalArgumentException.class, () -> new CalendarExchange().preview(
                new ByteArrayInputStream(new byte[CalendarExchange.MAX_BYTES + 1]), "alice", "UTC"));
    }

    private CalendarExchange.Preview preview(String properties) throws Exception
    {
        String source = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Test//EN\r\nBEGIN:VEVENT\r\nUID:test-id\r\n" + "DTSTAMP:20261023T000000Z\r\nDTSTART:20261023T100000Z\r\nDTEND:20261023T110000Z\r\n" + properties + "END:VEVENT\r\nEND:VCALENDAR\r\n";
        return new CalendarExchange().preview(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)), "alice", "UTC");
    }

    /**
     * Parasoft Jtest UTA: Test for preview(InputStream, String, String)
     *
     * @see com.app.calendar.CalendarExchange#preview(InputStream, String, String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testPreview() throws Throwable
    {
        // UTA is unable to resolve the values required to create the requested test case.
        // A test case with default input values has been created.

        // Given
        CalendarExchange underTest = new CalendarExchange();

        // When
        InputStream input = mock(InputStream.class);
        String owner = "owner"; // UTA: default value
        String defaultZone = "defaultZone"; // UTA: default value
        Preview result = underTest.preview(input, owner, defaultZone);

    }
}