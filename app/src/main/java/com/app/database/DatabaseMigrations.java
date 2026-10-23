package com.app.database;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

public final class DatabaseMigrations {
    private static final String[] NO_IGNORED_MIGRATIONS = new String[0];
    private static final String TEXT_CLASSPATH_DB_MIGRATION = "classpath:db/migration";
    private static final String TEXT_0 = "0";


    private DatabaseMigrations() {
    }

    public static int migrate(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(DatabaseMigrations.TEXT_CLASSPATH_DB_MIGRATION)
                .baselineOnMigrate(true)
                .baselineVersion(DatabaseMigrations.TEXT_0)
                .cleanDisabled(true)
                .validateMigrationNaming(true)
                .ignoreMigrationPatterns(NO_IGNORED_MIGRATIONS)
                .load()
                .migrate()
                .migrationsExecuted;
    }
}