package com.app.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

public final class CalendarStore {
    private static final String AUDIT_LINE_SEPARATOR = System.lineSeparator();
    private static final String TEXT_SELECT_ZONE_ID_WEEK_START_DEFAULT_VIEW_FROM_CALENDAR_PREFERENCES = "SELECT zone_id, week_start, default_view FROM calendar_preferences WHERE username = ?";
    private static final String TEXT_MERGE_INTO_CALENDAR_PREFERENCES_AS_TARGET_USING_VALUES_CAST_AS_VA = """
                MERGE INTO calendar_preferences AS target
                USING (VALUES(CAST(? AS VARCHAR(100)), CAST(? AS VARCHAR(100)), CAST(? AS INTEGER), CAST(? AS VARCHAR(16))))
                    AS incoming(username, zone_id, week_start, default_view)
                ON target.username = incoming.username
                WHEN MATCHED THEN UPDATE SET zone_id = incoming.zone_id, week_start = incoming.week_start, default_view = incoming.default_view
                WHEN NOT MATCHED THEN INSERT (username, zone_id, week_start, default_view)
                    VALUES (incoming.username, incoming.zone_id, incoming.week_start, incoming.default_view)
                """;
    private static final String TEXT_SELECT = "SELECT ";
    private static final String TEXT_FROM_CALENDAR_EVENTS_WHERE_USERNAME_ORDER_BY_START_LOCAL_ID_FETCH = " FROM calendar_events WHERE username = ? ORDER BY start_local, id FETCH FIRST 1001 ROWS ONLY";
    private static final String TEXT_CALENDAR_EVENT_LIMIT_EXCEEDED = "Calendar event limit exceeded";
    private static final String TEXT_EVENT_OWNER_CANNOT_BE_CHANGED = "Event owner cannot be changed";
    private static final String TEXT_CREATE = "CREATE";
    private static final String TEXT_UPDATE_CALENDAR_EVENTS_SET_TITLE_DESCRIPTION_START_LOCAL_END_LOCA = """
                    UPDATE calendar_events SET title=?, description=?, start_local=?, end_local=?, zone_id=?, all_day=?,
                        recurrence_rule=?, reminder_minutes=?, version=version+1 WHERE id=? AND username=? AND version=?
                    """;
    private static final String TEXT_UPDATE = "UPDATE";
    private static final String TEXT_UPDATED_TO = AUDIT_LINE_SEPARATOR + "Updated to:" + AUDIT_LINE_SEPARATOR;
    private static final String TEXT_FROM_CALENDAR_EVENTS_WHERE_USERNAME_AND_SERIES_ID = " FROM calendar_events WHERE username=? AND series_id=?";
    private static final String TEXT_DELETE_FROM_CALENDAR_EVENTS_WHERE_ID_AND_USERNAME_AND_VERSION = "DELETE FROM calendar_events WHERE id=? AND username=? AND version=?";
    private static final String TEXT_DELETE_WITH_SERIES = "DELETE_WITH_SERIES";
    private static final String TEXT_DELETE = "DELETE";
    private static final String TEXT_AN_OCCURRENCE_REPLACEMENT_MUST_BE_A_NEW_NON_REPEATING_EVENT_FOR_T = "An occurrence replacement must be a new, non-repeating event for this owner";
    private static final String TEXT_UPDATE_CALENDAR_EVENTS_SET_VERSION_VERSION_1_WHERE_ID_AND_USERNAM = "UPDATE calendar_events SET version=version+1 WHERE id=? AND username=? AND version=?";
    private static final String TEXT_INSERT_INTO_CALENDAR_EXCEPTIONS_EVENT_ID_OCCURRENCE_START_VALUES = "INSERT INTO calendar_exceptions(event_id, occurrence_start) VALUES (?,?)";
    private static final String TEXT_CREATE_EXCEPTION = "CREATE_EXCEPTION";
    private static final String TEXT_UPDATE_CALENDAR_EVENTS_SET_SERIES_ID_WHERE_ID_AND_USERNAME = "UPDATE calendar_events SET series_id=? WHERE id=? AND username=?";
    private static final String TEXT_SKIP_OCCURRENCE = "SKIP_OCCURRENCE";
    private static final String TEXT_EDIT_OCCURRENCE = "EDIT_OCCURRENCE";
    private static final String TEXT_SELECT_EXCEPTIONS_OCCURRENCE_START_FROM_CALENDAR_EXCEPTIONS_AS_EX = """
                SELECT exceptions.occurrence_start FROM calendar_exceptions AS exceptions
                JOIN calendar_events AS events ON events.id=exceptions.event_id WHERE events.username=? AND events.id=?
                """;
    private static final String TEXT_IMPORT_SUPPORTS_AT_MOST_200_EVENTS_OWNED_BY_THE_SIGNED_IN_ACCOUNT = "Import supports at most 200 events owned by the signed-in account";
    private static final String TEXT_SELECT_ID_FROM_CALENDAR_EVENTS_WHERE_USERNAME_AND_EVENT_UID = "SELECT id FROM calendar_events WHERE username=? AND event_uid=?";
    private static final String TEXT_IMPORT = "IMPORT";
    private static final String TEXT_SELECT_ID_EVENT_ID_ACTION_TITLE_DETAILS_CHANGED_MS_FROM_CALENDAR = """
                SELECT id, event_id, action, title, details, changed_ms FROM calendar_audit
                WHERE username=? ORDER BY changed_ms DESC, id DESC OFFSET ? ROWS FETCH NEXT 50 ROWS ONLY
                """;
    private static final String TEXT_SELECT_ID_EVENT_ID_TITLE_OCCURRENCE_START_DUE_MS_IS_READ_FROM_CAL = """
                SELECT id, event_id, title, occurrence_start, due_ms, is_read FROM calendar_notifications
                WHERE username=? ORDER BY is_read, due_ms DESC, id DESC FETCH FIRST 100 ROWS ONLY
                """;
    private static final String TEXT_UPDATE_CALENDAR_NOTIFICATIONS_SET_IS_READ_TRUE_WHERE_ID_AND_USERN = "UPDATE calendar_notifications SET is_read=TRUE WHERE id=? AND username=?";
    private static final String TEXT_NOTIFICATION_NOT_FOUND = "Notification not found";
    private static final String TEXT_INSERT_INTO_CALENDAR_NOTIFICATIONS_EVENT_ID_USERNAME_OCCURRENCE_S = """
                INSERT INTO calendar_notifications(event_id, username, occurrence_start, title, due_ms)
                SELECT id, username, ?, title, ? FROM calendar_events WHERE id=? AND username=? AND version=?
                AND NOT EXISTS (SELECT 1 FROM calendar_notifications WHERE event_id=? AND occurrence_start=?)
                """;
    private static final String TEXT_23505 = "23505";
    private static final String TEXT_SELECT_DISTINCT_USERNAME_FROM_CALENDAR_EVENTS_WHERE_REMINDER_MINU = "SELECT DISTINCT username FROM calendar_events WHERE reminder_minutes >= 0 ORDER BY username";
    private static final String TEXT_SELECT_COUNT_FROM_CALENDAR_EVENTS_WHERE_USERNAME = "SELECT COUNT(*) FROM calendar_events WHERE username=?";
    private static final String TEXT_A_CALENDAR_CAN_CONTAIN_AT_MOST_1000_EVENT_SERIES = "A calendar can contain at most 1000 event series";
    private static final String TEXT_INSERT_INTO_CALENDAR_EVENTS_TITLE_DESCRIPTION_START_LOCAL_END_LOC = """
                INSERT INTO calendar_events(title, description, start_local, end_local, zone_id, all_day,
                    recurrence_rule, reminder_minutes, username, event_uid) VALUES (?,?,?,?,?,?,?,?,?,?)
                """;
    private static final String TEXT_EVENT_IDENTIFIER_WAS_NOT_GENERATED = "Event identifier was not generated";
    private static final String TEXT_FROM_CALENDAR_EVENTS_WHERE_ID_AND_USERNAME = " FROM calendar_events WHERE id=? AND username=?";
    private static final String TEXT_EVENT_NOT_FOUND = "Event not found";
    private static final String TEXT_TO = " to ";
    private static final String TEXT_SYMBOL = " [";
    private static final String TEXT_SYMBOL_B4D = ']' + AUDIT_LINE_SEPARATOR;
    private static final String TEXT_SYMBOL_A = AUDIT_LINE_SEPARATOR;
    private static final String TEXT_ALL_DAY = AUDIT_LINE_SEPARATOR + "All day: ";
    private static final String TEXT_RECURRENCE = AUDIT_LINE_SEPARATOR + "Recurrence: ";
    private static final String TEXT_REMINDER_MINUTES = AUDIT_LINE_SEPARATOR + "Reminder minutes: ";
    private static final String TEXT_INSERT_INTO_CALENDAR_AUDIT_USERNAME_EVENT_ID_ACTION_TITLE_DETAILS = "INSERT INTO calendar_audit(username,event_id,action,title,details,changed_ms) VALUES (?,?,?,?,?,?)";
    private static final String TEXT_DELETE_FROM_CALENDAR_NOTIFICATIONS_WHERE_EVENT_ID_AND_IS_READ_FAL = "DELETE FROM calendar_notifications WHERE event_id=? AND is_read=FALSE";
    private static final String TEXT_THIS_EVENT_CHANGED_IN_ANOTHER_TAB_RELOAD_BEFORE_SAVING = "This event changed in another tab. Reload before saving.";


    private static final String COLUMNS = "id, username, event_uid, title, description, start_local, end_local, "
            + "zone_id, all_day, recurrence_rule, reminder_minutes, version";
    private final DataSource dataSource;

    public CalendarStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CalendarPreferences preferences(String owner) throws SQLException {
        new CalendarDao(dataSource).ensureCalendar(owner);
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                CalendarStore.TEXT_SELECT_ZONE_ID_WEEK_START_DEFAULT_VIEW_FROM_CALENDAR_PREFERENCES)) {
            statement.setString(1, owner);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? new CalendarPreferences(rows.getString(1), rows.getInt(2), rows.getString(3))
                        : CalendarPreferences.defaults();
            }
        }
    }

    public void savePreferences(String owner, CalendarPreferences preferences) throws SQLException {
        new CalendarDao(dataSource).ensureCalendar(owner);
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_MERGE_INTO_CALENDAR_PREFERENCES_AS_TARGET_USING_VALUES_CAST_AS_VA)) {
            statement.setString(1, owner);
            statement.setString(2, preferences.zoneId());
            statement.setInt(3, preferences.weekStart());
            statement.setString(4, preferences.defaultView());
            statement.executeUpdate();
        }
    }

    public List<CalendarEvent> events(String owner) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                CalendarStore.TEXT_SELECT + COLUMNS + CalendarStore.TEXT_FROM_CALENDAR_EVENTS_WHERE_USERNAME_ORDER_BY_START_LOCAL_ID_FETCH)) {
            statement.setString(1, owner);
            List<CalendarEvent> events = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    events.add(readEvent(rows));
                }
            }
            if (events.size() > 1000) {
                throw new IllegalStateException(CalendarStore.TEXT_CALENDAR_EVENT_LIMIT_EXCEEDED);
            }
            return events;
        }
    }

    public CalendarEvent find(String owner, long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return find(connection, owner, id);
        }
    }

    public CalendarEvent save(String owner, CalendarEvent event) throws SQLException {
        if (!owner.equals(event.username())) {
            throw new IllegalArgumentException(CalendarStore.TEXT_EVENT_OWNER_CANNOT_BE_CHANGED);
        }
        return transaction(connection -> {
            if (event.id() == 0) {
                return insert(connection, event, CalendarStore.TEXT_CREATE);
            }
            CalendarEvent previous = find(connection, owner, event.id());
            try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_UPDATE_CALENDAR_EVENTS_SET_TITLE_DESCRIPTION_START_LOCAL_END_LOCA)) {
                bindContent(statement, event);
                statement.setLong(9, event.id());
                statement.setString(10, owner);
                statement.setLong(11, event.version());
                requireChanged(statement.executeUpdate());
            }
            clearUnread(connection, event.id());
            CalendarEvent saved = copy(event, event.id(), previous.uid(), event.version() + 1);
            audit(connection, saved, CalendarStore.TEXT_UPDATE, snapshot(previous) + CalendarStore.TEXT_UPDATED_TO + snapshot(saved));
            return saved;
        });
    }

    public void delete(String owner, long id, long version) throws SQLException {
        transaction(connection -> {
            CalendarEvent event = find(connection, owner, id);
            List<CalendarEvent> replacements = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    CalendarStore.TEXT_SELECT + COLUMNS + CalendarStore.TEXT_FROM_CALENDAR_EVENTS_WHERE_USERNAME_AND_SERIES_ID)) {
                statement.setString(1, owner);
                statement.setLong(2, id);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        replacements.add(readEvent(rows));
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_DELETE_FROM_CALENDAR_EVENTS_WHERE_ID_AND_USERNAME_AND_VERSION)) {
                statement.setLong(1, id);
                statement.setString(2, owner);
                statement.setLong(3, version);
                requireChanged(statement.executeUpdate());
            }
            for (CalendarEvent replacement : replacements) {
                audit(connection, replacement, CalendarStore.TEXT_DELETE_WITH_SERIES, snapshot(replacement));
            }
            audit(connection, event, CalendarStore.TEXT_DELETE, snapshot(event));
            return null;
        });
    }

    public void changeOccurrence(String owner, long id, long version, LocalDateTime occurrence, CalendarEvent replacement) throws SQLException {
        if (replacement != null && (!owner.equals(replacement.username()) || replacement.id() != 0
                || !replacement.recurrenceRule().isEmpty())) {
            throw new IllegalArgumentException(CalendarStore.TEXT_AN_OCCURRENCE_REPLACEMENT_MUST_BE_A_NEW_NON_REPEATING_EVENT_FOR_T);
        }
        transaction(connection -> {
            CalendarEvent series = find(connection, owner, id);
            try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_UPDATE_CALENDAR_EVENTS_SET_VERSION_VERSION_1_WHERE_ID_AND_USERNAM)) {
                statement.setLong(1, id);
                statement.setString(2, owner);
                statement.setLong(3, version);
                requireChanged(statement.executeUpdate());
            }
            try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_INSERT_INTO_CALENDAR_EXCEPTIONS_EVENT_ID_OCCURRENCE_START_VALUES)) {
                statement.setLong(1, id);
                statement.setTimestamp(2, Timestamp.valueOf(occurrence));
                statement.executeUpdate();
            }
            clearUnread(connection, id);
            if (replacement != null) {
                CalendarEvent saved = insert(connection, replacement, CalendarStore.TEXT_CREATE_EXCEPTION);
                try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_UPDATE_CALENDAR_EVENTS_SET_SERIES_ID_WHERE_ID_AND_USERNAME)) {
                    statement.setLong(1, id);
                    statement.setLong(2, saved.id());
                    statement.setString(3, owner);
                    statement.executeUpdate();
                }
            }
            audit(connection, series, replacement == null ? CalendarStore.TEXT_SKIP_OCCURRENCE : CalendarStore.TEXT_EDIT_OCCURRENCE, occurrence.toString());
            return null;
        });
    }

    public Set<LocalDateTime> exceptions(String owner, long id) throws SQLException {
        Set<LocalDateTime> exceptions = new HashSet<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_SELECT_EXCEPTIONS_OCCURRENCE_START_FROM_CALENDAR_EXCEPTIONS_AS_EX)) {
            statement.setString(1, owner);
            statement.setLong(2, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    exceptions.add(rows.getTimestamp(1).toLocalDateTime());
                }
            }
        }
        return exceptions;
    }

    public ImportResult importEvents(String owner, List<CalendarEvent> events) throws SQLException {
        return importCalendar(owner, events, java.util.Map.of());
        }

        public ImportResult importCalendar(String owner, List<CalendarEvent> events,
            java.util.Map<String, Set<LocalDateTime>> exceptions) throws SQLException {
        if (events.size() > 200 || events.stream().anyMatch(event -> !owner.equals(event.username()) || event.id() != 0)) {
            throw new IllegalArgumentException(CalendarStore.TEXT_IMPORT_SUPPORTS_AT_MOST_200_EVENTS_OWNED_BY_THE_SIGNED_IN_ACCOUNT);
        }
        return transaction(connection -> {
            int added = 0;
            int skipped = 0;
            for (CalendarEvent event : events) {
                try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_SELECT_ID_FROM_CALENDAR_EVENTS_WHERE_USERNAME_AND_EVENT_UID)) {
                    statement.setString(1, owner);
                    statement.setString(2, event.uid());
                    try (ResultSet rows = statement.executeQuery()) {
                        if (rows.next()) {
                            skipped++;
                            continue;
                        }
                    }
                }
                CalendarEvent saved = insert(connection, event, CalendarStore.TEXT_IMPORT);
                for (LocalDateTime excluded : exceptions.getOrDefault(event.uid(), Set.of())) {
                    try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_INSERT_INTO_CALENDAR_EXCEPTIONS_EVENT_ID_OCCURRENCE_START_VALUES)) {
                        statement.setLong(1, saved.id());
                        statement.setTimestamp(2, Timestamp.valueOf(excluded));
                        statement.executeUpdate();
                    }
                }
                added++;
            }
            return new ImportResult(added, skipped);
        });
    }

    public List<AuditEntry> history(String owner, int page) throws SQLException {
        List<AuditEntry> history = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_SELECT_ID_EVENT_ID_ACTION_TITLE_DETAILS_CHANGED_MS_FROM_CALENDAR)) {
            statement.setString(1, owner);
            statement.setInt(2, Math.multiplyExact(page, 50));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    history.add(new AuditEntry(rows.getLong(1), rows.getLong(2), rows.getString(3), rows.getString(4), rows.getString(5), rows.getLong(6)));
                }
            }
        }
        return history;
    }

    public List<Notification> notifications(String owner) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_SELECT_ID_EVENT_ID_TITLE_OCCURRENCE_START_DUE_MS_IS_READ_FROM_CAL)) {
            statement.setString(1, owner);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    notifications.add(new Notification(rows.getLong(1), rows.getLong(2), rows.getString(3),
                            rows.getTimestamp(4).toLocalDateTime(), rows.getLong(5), rows.getBoolean(6)));
                }
            }
        }
        return notifications;
    }

    public void markRead(String owner, long id) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                CalendarStore.TEXT_UPDATE_CALENDAR_NOTIFICATIONS_SET_IS_READ_TRUE_WHERE_ID_AND_USERN)) {
            statement.setLong(1, id);
            statement.setString(2, owner);
            if (statement.executeUpdate() != 1) {
                throw new NoSuchElementException(CalendarStore.TEXT_NOTIFICATION_NOT_FOUND);
            }
        }
    }

    public void addNotification(CalendarEvent event, LocalDateTime occurrence, long dueMillis) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_INSERT_INTO_CALENDAR_NOTIFICATIONS_EVENT_ID_USERNAME_OCCURRENCE_S)) {
            statement.setTimestamp(1, Timestamp.valueOf(occurrence));
            statement.setLong(2, dueMillis);
            statement.setLong(3, event.id());
            statement.setString(4, event.username());
            statement.setLong(5, event.version());
            statement.setLong(6, event.id());
            statement.setTimestamp(7, Timestamp.valueOf(occurrence));
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (!CalendarStore.TEXT_23505.equals(exception.getSQLState())) {
                throw exception;
            }
        }
    }

    public List<String> reminderOwners() throws SQLException {
        List<String> owners = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                CalendarStore.TEXT_SELECT_DISTINCT_USERNAME_FROM_CALENDAR_EVENTS_WHERE_REMINDER_MINU);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                owners.add(rows.getString(1));
            }
        }
        return owners;
    }

    private CalendarEvent insert(Connection connection, CalendarEvent event, String action) throws SQLException {
        try (PreparedStatement count = connection.prepareStatement(CalendarStore.TEXT_SELECT_COUNT_FROM_CALENDAR_EVENTS_WHERE_USERNAME)) {
            count.setString(1, event.username());
            try (ResultSet rows = count.executeQuery()) {
                rows.next();
                if (rows.getInt(1) >= 1000) {
                    throw new IllegalArgumentException(CalendarStore.TEXT_A_CALENDAR_CAN_CONTAIN_AT_MOST_1000_EVENT_SERIES);
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_INSERT_INTO_CALENDAR_EVENTS_TITLE_DESCRIPTION_START_LOCAL_END_LOC, Statement.RETURN_GENERATED_KEYS)) {
            bindContent(statement, event);
            statement.setString(9, event.username());
            statement.setString(10, event.uid());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(CalendarStore.TEXT_EVENT_IDENTIFIER_WAS_NOT_GENERATED);
                }
                CalendarEvent saved = copy(event, keys.getLong(1), event.uid(), 1);
                audit(connection, saved, action, snapshot(saved));
                return saved;
            }
        }
    }

    private CalendarEvent find(Connection connection, String owner, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_SELECT + COLUMNS + CalendarStore.TEXT_FROM_CALENDAR_EVENTS_WHERE_ID_AND_USERNAME)) {
            statement.setLong(1, id);
            statement.setString(2, owner);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new NoSuchElementException(CalendarStore.TEXT_EVENT_NOT_FOUND);
                }
                return readEvent(rows);
            }
        }
    }

    private static CalendarEvent readEvent(ResultSet rows) throws SQLException {
        return new CalendarEvent(rows.getLong(1), rows.getString(2), rows.getString(3), rows.getString(4), rows.getString(5),
                rows.getTimestamp(6).toLocalDateTime(), rows.getTimestamp(7).toLocalDateTime(), rows.getString(8),
                rows.getBoolean(9), rows.getString(10), rows.getInt(11), rows.getLong(12));
    }

    private static void bindContent(PreparedStatement statement, CalendarEvent event) throws SQLException {
        statement.setString(1, event.title());
        statement.setString(2, event.description());
        statement.setTimestamp(3, Timestamp.valueOf(event.start()));
        statement.setTimestamp(4, Timestamp.valueOf(event.end()));
        statement.setString(5, event.zoneId());
        statement.setBoolean(6, event.allDay());
        statement.setString(7, event.recurrenceRule());
        statement.setInt(8, event.reminderMinutes());
    }

    private static CalendarEvent copy(CalendarEvent event, long id, String uid, long version) {
        return new CalendarEvent(id, event.username(), uid, event.title(), event.description(), event.start(), event.end(),
                event.zoneId(), event.allDay(), event.recurrenceRule(), event.reminderMinutes(), version);
    }

    private static String snapshot(CalendarEvent event) {
        return event.start() + CalendarStore.TEXT_TO + event.end() + CalendarStore.TEXT_SYMBOL + event.zoneId() + CalendarStore.TEXT_SYMBOL_B4D + event.title() + CalendarStore.TEXT_SYMBOL_A
                + event.description() + CalendarStore.TEXT_ALL_DAY + event.allDay() + CalendarStore.TEXT_RECURRENCE + event.recurrenceRule()
                + CalendarStore.TEXT_REMINDER_MINUTES + event.reminderMinutes();
    }

    private static void audit(Connection connection, CalendarEvent event, String action, String details) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                CalendarStore.TEXT_INSERT_INTO_CALENDAR_AUDIT_USERNAME_EVENT_ID_ACTION_TITLE_DETAILS)) {
            statement.setString(1, event.username());
            statement.setLong(2, event.id());
            statement.setString(3, action);
            statement.setString(4, event.title());
            statement.setString(5, details.length() > 12000 ? details.substring(0, 12000) : details);
            statement.setLong(6, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    private static void clearUnread(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(CalendarStore.TEXT_DELETE_FROM_CALENDAR_NOTIFICATIONS_WHERE_EVENT_ID_AND_IS_READ_FAL)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private static void requireChanged(int rows) {
        if (rows != 1) {
            throw new ConflictException(CalendarStore.TEXT_THIS_EVENT_CHANGED_IN_ANOTHER_TAB_RELOAD_BEFORE_SAVING);
        }
    }

    private <Result> Result transaction(DatabaseWork<Result> work) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Result result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    @FunctionalInterface
    private interface DatabaseWork<Result> {
        Result run(Connection connection) throws SQLException;
    }

    public static final class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }

    public record ImportResult(int added, int skipped) { }
    public record AuditEntry(long id, long eventId, String action, String title, String details, long changedMillis) { }
    public record Notification(long id, long eventId, String title, LocalDateTime occurrence, long dueMillis, boolean read) { }
}