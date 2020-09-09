package de.codingair.tradesystem.utils.database.migrations;

public interface SqlMigrations {
    void createMigrationTable();

    void runMigrations();
}
