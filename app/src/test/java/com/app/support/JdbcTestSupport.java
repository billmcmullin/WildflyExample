package com.app.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.hsqldb.jdbc.JDBCDataSource;

public final class JdbcTestSupport {

    private JdbcTestSupport() {
    }

    public static JDBCDataSource newDataSource() throws SQLException {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:testdb_" + UUID.randomUUID());
        ds.setUser("sa");
        ds.setPassword("");
        initSchema(ds);
        return ds;
    }

    public static void initSchema(JDBCDataSource ds) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement users = c.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS users ("
                        + "id INTEGER IDENTITY PRIMARY KEY,"
                            + "username VARCHAR(100) UNIQUE NOT NULL,"
                            + "password_hash VARCHAR(256) NOT NULL,"
                            + "role VARCHAR(32) NOT NULL)")) {
                users.executeUpdate();
            }

            try (PreparedStatement tokens = c.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS remember_tokens ("
                            + "token VARCHAR(128) PRIMARY KEY,"
                            + "username VARCHAR(100) NOT NULL,"
                            + "expiry_ms BIGINT NOT NULL)")) {
                tokens.executeUpdate();
            }
        }
    }

    public static void insertUser(JDBCDataSource ds, String username, String hash, String role) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    public static void insertToken(JDBCDataSource ds, String token, String username, long expiryMs) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO remember_tokens(token, username, expiry_ms) VALUES(?, ?, ?)")) {
            ps.setString(1, token);
            ps.setString(2, username);
            ps.setLong(3, expiryMs);
            ps.executeUpdate();
        }
    }

    public static int countUsersByName(JDBCDataSource ds, String username) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public static int countTokensForUser(JDBCDataSource ds, String username) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM remember_tokens WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public static String roleByUsername(JDBCDataSource ds, String username) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT role FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }
}
