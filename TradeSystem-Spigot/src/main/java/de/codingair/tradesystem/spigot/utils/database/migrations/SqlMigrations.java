package de.codingair.tradesystem.spigot.utils.database.migrations;

import java.sql.SQLException;

public interface SqlMigrations {
    void createMigrationTable() throws SQLException;

    void runMigrations() throws SQLException;
}
