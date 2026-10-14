package com.app.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
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
 * Initializes HSQLDB. Uses a local runtime folder and falls back to
 * an in-memory DB if filesystem initialization fails. Does not throw from
 * contextInitialized so deployment is not blocked by startup issues.
 */
@WebListener
public class DatabaseInitListener implements ServletContextListener {

    private static final String BASE_DIR = "app-runtime";
    private static final String DATA_DIR = "app-data";
    private static final String DB_NAME = "appdb";
    private static final String DB_URL_FILE_PREFIX = "jdbc:hsqldb:file:";
    private static final String DB_URL_MEM = "jdbc:hsqldb:mem:appdb";
    private static final String DB_URL_SUFFIX = ";shutdown=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String CONTEXT_DATASOURCE = "datasource";
    private static final String DEFAULT_ADMIN = "admin";
    private static final String ROLE_ADMIN = "ADMIN";

    private static final String SQL_CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER IDENTITY PRIMARY KEY,
                username VARCHAR(100) UNIQUE NOT NULL,
                password_hash VARCHAR(256) NOT NULL,
                role VARCHAR(32) NOT NULL
            )
            """;

    private static final String SQL_CREATE_REMEMBER_TOKENS = """
            CREATE TABLE IF NOT EXISTS remember_tokens (
                token VARCHAR(128) PRIMARY KEY,
                username VARCHAR(100) NOT NULL,
                expiry_ms BIGINT NOT NULL
            )
            """;

    private static final String SQL_COUNT_ADMIN = "SELECT COUNT(*) FROM users WHERE username = ?";
    private static final String SQL_INSERT_USER = "INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)";

    private static final String LOG_FILE_DB = "DatabaseInitListener: using file DB at ";
    private static final String LOG_FILE_DB_FAIL = "DatabaseInitListener: failed to create/use file DB at ";
    private static final String LOG_FILE_DB_FALLBACK = "DatabaseInitListener: falling back to in-memory HSQLDB (data will NOT persist).";
    private static final String LOG_ADMIN_CREATED = "DatabaseInitListener: created default admin user";
    private static final String LOG_SCHEMA_INIT_FAIL = "DatabaseInitListener: failed to initialize database schema: ";
    private static final String LOG_SEPARATOR = " - ";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        JDBCDataSource ds = new JDBCDataSource();

        // Use a local runtime folder by default.
        String base = BASE_DIR;

        Path dataDir = null;

        try {
            dataDir = Path.of(base).resolve(DATA_DIR);
            Files.createDirectories(dataDir);
            // Use a file-based HSQLDB so data persists across restarts
            String dbUrl = DB_URL_FILE_PREFIX + dataDir.resolve(DB_NAME).toAbsolutePath() + DB_URL_SUFFIX;
            ds.setUrl(dbUrl);
            ds.setUser(DB_USER);
            ds.setPassword(DB_PASSWORD);
            ctx.log(LOG_FILE_DB + dataDir.toAbsolutePath());
        } catch (Throwable t) {
            // Could not use file-based DB (permissions or other). Fall back to in-memory DB.
            ctx.log(LOG_FILE_DB_FAIL + (dataDir != null ? dataDir.toAbsolutePath() : base) + LOG_SEPARATOR + t.getMessage());
            ctx.log(LOG_FILE_DB_FALLBACK);
            ds.setUrl(DB_URL_MEM);
            ds.setUser(DB_USER);
            ds.setPassword(DB_PASSWORD);
        }

        // Initialize schema and default admin user if possible
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement createUsers = c.prepareStatement(SQL_CREATE_USERS)) {
                createUsers.executeUpdate();
            }

            try (PreparedStatement createRememberTokens = c.prepareStatement(SQL_CREATE_REMEMBER_TOKENS)) {
                createRememberTokens.executeUpdate();
            }

            // insert default admin if not exists
            try (PreparedStatement ps = c.prepareStatement(SQL_COUNT_ADMIN)) {
                ps.setString(1, DEFAULT_ADMIN);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        try (PreparedStatement ins = c.prepareStatement(SQL_INSERT_USER)) {
                            ins.setString(1, DEFAULT_ADMIN);
                            ins.setString(2, PasswordUtil.hashPassword(DEFAULT_ADMIN)); // default admin/admin
                            ins.setString(3, ROLE_ADMIN);
                            ins.executeUpdate();
                        }
                        ctx.log(LOG_ADMIN_CREATED);
                    }
                }
            }
            // put the datasource into the servlet context for DAOs to use
            ctx.setAttribute(CONTEXT_DATASOURCE, ds);
        } catch (Throwable e) {
            // If we can't initialize DB, log but do not rethrow (so app can still deploy)
            ctx.log(LOG_SCHEMA_INIT_FAIL + e.getMessage(), e);
            // Ensure datasource is still available (it may be in-memory or partially initialized)
            ctx.setAttribute(CONTEXT_DATASOURCE, ds);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing special to do for file-based HSQLDB here
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException("DatabaseInitListener");
    }
}
