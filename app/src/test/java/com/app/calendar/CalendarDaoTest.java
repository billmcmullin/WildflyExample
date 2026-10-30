package com.app.calendar;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class CalendarDaoTest
{

    @TempDir
    Path directory;

    @Test
    void calendarsAreUserScopedIdempotentAndPersistAcrossDatabaseRestart() throws Exception
    {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:file:" + directory.resolve("calendars"));
        dataSource.setUser("sa");
        Timestamp createdAt;
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE users (username VARCHAR(100) PRIMARY KEY)");
            statement.executeUpdate("INSERT INTO users VALUES ('alice'), ('bob')");
            CalendarDao.initializeSchema(connection);
            CalendarDao.initializeSchema(connection);
            CalendarDao dao = new CalendarDao(dataSource);
            dao.ensureCalendar("alice");
            dao.ensureCalendar("alice");
            dao.ensureCalendar("bob");
            try (ResultSet rows = statement.executeQuery("SELECT username, created_at FROM user_calendars ORDER BY username")) {
                assertTrue(rows.next());
                assertEquals("alice", rows.getString(1));
                createdAt = rows.getTimestamp(2);
                assertNotNull(createdAt);
                assertTrue(rows.next());
                assertEquals("bob", rows.getString(1));
                assertFalse(rows.next());
            }
            assertThrows(java.sql.SQLException.class, () -> dao.ensureCalendar("unknown-user"));
            statement.execute("SHUTDOWN");
        }
        try (Connection reopened = dataSource.getConnection(); Statement statement = reopened.createStatement()) {
            new CalendarDao(dataSource).ensureCalendar("alice");
            try (ResultSet rows = statement.executeQuery("SELECT created_at FROM user_calendars WHERE username = 'alice'")) {
                assertTrue(rows.next());
                assertEquals(createdAt, rows.getTimestamp(1));
            }
            statement.execute("SHUTDOWN");
        }
    }

}