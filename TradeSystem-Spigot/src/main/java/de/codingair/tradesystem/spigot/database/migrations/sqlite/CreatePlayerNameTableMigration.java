package de.codingair.tradesystem.spigot.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.database.migrations.Migration;
import org.jetbrains.annotations.NotNull;

public class CreatePlayerNameTableMigration implements Migration {
    @Override
    public @NotNull String getStatement() {
        return "CREATE TABLE IF NOT EXISTS trade_players ("
                + "	id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "	uuid VARCHAR(36) NOT NULL UNIQUE,"
                + "	name VARCHAR(16) NOT NULL);";
    }
}
