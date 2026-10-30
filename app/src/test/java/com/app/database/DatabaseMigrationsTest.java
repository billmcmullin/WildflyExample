package com.app.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.flywaydb.core.api.FlywayException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.io.TempDir;

import com.app.support.JdbcTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockMakers.PROXY;
import static org.mockito.Mockito.*;
class DatabaseMigrationsTest
{

    @TempDir
    Path directory;

    @Test
    void createsFreshDatabaseAndDoesNotRepeatMigrationsAfterRestart() throws Exception
    {
        JDBCDataSource dataSource = fileDataSource();
        assertEquals(4, DatabaseMigrations.migrate(dataSource));
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO users(username, password_hash, role) VALUES ('alice', 'hash', 'USER')");
            statement.executeUpdate("INSERT INTO user_calendars(username) VALUES ('alice')");
            statement.execute("SHUTDOWN");
        }
        try {
            assertEquals(0, DatabaseMigrations.migrate(dataSource));
            assertEquals(1, JdbcTestSupport.countUsersByName(dataSource, "alice"));
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM user_calendars WHERE username = 'alice'")) {
                assertTrue(rows.next());
                assertEquals(1, rows.getInt(1));
            }
        } finally {
            shutdown(dataSource);
        }
    }

    @Test
    void upgradesLegacyDatabaseWithoutChangingAccountsTokensOrCalendarDates() throws Exception
    {
        JDBCDataSource dataSource = fileDataSource();
        JdbcTestSupport.initSchema(dataSource);
        JdbcTestSupport.insertUser(dataSource, "alice", "existing-hash", "ADMIN");
        JdbcTestSupport.insertToken(dataSource, "existing-token", "alice", 123456789L);
        Timestamp createdAt = Timestamp.valueOf("2026-01-02 03:04:05");
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE user_calendars (username VARCHAR(100) PRIMARY KEY REFERENCES users(username), " + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)");
            statement.executeUpdate("INSERT INTO user_calendars VALUES ('alice', TIMESTAMP '2026-01-02 03:04:05')");
        }
        try {
            assertEquals(4, DatabaseMigrations.migrate(dataSource));
            assertEquals(0, DatabaseMigrations.migrate(dataSource));
            assertEquals("ADMIN", JdbcTestSupport.roleByUsername(dataSource, "alice"));
            assertEquals(1, JdbcTestSupport.countTokensForUser(dataSource, "alice"));
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT users.password_hash, user_calendars.created_at " + "FROM users JOIN user_calendars ON users.username = user_calendars.username WHERE users.username = 'alice'")) {
                assertTrue(rows.next());
                assertEquals("existing-hash", rows.getString(1));
                assertEquals(createdAt, rows.getTimestamp(2));
                assertFalse(rows.next());
            }
        } finally {
            shutdown(dataSource);
        }
    }

    @Test
    void upgradesLegacyDatabaseThatHasNoCalendarsYet() throws Exception
    {
        JDBCDataSource dataSource = fileDataSource();
        JdbcTestSupport.initSchema(dataSource);
        JdbcTestSupport.insertUser(dataSource, "bob", "hash", "USER");
        try {
            assertEquals(4, DatabaseMigrations.migrate(dataSource));
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                assertEquals(1, statement.executeUpdate("INSERT INTO user_calendars(username) VALUES ('bob')"));
            }
        } finally {
            shutdown(dataSource);
        }
    }

    @Test
    void rejectsChangedMigrationHistory() throws Exception
    {
        JDBCDataSource dataSource = fileDataSource();
        DatabaseMigrations.migrate(dataSource);
        try {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("UPDATE \"flyway_schema_history\" SET \"checksum\" = 1 WHERE \"version\" = '2'");
            }
            assertThrows(FlywayException.class, () -> DatabaseMigrations.migrate(dataSource));
        } finally {
            shutdown(dataSource);
        }
    }

    @Test
    void propagatesConnectionFailures() throws Exception
    {
        DataSource dataSource = mock(DataSource.class, withSettings().mockMaker(PROXY));
        when(dataSource.getConnection()).thenThrow(new SQLException("database unavailable"));
        when(dataSource.getConnection(anyString(), anyString())).thenThrow(new SQLException("database unavailable"));
        assertThrows(FlywayException.class, () -> DatabaseMigrations.migrate(dataSource));
    }

    private JDBCDataSource fileDataSource()
    {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:file:" + directory.resolve("appdb") + ";shutdown=true");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private void shutdown(DataSource dataSource) throws SQLException
    {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
    }

}