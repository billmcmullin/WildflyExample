package com.app.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import com.app.database.DatabaseMigrations;
import com.app.support.JdbcTestSupport;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.MockMakers.PROXY;
import static org.mockito.Mockito.*;
class DatabaseInitListenerTest
{

    @TempDir
    Path directory;

    @Test
    void contextInitializedMigratesBeforePublishingDatasourceAndSeedsAdminOnce() throws Exception
    {
        DatabaseInitListener listener = new DatabaseInitListener();
        ServletContext ctx = contextFor(directory);
        ServletContextEvent event = new ServletContextEvent(ctx);

        listener.contextInitialized(event);
        listener.contextInitialized(event);

        ArgumentCaptor<DataSource> captured = ArgumentCaptor.forClass(DataSource.class);
        verify(ctx, times(2)).setAttribute(eq("datasource"), captured.capture());
        DataSource dataSource = captured.getValue();
        assertEquals(0, DatabaseMigrations.migrate(dataSource));
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT password_hash, role FROM users WHERE username = 'admin'")) {
            assertTrue(rows.next());
            assertTrue(PasswordUtil.verify("admin", rows.getString(1)));
            assertEquals("ADMIN", rows.getString(2));
            assertFalse(rows.next());
        }
    }

    @Test
    void preservesExistingAdminCredentials() throws Exception
    {
        JDBCDataSource dataSource = existingDataSource();
        DatabaseMigrations.migrate(dataSource);
        JdbcTestSupport.insertUser(dataSource, "admin", "existing-hash", "ADMIN");
        ServletContext context = contextFor(directory);

        new DatabaseInitListener().contextInitialized(new ServletContextEvent(context));

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT password_hash FROM users WHERE username = 'admin'")) {
            assertTrue(rows.next());
            assertEquals("existing-hash", rows.getString(1));
            assertFalse(rows.next());
        }
    }

    @Test
    void refusesToPublishDatasourceWhenStorageDirectoryCannotBeCreated() throws Exception
    {
        Path blocked = Files.createFile(directory.resolve("not-a-directory"));
        ServletContext context = contextFor(blocked);

        assertThrows(IllegalStateException.class, () -> new DatabaseInitListener().contextInitialized(new ServletContextEvent(context)));

        verify(context, never()).setAttribute(eq("datasource"), any());
        verify(context).log(anyString(), any(Throwable.class));
    }

    @Test
    void refusesToPublishDatasourceWhenMigrationValidationFails() throws Exception
    {
        JDBCDataSource dataSource = existingDataSource();
        DatabaseMigrations.migrate(dataSource);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE \"flyway_schema_history\" SET \"checksum\" = 1 WHERE \"version\" = '2'");
        }
        ServletContext context = contextFor(directory);

        assertThrows(IllegalStateException.class, () -> new DatabaseInitListener().contextInitialized(new ServletContextEvent(context)));

        verify(context, never()).setAttribute(eq("datasource"), any());
        verify(context).log(anyString(), any(Throwable.class));
    }

    @Test
    void contextDestroyedIsNoOp()
    {
        DatabaseInitListener listener = new DatabaseInitListener();
        ServletContextEvent event = new ServletContextEvent(mock(ServletContext.class, withSettings().mockMaker(PROXY)));

        listener.contextDestroyed(event);
    }

    @Test
    void wildFlyDefaultUsesServerDataDirectoryInsteadOfWorkingDirectory()
    {
        Path serverData = directory.resolve("wildfly-data");
        assertEquals(serverData.resolve("app-runtime").resolve("app-data"), DatabaseInitListener.resolveDataDirectory(null, null, serverData.toString()));
    }

    @Test
    void explicitContextDirectoryTakesPrecedenceOverJvmAndServerDefaults()
    {
        Path contextData = directory.resolve("context-data");
        assertEquals(contextData, DatabaseInitListener.resolveDataDirectory(contextData.toString(), directory.resolve("jvm-data").toString(), directory.resolve("server-data").toString()));
    }

    @Test
    void jvmDirectoryOverridesServerDefaultWhenContextSettingIsBlank()
    {
        Path jvmData = directory.resolve("jvm-data");
        assertEquals(jvmData, DatabaseInitListener.resolveDataDirectory(" ", jvmData.toString(), directory.resolve("server-data").toString()));
    }

    @Test
    void nonWildFlyDefaultRemainsCompatibleWithTheExistingRelativeLocation()
    {
        assertEquals(Path.of("app-runtime", "app-data"), DatabaseInitListener.resolveDataDirectory(null, " ", null));
    }

    private ServletContext contextFor(Path dataDirectory)
    {
        ServletContext context = mock(ServletContext.class, withSettings().mockMaker(PROXY));
        when(context.getInitParameter("database.directory")).thenReturn(dataDirectory.toString());
        return context;
    }

    private JDBCDataSource existingDataSource()
    {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:file:" + directory.resolve("appdb") + ";shutdown=true");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

}
