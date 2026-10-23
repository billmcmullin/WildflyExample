package com.app.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.app.database.DatabaseMigrations;

import org.hsqldb.jdbc.JDBCDataSource;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Initializes persistent HSQLDB storage and applies versioned schema migrations.
 * Initialization failures prevent deployment rather than expose partial storage.
 */
@WebListener
public class DatabaseInitListener implements ServletContextListener {
    private static final String TEXT_DATABASE_DIRECTORY = "database.directory";
    private static final String TEXT_DATABASE_INIT_LISTENER_APPLIED = "DatabaseInitListener: applied ";
    private static final String TEXT_SCHEMA_MIGRATIONS = " schema migrations";


    private static final String BASE_DIR = "app-runtime";
    private static final String DATA_DIR = "app-data";
    private static final String DB_NAME = "appdb";
    private static final String DB_URL_FILE_PREFIX = "jdbc:hsqldb:file:";
    private static final String DB_URL_SUFFIX = ";shutdown=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String CONTEXT_DATASOURCE = "datasource";
    private static final String DEFAULT_ADMIN = "admin";
    private static final String ROLE_ADMIN = "ADMIN";

    private static final String SQL_COUNT_ADMIN = "SELECT COUNT(*) FROM users WHERE username = ?";
    private static final String SQL_INSERT_USER = "INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)";

    private static final String LOG_FILE_DB = "DatabaseInitListener: using file DB at ";
    private static final String LOG_ADMIN_CREATED = "DatabaseInitListener: created default admin user";
    private static final String LOG_INIT_FAIL = "DatabaseInitListener: persistent database initialization failed";
    private static final String CLASS_NAME = "DatabaseInitListener";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        JDBCDataSource ds = new JDBCDataSource();

        try {
            String configuredDirectory = ctx.getInitParameter(DatabaseInitListener.TEXT_DATABASE_DIRECTORY);
            Path dataDir = configuredDirectory == null || configuredDirectory.isBlank()
                    ? Path.of(BASE_DIR).resolve(DATA_DIR) : Path.of(configuredDirectory);
            Files.createDirectories(dataDir);
            String dbUrl = DB_URL_FILE_PREFIX + dataDir.resolve(DB_NAME).toAbsolutePath() + DB_URL_SUFFIX;
            ds.setUrl(dbUrl);
            ds.setUser(DB_USER);
            ds.setPassword(DB_PASSWORD);
            ctx.log(LOG_FILE_DB + dataDir.toAbsolutePath());

            int migrationsApplied = DatabaseMigrations.migrate(ds);
            ctx.log(DatabaseInitListener.TEXT_DATABASE_INIT_LISTENER_APPLIED + migrationsApplied + DatabaseInitListener.TEXT_SCHEMA_MIGRATIONS);

            try (Connection connection = ds.getConnection();
                 PreparedStatement statement = connection.prepareStatement(SQL_COUNT_ADMIN)) {
                statement.setString(1, DEFAULT_ADMIN);
                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();
                    if (rows.getInt(1) == 0) {
                        try (PreparedStatement insert = connection.prepareStatement(SQL_INSERT_USER)) {
                            insert.setString(1, DEFAULT_ADMIN);
                            insert.setString(2, PasswordUtil.hashPassword(DEFAULT_ADMIN));
                            insert.setString(3, ROLE_ADMIN);
                            insert.executeUpdate();
                        }
                        ctx.log(LOG_ADMIN_CREATED);
                    }
                }
            }
            ctx.setAttribute(CONTEXT_DATASOURCE, ds);
        } catch (IOException | SQLException | RuntimeException exception) {
            ctx.log(LOG_INIT_FAIL, exception);
            throw new IllegalStateException(LOG_INIT_FAIL, exception);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing special to do for file-based HSQLDB here
    }

    private final void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException(CLASS_NAME);
    }
}
