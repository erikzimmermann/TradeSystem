package de.codingair.tradesystem.spigot.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.database.migrations.Migration;
import org.jetbrains.annotations.NotNull;

public class AddIndexTradeLogTableMigration implements Migration {
    @Override
    public @NotNull String getStatement() {
        return "CREATE INDEX timestamp_tradelog"
                + " ON tradelog(timestamp);";
    }
}
