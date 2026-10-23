package com.app.calendar;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.model.Recur;

public final class CalendarRecurrence {
    private static final String NON_CANONICAL_RULE = "Recurrence rules must use their canonical representation";
    private static final String TEXT_YYYY_MMDD_T_HHMMSS = "yyyyMMdd'T'HHmmss";
    private static final String TEXT_NONE = "NONE";
    private static final String EMPTY_TEXT = "";
    private static final String TEXT_FREQ = "FREQ=";
    private static final String TEXT_COUNT = ";COUNT=";
    private static final String TEXT_UNTIL = ";UNTIL=";
    private static final String TEXT_DAILY = "DAILY";
    private static final String TEXT_WEEKLY = "WEEKLY";
    private static final String TEXT_MONTHLY = "MONTHLY";
    private static final String TEXT_FREQ_210E78 = "FREQ";
    private static final String TEXT_COUNT_3D558EF = "COUNT";
    private static final String TEXT_UNTIL_4D2873E = "UNTIL";
    private static final String TEXT_USE_DAILY_WEEKLY_OR_MONTHLY_RECURRENCE_WITH_A_COUNT_OR_END_DATE = "Use daily, weekly, or monthly recurrence with a count or end date";
    private static final String TEXT_RECURRENCE_COUNT_MUST_BE_BETWEEN_1_AND_999 = "Recurrence count must be between 1 and 999";
    private static final String TEXT_RECURRENCE_MUST_END_AFTER_ITS_START_AND_WITHIN_FIVE_YEARS = "Recurrence must end after its start and within five years";
    private static final String TEXT_SYMBOL = ";";
    private static final String TEXT_SYMBOL_3D = "=";
    private static final String TEXT_UNSUPPORTED_RECURRENCE_RULE = "Unsupported recurrence rule";
    private static final String TEXT_CHOOSE_A_DATE_RANGE_OF_1_TO_366_DAYS = "Choose a date range of 1 to 366 days";
    private static final String TEXT_Z = "Z";
    private static final String TEXT_TO = " to ";

    private static final DateTimeFormatter RULE_TIME = DateTimeFormatter.ofPattern(CalendarRecurrence.TEXT_YYYY_MMDD_T_HHMMSS);

    private CalendarRecurrence() { }

    public static String rule(String frequency, String count, String until, LocalDateTime start) {
        if (frequency == null || frequency.equals(CalendarRecurrence.TEXT_NONE) || frequency.isBlank()) {
            return CalendarRecurrence.EMPTY_TEXT;
        }
        String rule = CalendarRecurrence.TEXT_FREQ + frequency + (count != null && !count.isBlank()
                ? CalendarRecurrence.TEXT_COUNT + count : CalendarRecurrence.TEXT_UNTIL + LocalDate.parse(until).atTime(23, 59, 59).format(RULE_TIME));
        validate(canonicalize(rule), start);
        return rule;
    }

    public static String canonicalize(String rule) {
        String normalized = Normalizer.normalize(rule, Normalizer.Form.NFKC);
        if (!normalized.equals(rule)) {
            throw new IllegalArgumentException(NON_CANONICAL_RULE);
        }
        return normalized;
    }

    public static void validate(String rule, LocalDateTime start) {
        if (rule.isEmpty()) {
            return;
        }
        Map<String, String> parts = parts(rule);
        String countValue = parts.get(CalendarRecurrence.TEXT_COUNT_3D558EF);
        String untilValue = parts.get(CalendarRecurrence.TEXT_UNTIL_4D2873E);
        if (!Set.of(CalendarRecurrence.TEXT_DAILY, CalendarRecurrence.TEXT_WEEKLY, CalendarRecurrence.TEXT_MONTHLY).contains(parts.getOrDefault(CalendarRecurrence.TEXT_FREQ_210E78, CalendarRecurrence.EMPTY_TEXT))
            || parts.size() != 2 || (countValue != null) == (untilValue != null)) {
            throw new IllegalArgumentException(CalendarRecurrence.TEXT_USE_DAILY_WEEKLY_OR_MONTHLY_RECURRENCE_WITH_A_COUNT_OR_END_DATE);
        }
        Recur<LocalDateTime> parsedRule = new Recur<>(rule);
        if (countValue != null) {
            int count = parsedRule.getCount();
            if (count < 1 || count > 999) {
                throw new IllegalArgumentException(CalendarRecurrence.TEXT_RECURRENCE_COUNT_MUST_BE_BETWEEN_1_AND_999);
            }
        } else {
            LocalDateTime until = LocalDateTime.parse(untilValue, RULE_TIME);
            if (until.isBefore(start) || until.isAfter(start.plusYears(5))) {
                throw new IllegalArgumentException(CalendarRecurrence.TEXT_RECURRENCE_MUST_END_AFTER_ITS_START_AND_WITHIN_FIVE_YEARS);
            }
        }
    }

    public static Map<String, String> parts(String rule) {
        Map<String, String> parts = new HashMap<>();
        if (!rule.isEmpty()) {
            for (String entry : rule.split(CalendarRecurrence.TEXT_SYMBOL)) {
                String[] pair = entry.split(CalendarRecurrence.TEXT_SYMBOL_3D, 2);
                if (pair.length != 2 || !Set.of(CalendarRecurrence.TEXT_FREQ_210E78, CalendarRecurrence.TEXT_COUNT_3D558EF, CalendarRecurrence.TEXT_UNTIL_4D2873E).contains(pair[0])
                        || parts.put(pair[0], pair[1]) != null) {
                    throw new IllegalArgumentException(CalendarRecurrence.TEXT_UNSUPPORTED_RECURRENCE_RULE);
                }
            }
        }
        return parts;
    }

    public static List<Occurrence> expand(CalendarEvent event, LocalDate from, LocalDate untilExclusive,
            ZoneId displayZone, Set<LocalDateTime> exceptions) {
        if (!untilExclusive.isAfter(from) || untilExclusive.isAfter(from.plusDays(366))) {
            throw new IllegalArgumentException(CalendarRecurrence.TEXT_CHOOSE_A_DATE_RANGE_OF_1_TO_366_DAYS);
        }
        validate(canonicalize(event.recurrenceRule()), event.start());
        ZoneId eventZone = ZoneId.of(event.zoneId());
        LocalDateTime searchStart = from.atStartOfDay(displayZone).withZoneSameInstant(eventZone)
                .toLocalDateTime().minusDays(367);
        LocalDateTime searchEnd = untilExclusive.atStartOfDay(displayZone).withZoneSameInstant(eventZone)
                .toLocalDateTime().plusDays(1);
        List<LocalDateTime> starts = event.recurrenceRule().isEmpty() ? List.of(event.start())
                : new Recur<LocalDateTime>(event.recurrenceRule()).getDates(event.start(), searchStart, searchEnd, 1000);
        List<Occurrence> result = new ArrayList<>();
        Duration duration = Duration.between(event.start(), event.end());
        for (LocalDateTime start : starts) {
            LocalDateTime end = start.plus(duration);
            if (exceptions.contains(start) || (!event.allDay() && (eventZone.getRules().getValidOffsets(start).isEmpty()
                    || eventZone.getRules().getValidOffsets(end).isEmpty()))) {
                continue;
            }
            LocalDateTime displayStart = event.allDay() ? start : start.atZone(eventZone).withZoneSameInstant(displayZone).toLocalDateTime();
            LocalDateTime displayEnd = event.allDay() ? end : end.atZone(eventZone).withZoneSameInstant(displayZone).toLocalDateTime();
            if (displayStart.isBefore(untilExclusive.atStartOfDay()) && displayEnd.isAfter(from.atStartOfDay())) {
                result.add(new Occurrence(event, start, displayStart, displayEnd));
            }
        }
        return result;
    }

    public static String exportRule(CalendarEvent event) {
        if (event.recurrenceRule().isEmpty()) {
            return CalendarRecurrence.EMPTY_TEXT;
        }
        Map<String, String> parts = parts(event.recurrenceRule());
        if (parts.containsKey(CalendarRecurrence.TEXT_COUNT_3D558EF)) {
            return event.recurrenceRule();
        }
        LocalDateTime end = LocalDateTime.parse(parts.get(CalendarRecurrence.TEXT_UNTIL_4D2873E), RULE_TIME);
        String until = event.allDay() ? end.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)
            : end.atZone(ZoneId.of(event.zoneId())).withZoneSameInstant(java.time.ZoneOffset.UTC).format(RULE_TIME) + CalendarRecurrence.TEXT_Z;
        return CalendarRecurrence.TEXT_FREQ + parts.get(CalendarRecurrence.TEXT_FREQ_210E78) + CalendarRecurrence.TEXT_UNTIL + until;
    }

    public record Occurrence(CalendarEvent event, LocalDateTime originalStart, LocalDateTime start, LocalDateTime end) {
        public String dateLabel() {
            return event.allDay() ? start.toLocalDate() + CalendarRecurrence.TEXT_TO + end.toLocalDate().minusDays(1)
                    : start.toString().replace('T', ' ') + CalendarRecurrence.TEXT_TO + end.toString().replace('T', ' ');
        }
    }
}