package de.codingair.tradesystem.spigot.utils.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.utils.database.migrations.Migration;

public class AddIndexTradeLogTableMigration implements Migration {
    @Override
    public String getStatement() {
        return "CREATE INDEX timestamp_tradelog"
                + " ON tradelog(timestamp);";
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
