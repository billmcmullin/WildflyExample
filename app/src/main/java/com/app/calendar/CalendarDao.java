package com.app.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public final class CalendarDao {

    private final DataSource dataSource;

    public CalendarDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void initializeSchema(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS user_calendars (
                    username VARCHAR(100) PRIMARY KEY REFERENCES users(username),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
                )
                """)) {
            statement.executeUpdate();
        }
    }

    void ensureCalendar(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     MERGE INTO user_calendars AS calendar
                     USING (VALUES(CAST(? AS VARCHAR(100)))) AS incoming(username)
                     ON calendar.username = incoming.username
                     WHEN NOT MATCHED THEN INSERT (username) VALUES (incoming.username)
                     """)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }
}