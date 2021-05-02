package de.codingair.tradesystem.spigot.utils.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.utils.database.migrations.Migration;

public class CreateTradeLogTableMigration implements Migration {
    @Override
    public String getStatement() {
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

    @Override
    public int getVersion() {
        return 1;
    }
}
