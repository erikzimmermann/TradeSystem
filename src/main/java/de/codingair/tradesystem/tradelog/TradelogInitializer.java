package de.codingair.tradesystem.tradelog;

import de.codingair.tradesystem.utils.database.DatabaseType;
import de.codingair.tradesystem.utils.database.DatabaseUtil;
import de.codingair.tradesystem.utils.database.migrations.SqLiteMigrations;
import de.codingair.tradesystem.utils.database.migrations.SqlMigrations;

import static de.codingair.tradesystem.tradelog.TradeLogService.getTradeLog;

public class TradelogInitializer {

    public void initialize() {
        if (getTradeLog().isEnabled()) {
            SqlMigrations sqlMigrations = getMigrationHandler();
            sqlMigrations.createMigrationTable();
            sqlMigrations.runMigrations();
        }
    }

    private SqlMigrations getMigrationHandler() {
        DatabaseType type = DatabaseUtil.database().getType();
        SqlMigrations sqlMigrations;
        if (type == DatabaseType.SQLITE) {
            sqlMigrations = SqLiteMigrations.getInstance();
        } else {
            throw new RuntimeException("Invalid database type provided: " + type);
        }
        return sqlMigrations;
    }
}
