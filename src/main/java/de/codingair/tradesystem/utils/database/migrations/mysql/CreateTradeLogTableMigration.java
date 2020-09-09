package de.codingair.tradesystem.utils.database.migrations.mysql;

import de.codingair.tradesystem.utils.database.migrations.Migration;

public class CreateTradeLogTableMigration implements Migration {
    @Override
    public String getStatement() {
        return "CREATE TABLE IF NOT EXISTS tradelog (\n"
                + "	id BIGINT PRIMARY KEY AUTO_INCREMENT,\n"
                + "	player1 varchar(255) NOT NULL,\n"
                + "	player2 varchar(255) NOT NULL,\n"
                + "	message text NOT NULL,\n"
                + "	timestamp BIGINT NOT NULL" + ");";
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
