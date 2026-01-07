package com.app.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.hsqldb.jdbc.JDBCDataSource;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Initializes HSQLDB. Uses jboss.server.data.dir if available, falls back to
 * java.io.tmpdir, and finally to an in-memory DB if necessary. Does not throw
 * from contextInitialized so deployment is not blocked by filesystem issues.
 */
@WebListener
public class DatabaseInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        JDBCDataSource ds = new JDBCDataSource();

        // Determine a writable base directory:
        String jbossData = System.getProperty("jboss.server.data.dir"); // WildFly standard
        String base;
        if (jbossData != null && !jbossData.isBlank()) {
            base = jbossData;
        } else {
            base = System.getProperty("java.io.tmpdir");
        }

        Path dataDir = null;
        boolean fileMode = false;

        try {
            dataDir = Path.of(base).resolve("app-data");
            Files.createDirectories(dataDir);
            // Use a file-based HSQLDB so data persists across restarts
            String dbUrl = "jdbc:hsqldb:file:" + dataDir.resolve("appdb").toAbsolutePath().toString() + ";shutdown=true";
            ds.setUrl(dbUrl);
            ds.setUser("sa");
            ds.setPassword("");
            fileMode = true;
            ctx.log("DatabaseInitListener: using file DB at " + dataDir.toAbsolutePath());
        } catch (Throwable t) {
            // Could not use file-based DB (permissions or other). Fall back to in-memory DB.
            ctx.log("DatabaseInitListener: failed to create/use file DB at " + (dataDir != null ? dataDir.toAbsolutePath() : base) + " - " + t.getMessage());
            ctx.log("DatabaseInitListener: falling back to in-memory HSQLDB (data will NOT persist).");
            ds.setUrl("jdbc:hsqldb:mem:appdb");
            ds.setUser("sa");
            ds.setPassword("");
        }

        // Initialize schema and default admin user if possible
        try (Connection c = ds.getConnection()) {
            c.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER IDENTITY PRIMARY KEY, "
                    + "username VARCHAR(100) UNIQUE NOT NULL, "
                    + "password_hash VARCHAR(256) NOT NULL, "
                    + "role VARCHAR(32) NOT NULL"
                    + ")"
            );

            c.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS remember_tokens ("
                    + "token VARCHAR(128) PRIMARY KEY, "
                    + "username VARCHAR(100) NOT NULL, "
                    + "expiry_ms BIGINT NOT NULL"
                    + ")"
            );

            // insert default admin if not exists
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
                ps.setString(1, "admin");
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        try (PreparedStatement ins = c.prepareStatement("INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)")) {
                            ins.setString(1, "admin");
                            ins.setString(2, PasswordUtil.hashPassword("admin")); // default admin/admin
                            ins.setString(3, "ADMIN");
                            ins.executeUpdate();
                        }
                        ctx.log("DatabaseInitListener: created default admin user");
                    }
                }
            }
            // put the datasource into the servlet context for DAOs to use
            ctx.setAttribute("datasource", ds);
        } catch (Throwable e) {
            // If we can't initialize DB, log but do not rethrow (so app can still deploy)
            ctx.log("DatabaseInitListener: failed to initialize database schema: " + e.getMessage(), e);
            // Ensure datasource is still available (it may be in-memory or partially initialized)
            ctx.setAttribute("datasource", ds);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing special to do for file-based HSQLDB here
    }
}
