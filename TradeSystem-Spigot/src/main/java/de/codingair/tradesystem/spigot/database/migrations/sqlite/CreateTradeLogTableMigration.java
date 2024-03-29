package de.codingair.tradesystem.spigot.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.database.migrations.Migration;
import org.jetbrains.annotations.NotNull;

public class CreateTradeLogTableMigration implements Migration {
    @Override
    public @NotNull String getStatement() {
        return "CREATE TABLE IF NOT EXISTS tradelog ("
                + "	id integer PRIMARY KEY,"
                + "	player1 varchar(16) NOT NULL,"
                + "	player2 varchar(16) NOT NULL,"
                + "	message text NOT NULL,"
                + "	timestamp INTEGER NOT NULL" + ");" +
                " CREATE INDEX player1_tradelog" +
                " ON tradelog(player1);" +
                " CREATE INDEX player2_tradelog" +
                " ON tradelog(player2);";
    }
}
