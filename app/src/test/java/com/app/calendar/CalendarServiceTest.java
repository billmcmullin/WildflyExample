package com.app.calendar;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import com.app.calendar.CalendarRecurrence.Occurrence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for CalendarService
 *
 * @see com.app.calendar.CalendarService
 * @author bmcmullin
 */
public class CalendarServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for save(String, CalendarEvent)
     *
     * @see com.app.calendar.CalendarService#save(String, CalendarEvent)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testSave() throws Throwable
    {
        // Given
        CalendarStore store = mock(CalendarStore.class);
        CalendarService underTest = new CalendarService(store);

        // When
        String owner = "owner"; // UTA: default value
        CalendarEvent event = mock(CalendarEvent.class);
        LocalDateTime startResult = mock(LocalDateTime.class);
        when(event.start()).thenReturn(startResult);
        CalendarEvent result = underTest.save(owner, event);

    }

}
