package com.example.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.hsqldb.jdbc.JDBCDataSource;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DatabaseInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            JDBCDataSource ds = new JDBCDataSource();
            // in-memory DB; change to file mode if you want persistence:
            ds.setUrl("jdbc:hsqldb:mem:appdb");
            ds.setUser("sa");
            ds.setPassword("");

            // create table and default admin if missing
            try (Connection c = ds.getConnection()) {
                c.createStatement().executeUpdate(
                        "CREATE TABLE IF NOT EXISTS users ("
                        + "id INTEGER IDENTITY PRIMARY KEY, "
                        + "username VARCHAR(100) UNIQUE NOT NULL, "
                        + "password_hash VARCHAR(256) NOT NULL"
                        + ")"
                );

                // insert default admin if not exists
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
                    ps.setString(1, "admin");
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            try (PreparedStatement ins = c.prepareStatement("INSERT INTO users(username, password_hash) VALUES(?, ?)")) {
                                ins.setString(1, "admin");
                                ins.setString(2, PasswordUtil.hashPassword("admin")); // default admin/admin
                                ins.executeUpdate();
                            }
                        }
                    }
                }
            }

            sce.getServletContext().setAttribute("datasource", ds);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no-op for in-memory DB; if file DB used, you could shutdown here
    }

}
