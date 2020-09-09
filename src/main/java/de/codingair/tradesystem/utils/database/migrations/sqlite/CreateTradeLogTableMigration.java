package de.codingair.tradesystem.utils.database.migrations.sqlite;

import de.codingair.tradesystem.utils.database.migrations.Migration;

public class CreateTradeLogTableMigration implements Migration {
    @Override
    public String getStatement() {
        return "CREATE TABLE IF NOT EXISTS tradelog (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	player1 varchar NOT NULL,\n"
                + "	player2 varchar NOT NULL,\n"
                + "	message text NOT NULL,\n"
                + "	timestamp INTEGER NOT NULL" + ");\n" +
                " CREATE INDEX player1_tradelog \n" +
                " ON tradelog(player1);\n" +
                " CREATE INDEX player2_tradelog \n" +
                " ON tradelog(player2);";
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
