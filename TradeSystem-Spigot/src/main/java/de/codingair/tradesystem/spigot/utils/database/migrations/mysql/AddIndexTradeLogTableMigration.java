package de.codingair.tradesystem.spigot.utils.database.migrations.mysql;

import de.codingair.tradesystem.spigot.utils.database.migrations.Migration;
import org.jetbrains.annotations.NotNull;

public class AddIndexTradeLogTableMigration implements Migration {
    @Override
    public @NotNull String getStatement() {
        return "ALTER TABLE tradelog "
                + "ADD INDEX(player1), "
                + "ADD INDEX(player2), "
                + "ADD INDEX(timestamp);";
    }
}
