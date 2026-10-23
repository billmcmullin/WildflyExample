package com.app.calendar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

public final class CalendarExchange {
    private static final String TEXT_YYYY_MMDD_T_HHMMSS = "yyyyMMdd'T'HHmmss";
    private static final String TEXT_UID = "UID";
    private static final String TEXT_DTSTAMP = "DTSTAMP";
    private static final String TEXT_DTSTART = "DTSTART";
    private static final String TEXT_DTEND = "DTEND";
    private static final String TEXT_SUMMARY = "SUMMARY";
    private static final String TEXT_DESCRIPTION = "DESCRIPTION";
    private static final String TEXT_RRULE = "RRULE";
    private static final String TEXT_EXDATE = "EXDATE";
    private static final String TEXT_SEQUENCE = "SEQUENCE";
    private static final String TEXT_CREATED = "CREATED";
    private static final String TEXT_LAST_MODIFIED = "LAST-MODIFIED";
    private static final String TEXT_CLASS = "CLASS";
    private static final String TEXT_TRANSP = "TRANSP";
    private static final String TEXT_STATUS = "STATUS";
    private static final String TEXT_X_APP_ZONE = "X-APP-ZONE";
    private static final String TEXT_X_APP_REMINDER = "X-APP-REMINDER";
    private static final String TEXT_IMPORT_FILES_MUST_BE_256_KI_B_OR_SMALLER = "Import files must be 256 KiB or smaller";
    private static final String TEXT_INVALID_I_CALENDAR_DOCUMENT = "Invalid iCalendar document";
    private static final String TEXT_VEVENT = "VEVENT";
    private static final String TEXT_VTIMEZONE = "VTIMEZONE";
    private static final String TEXT_ONLY_EVENTS_AND_TIMEZONE_DEFINITIONS_CAN_BE_IMPORTED = "Only events and timezone definitions can be imported";
    private static final String TEXT_IMPORT_MUST_CONTAIN_BETWEEN_1_AND_200_EVENTS = "Import must contain between 1 and 200 events";
    private static final String TEXT_UNSUPPORTED_EVENT_PROPERTY = "Unsupported event property: ";
    private static final String TEXT_EACH_EVENT_NEEDS_ONE_START_AND_END_ALARMS_AND_DUPLICATE_PROPERTIE = "Each event needs one start and end; alarms and duplicate properties are unsupported";
    private static final String TEXT_CANCELLED = "CANCELLED";
    private static final String EMPTY_TEXT = "";
    private static final String TEXT_CANCELLED_EVENTS_ARE_NOT_IMPORTED = "Cancelled events are not imported";
    private static final String TEXT_EVENT_START_AND_END_MUST_USE_THE_SAME_DATE_TYPE = "Event start and end must use the same date type";
    private static final String TEXT_DUPLICATE_EVENT_UID_IN_IMPORT_DETACHED_RECURRENCE_ID_EVENTS_ARE_N = "Duplicate event UID in import; detached RECURRENCE-ID events are not supported";
    private static final String TEXT_UNTITLED_EVENT = "Untitled event";
    private static final String TEXT_1 = "-1";
    private static final String TEXT_EXDATE_MUST_IDENTIFY_AN_OCCURRENCE_OF_THE_SUPPORTED_RECURRENCE_RU = "EXDATE must identify an occurrence of the supported recurrence rule";
    private static final String TEXT_2_0 = "2.0";
    private static final String TEXT_WILDFLY_EXAMPLE_PRIVATE_CALENDAR_EN = "-//WildflyExample//Private Calendar//EN";
    private static final String TEXT_CANNOT_EXPORT_ANOTHER_ACCOUNT_S_EVENT = "Cannot export another account's event";
    private static final String TEXT_CANNOT_EXPORT_TIMEZONE = "Cannot export timezone: ";
    private static final String TEXT_PRIVATE = "PRIVATE";
    private static final String TEXT_UTC = "UTC";
    private static final String TEXT_USE_UTC_OR_AN_IANA_TIMEZONE_WHEN_EXPORTING_REPEATING_TIMED_EVENTS = "Use UTC or an IANA timezone when exporting repeating timed events";
    private static final String TEXT_Z = "Z";
    private static final String TEXT_UNTIL = "UNTIL";
    private static final String TEXT_SYMBOL = "=";
    private static final String TEXT_SYMBOL_3B = ";";

    public static final int MAX_BYTES = 262144;
    private static final DateTimeFormatter BASIC_TIME = DateTimeFormatter.ofPattern(CalendarExchange.TEXT_YYYY_MMDD_T_HHMMSS);
    private static final Set<String> SUPPORTED = Set.of(CalendarExchange.TEXT_UID, CalendarExchange.TEXT_DTSTAMP, CalendarExchange.TEXT_DTSTART, CalendarExchange.TEXT_DTEND, CalendarExchange.TEXT_SUMMARY, CalendarExchange.TEXT_DESCRIPTION,
            CalendarExchange.TEXT_RRULE, CalendarExchange.TEXT_EXDATE, CalendarExchange.TEXT_SEQUENCE, CalendarExchange.TEXT_CREATED, CalendarExchange.TEXT_LAST_MODIFIED, CalendarExchange.TEXT_CLASS, CalendarExchange.TEXT_TRANSP, CalendarExchange.TEXT_STATUS, CalendarExchange.TEXT_X_APP_ZONE, CalendarExchange.TEXT_X_APP_REMINDER);

    public Preview preview(InputStream input, String owner, String defaultZone) throws IOException {
        byte[] bytes = input.readNBytes(MAX_BYTES + 1);
        if (bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException(CalendarExchange.TEXT_IMPORT_FILES_MUST_BE_256_KI_B_OR_SMALLER);
        }
        net.fortuna.ical4j.model.Calendar calendar;
        try {
            calendar = new CalendarBuilder().build(new ByteArrayInputStream(bytes));
        } catch (ParserException exception) {
            throw new IllegalArgumentException(CalendarExchange.TEXT_INVALID_I_CALENDAR_DOCUMENT, exception);
        }
        List<VEvent> components = calendar.getComponents(CalendarExchange.TEXT_VEVENT);
        if (calendar.getComponents().stream().anyMatch(component -> !component.getName().equals(CalendarExchange.TEXT_VEVENT)
                && !component.getName().equals(CalendarExchange.TEXT_VTIMEZONE))) {
            throw new IllegalArgumentException(CalendarExchange.TEXT_ONLY_EVENTS_AND_TIMEZONE_DEFINITIONS_CAN_BE_IMPORTED);
        }
        if (components.isEmpty() || components.size() > 200) {
            throw new IllegalArgumentException(CalendarExchange.TEXT_IMPORT_MUST_CONTAIN_BETWEEN_1_AND_200_EVENTS);
        }
        List<CalendarEvent> events = new ArrayList<>();
        Map<String, Set<LocalDateTime>> exceptions = new HashMap<>();
        Set<String> uids = new HashSet<>();
        for (VEvent component : components) {
            for (Property property : component.getProperties()) {
                if (!SUPPORTED.contains(property.getName())) {
                    throw new IllegalArgumentException(CalendarExchange.TEXT_UNSUPPORTED_EVENT_PROPERTY + property.getName());
                }
            }
            if (component.getProperties(CalendarExchange.TEXT_DTSTART).size() != 1 || component.getProperties(CalendarExchange.TEXT_DTEND).size() != 1
                    || component.getProperties(CalendarExchange.TEXT_RRULE).size() > 1 || component.getProperties(CalendarExchange.TEXT_UID).size() > 1
                    || !component.getComponents().isEmpty()) {
                throw new IllegalArgumentException(CalendarExchange.TEXT_EACH_EVENT_NEEDS_ONE_START_AND_END_ALARMS_AND_DUPLICATE_PROPERTIE);
            }
            if (CalendarExchange.TEXT_CANCELLED.equals(property(component, CalendarExchange.TEXT_STATUS, CalendarExchange.EMPTY_TEXT))) {
                throw new IllegalArgumentException(CalendarExchange.TEXT_CANCELLED_EVENTS_ARE_NOT_IMPORTED);
            }
            Temporal rawStart = component.<DtStart<?>>getProperty(Property.DTSTART).orElseThrow().getDate();
            Temporal rawEnd = component.getEndDate().orElseThrow().getDate();
            boolean allDay = rawStart instanceof LocalDate;
            if (allDay != (rawEnd instanceof LocalDate)) {
                throw new IllegalArgumentException(CalendarExchange.TEXT_EVENT_START_AND_END_MUST_USE_THE_SAME_DATE_TYPE);
            }
            String zone = property(component, CalendarExchange.TEXT_X_APP_ZONE, zone(rawStart, defaultZone));
            LocalDateTime start = local(rawStart, ZoneId.of(zone));
            LocalDateTime end = local(rawEnd, ZoneId.of(zone));
            String uid = property(component, CalendarExchange.TEXT_UID, UUID.randomUUID().toString());
            if (!uids.add(uid)) {
                throw new IllegalArgumentException(CalendarExchange.TEXT_DUPLICATE_EVENT_UID_IN_IMPORT_DETACHED_RECURRENCE_ID_EVENTS_ARE_N);
            }
            String rule = importRule(property(component, CalendarExchange.TEXT_RRULE, CalendarExchange.EMPTY_TEXT), start, ZoneId.of(zone));
            CalendarEvent event = new CalendarEvent(0, owner, uid, property(component, CalendarExchange.TEXT_SUMMARY, CalendarExchange.TEXT_UNTITLED_EVENT),
                    property(component, CalendarExchange.TEXT_DESCRIPTION, CalendarExchange.EMPTY_TEXT), start, end, zone, allDay, rule,
                    Integer.parseInt(property(component, CalendarExchange.TEXT_X_APP_REMINDER, CalendarExchange.TEXT_1)), 1);
            Set<LocalDateTime> excluded = new HashSet<>();
            for (Property property : component.getProperties(CalendarExchange.TEXT_EXDATE)) {
                ExDate<?> exclusion = (ExDate<?>) property;
                for (Temporal date : exclusion.getDates()) {
                    LocalDateTime excludedDate = local(date, ZoneId.of(zone));
                    if (rule.isEmpty() || CalendarRecurrence.expand(event, excludedDate.toLocalDate(), excludedDate.toLocalDate().plusDays(1),
                            ZoneId.of(zone), Set.of()).stream().noneMatch(occurrence -> occurrence.originalStart().equals(excludedDate))) {
                        throw new IllegalArgumentException(CalendarExchange.TEXT_EXDATE_MUST_IDENTIFY_AN_OCCURRENCE_OF_THE_SUPPORTED_RECURRENCE_RU);
                    }
                    excluded.add(excludedDate);
                }
            }
            events.add(event);
            exceptions.put(uid, excluded);
        }
        return new Preview(List.copyOf(events), Map.copyOf(exceptions));
    }

    public byte[] export(String owner, List<CalendarEvent> events, CalendarStore store) throws SQLException {
        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        Version version = new Version();
        version.setValue(CalendarExchange.TEXT_2_0);
        calendar.add(version);
        calendar.add(new ProdId(CalendarExchange.TEXT_WILDFLY_EXAMPLE_PRIVATE_CALENDAR_EN));
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        Set<String> includedZones = new HashSet<>();
        for (CalendarEvent event : events) {
            if (!owner.equals(event.username())) {
                throw new IllegalArgumentException(CalendarExchange.TEXT_CANNOT_EXPORT_ANOTHER_ACCOUNT_S_EVENT);
            }
            ZoneId zone = ZoneId.of(event.zoneId());
            VEvent component;
            if (event.allDay()) {
                component = new VEvent(event.start().toLocalDate(), event.end().toLocalDate(), event.title());
            } else if (zone.getRules().isFixedOffset()) {
                component = new VEvent(event.start().atZone(zone).toInstant(),
                    event.end().atZone(zone).toInstant(), event.title());
            } else {
                component = new VEvent(event.start().atZone(zone), event.end().atZone(zone), event.title());
                if (includedZones.add(zone.getId())) {
                    net.fortuna.ical4j.model.TimeZone timeZone = registry.getTimeZone(zone.getId());
                    if (timeZone == null) {
                        throw new IllegalArgumentException(CalendarExchange.TEXT_CANNOT_EXPORT_TIMEZONE + zone);
                    }
                    calendar.add(timeZone.getVTimeZone());
                }
            }
            component.add(new Uid(event.uid()));
            if (component.getProperty(Property.DTSTAMP).isEmpty()) {
                component.add(new DtStamp());
            }
            component.add(new Description(event.description()));
            component.add(new Clazz(CalendarExchange.TEXT_PRIVATE));
            component.add(new XProperty(CalendarExchange.TEXT_X_APP_ZONE, event.zoneId()));
            component.add(new XProperty(CalendarExchange.TEXT_X_APP_REMINDER, Integer.toString(event.reminderMinutes())));
            String rule = CalendarRecurrence.exportRule(event);
            if (!rule.isEmpty()) {
                if (!event.allDay() && zone.getRules().isFixedOffset() && !zone.equals(ZoneOffset.UTC) && !zone.equals(ZoneId.of(CalendarExchange.TEXT_UTC))) {
                    throw new IllegalArgumentException(CalendarExchange.TEXT_USE_UTC_OR_AN_IANA_TIMEZONE_WHEN_EXPORTING_REPEATING_TIMED_EVENTS);
                }
                component.add(new RRule<>(rule));
            }
            for (LocalDateTime excluded : store.exceptions(owner, event.id())) {
                component.add(new ExDate<>(event.allDay() ? excluded.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)
                        : excluded.atZone(zone).withZoneSameInstant(ZoneOffset.UTC).format(BASIC_TIME) + CalendarExchange.TEXT_Z));
            }
            calendar.add(component);
        }
        return calendar.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String importRule(String rule, LocalDateTime start, ZoneId zone) {
        if (rule.isEmpty()) {
            return CalendarExchange.EMPTY_TEXT;
        }
        Map<String, String> parts = CalendarRecurrence.parts(rule);
        String value = parts.get(CalendarExchange.TEXT_UNTIL);
        if (value != null) {
            LocalDateTime until = value.length() == 8 ? LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE).atTime(23, 59, 59)
                    : value.endsWith(CalendarExchange.TEXT_Z) ? LocalDateTime.parse(value.substring(0, value.length() - 1), BASIC_TIME)
                            .atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime()
                    : LocalDateTime.parse(value, BASIC_TIME);
            parts.put(CalendarExchange.TEXT_UNTIL, until.format(BASIC_TIME));
        }
        String normalized = parts.entrySet().stream().map(entry -> entry.getKey() + CalendarExchange.TEXT_SYMBOL + entry.getValue())
                .collect(java.util.stream.Collectors.joining(CalendarExchange.TEXT_SYMBOL_3B));
        CalendarRecurrence.validate(CalendarRecurrence.canonicalize(normalized), start);
        return normalized;
    }

    private static String property(VEvent event, String name, String fallback) {
        return event.getProperty(name).map(Property::getValue).orElse(fallback);
    }

    private static String zone(Temporal temporal, String fallback) {
        if (temporal instanceof ZonedDateTime zoned) {
            return zoned.getZone().getId();
        }
        if (temporal instanceof OffsetDateTime offset) {
            return offset.getOffset().getId();
        }
        return temporal instanceof Instant ? CalendarExchange.TEXT_UTC : fallback;
    }

    private static LocalDateTime local(Temporal temporal, ZoneId zone) {
        if (temporal instanceof LocalDate date) {
            return date.atStartOfDay();
        }
        if (temporal instanceof ZonedDateTime zoned) {
            return zoned.withZoneSameInstant(zone).toLocalDateTime();
        }
        if (temporal instanceof OffsetDateTime offset) {
            return offset.atZoneSameInstant(zone).toLocalDateTime();
        }
        if (temporal instanceof Instant instant) {
            return instant.atZone(zone).toLocalDateTime();
        }
        return LocalDateTime.from(temporal);
    }

    public record Preview(List<CalendarEvent> events, Map<String, Set<LocalDateTime>> exceptions) implements Serializable { }
}