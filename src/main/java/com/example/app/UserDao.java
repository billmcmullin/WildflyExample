package com.example.app;

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

    public boolean createUser(String username, String passwordHash) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO users(username, password_hash) VALUES(?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            return ps.executeUpdate() == 1;
        }
    }

}
