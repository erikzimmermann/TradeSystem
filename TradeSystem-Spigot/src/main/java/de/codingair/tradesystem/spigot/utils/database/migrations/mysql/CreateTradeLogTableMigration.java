package de.codingair.tradesystem.spigot.utils.database.migrations.mysql;

import de.codingair.tradesystem.spigot.utils.database.migrations.Migration;

public class CreateTradeLogTableMigration implements Migration {
    @Override
    public String getStatement() {
        return "CREATE TABLE IF NOT EXISTS tradelog ("
                + "	id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "	player1 varchar(16) NOT NULL,"
                + "	player2 varchar(16) NOT NULL,"
                + "	message text NOT NULL,"
                + "	timestamp BIGINT NOT NULL" + ");";
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
