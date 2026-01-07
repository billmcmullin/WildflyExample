package com.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

public class UserDao {

    private final DataSource ds;

    public UserDao(DataSource ds) {
        this.ds = ds;
    }

    public String findPasswordHashByUsername(String username) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT password_hash FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }

    public String findRoleByUsername(String username) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT role FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }

    public boolean createUser(String username, String passwordHash, String role) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role != null ? role : "USER");
            return ps.executeUpdate() == 1;
        }
    }

    public boolean createRememberToken(String token, String username, long expiryMs) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO remember_tokens(token, username, expiry_ms) VALUES(?, ?, ?)")) {
            ps.setString(1, token);
            ps.setString(2, username);
            ps.setLong(3, expiryMs);
            return ps.executeUpdate() == 1;
        }
    }

    public String findUsernameByToken(String token) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT username, expiry_ms FROM remember_tokens WHERE token = ?")) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long expiry = rs.getLong("expiry_ms");
                    if (expiry >= System.currentTimeMillis()) {
                        return rs.getString("username");
                    } else {
                        // expired -> cleanup
                        deleteRememberToken(token);
                    }
                }
                return null;
            }
        }
    }

    public boolean deleteRememberToken(String token) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM remember_tokens WHERE token = ?")) {
            ps.setString(1, token);
            return ps.executeUpdate() > 0;
        }
    }

    public void deleteTokensForUser(String username) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM remember_tokens WHERE username = ?")) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    public void cleanExpiredTokens() throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM remember_tokens WHERE expiry_ms < ?")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }
}
