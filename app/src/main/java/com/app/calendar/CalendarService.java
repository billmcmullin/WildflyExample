package com.app.calendar;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

public final class CalendarService {
    private static final String TEXT_SEARCH_SUPPORTS_UP_TO_200_CHARACTERS_AND_A_DATE_RANGE_OF_1_TO_366 = "Search supports up to 200 characters and a date range of 1 to 366 days";
    private static final String TEXT_SYMBOL = " ";
    private static final String TEXT_TOO_MANY_OCCURRENCES_CHOOSE_A_SMALLER_DATE_RANGE_OR_SEARCH_TERM = "Too many occurrences; choose a smaller date range or search term";
    private static final String TEXT_RECURRING_OCCURRENCE_NOT_FOUND = "Recurring occurrence not found";

    private final CalendarStore store;
    private final Clock clock;

    public CalendarService(CalendarStore store) {
        this(store, Clock.systemUTC());
    }

    public CalendarService(CalendarStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    public CalendarEvent save(String owner, CalendarEvent event) throws SQLException {
        CalendarRecurrence.validate(CalendarRecurrence.canonicalize(event.recurrenceRule()), event.start());
        return store.save(owner, event);
    }

    public List<CalendarRecurrence.Occurrence> search(String owner, LocalDate from, LocalDate untilExclusive, String query) throws SQLException {
        if (query.length() > 200 || !untilExclusive.isAfter(from) || untilExclusive.isAfter(from.plusDays(366))) {
            throw new IllegalArgumentException(CalendarService.TEXT_SEARCH_SUPPORTS_UP_TO_200_CHARACTERS_AND_A_DATE_RANGE_OF_1_TO_366);
        }
        ZoneId zone = ZoneId.of(store.preferences(owner).zoneId());
        List<CalendarRecurrence.Occurrence> results = new ArrayList<>();
        String needle = query.toLowerCase(Locale.ROOT);
        for (CalendarEvent event : store.events(owner)) {
            if (!(event.title() + CalendarService.TEXT_SYMBOL + event.description()).toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            results.addAll(CalendarRecurrence.expand(event, from, untilExclusive, zone, store.exceptions(owner, event.id())));
            if (results.size() > 10000) {
                throw new IllegalArgumentException(CalendarService.TEXT_TOO_MANY_OCCURRENCES_CHOOSE_A_SMALLER_DATE_RANGE_OR_SEARCH_TERM);
            }
        }
        results.sort(Comparator.comparing(CalendarRecurrence.Occurrence::start)
                .thenComparingLong(occurrence -> occurrence.event().id()));
        return results;
    }

    public void changeOccurrence(String owner, long id, long version, LocalDateTime original, CalendarEvent replacement) throws SQLException {
        CalendarEvent event = store.find(owner, id);
        boolean exists = !event.recurrenceRule().isEmpty() && CalendarRecurrence.expand(event, original.toLocalDate(),
                original.toLocalDate().plusDays(1), ZoneId.of(event.zoneId()), store.exceptions(owner, id))
                .stream().anyMatch(occurrence -> occurrence.originalStart().equals(original));
        if (!exists) {
            throw new NoSuchElementException(CalendarService.TEXT_RECURRING_OCCURRENCE_NOT_FOUND);
        }
        if (replacement != null) {
            CalendarRecurrence.validate(CalendarRecurrence.canonicalize(replacement.recurrenceRule()), replacement.start());
        }
        store.changeOccurrence(owner, id, version, original, replacement);
    }

    public void refreshReminders(String owner) throws SQLException {
        Instant now = clock.instant();
        Instant earliest = now.minus(7, ChronoUnit.DAYS);
        for (CalendarEvent event : store.events(owner)) {
            if (event.reminderMinutes() < 0) {
                continue;
            }
            ZoneId zone = ZoneId.of(event.zoneId());
            LocalDate from = earliest.atZone(zone).toLocalDate().minusDays(1);
            LocalDate until = now.plus(8, ChronoUnit.DAYS).atZone(zone).toLocalDate();
            for (CalendarRecurrence.Occurrence occurrence : CalendarRecurrence.expand(event, from, until, zone,
                    store.exceptions(owner, event.id()))) {
                Instant due = occurrence.originalStart().atZone(zone).toInstant().minus(event.reminderMinutes(), ChronoUnit.MINUTES);
                if (!due.isAfter(now) && !due.isBefore(earliest)) {
                    store.addNotification(event, occurrence.originalStart(), due.toEpochMilli());
                }
            }
        }
    }
}