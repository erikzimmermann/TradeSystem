package de.codingair.tradesystem.utils.database;

import de.codingair.tradesystem.tradelog.TradeLogOptions;
import de.codingair.tradesystem.utils.database.migrations.SqlMigrations;
import de.codingair.tradesystem.utils.database.migrations.mysql.MysqlMigrations;

public class DatabaseInitializer {

    public void initialize() {
        if (TradeLogOptions.isEnabled()) {
            DatabaseType type = DatabaseUtil.database().getType();
            DatabaseUtil.database().init();

            SqlMigrations sqlMigrations = getMigrationHandler(type);
            sqlMigrations.createMigrationTable();
            sqlMigrations.runMigrations();
        }
    }

    public void close() {
        if (TradeLogOptions.isEnabled()) {
            DatabaseUtil.database().close();
        }
    }

    private SqlMigrations getMigrationHandler(DatabaseType type) {
        SqlMigrations sqlMigrations;
        if (type == DatabaseType.MYSQL) {
            sqlMigrations = MysqlMigrations.getInstance();
        } else {
            throw new RuntimeException("Invalid database type provided: " + type);
        }
        return sqlMigrations;
    }
}
