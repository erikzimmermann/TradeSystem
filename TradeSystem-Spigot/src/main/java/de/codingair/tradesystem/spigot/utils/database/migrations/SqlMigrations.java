package de.codingair.tradesystem.spigot.utils.database.migrations;

public interface SqlMigrations {
    void createMigrationTable();

    void runMigrations();
}
