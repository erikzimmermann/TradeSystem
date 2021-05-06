package de.codingair.tradesystem.spigot.utils.database.migrations.mysql;

import de.codingair.tradesystem.spigot.utils.database.migrations.Migration;

public class AddIndexTradeLogTableMigration implements Migration {
    @Override
    public String getStatement() {
        return "ALTER TABLE tradelog "
                + "ADD INDEX(player1), "
                + "ADD INDEX(player2), "
                + "ADD INDEX(timestamp);";
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
