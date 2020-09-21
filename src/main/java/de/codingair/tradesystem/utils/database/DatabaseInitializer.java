package de.codingair.tradesystem.utils.database;

import de.codingair.tradesystem.tradelog.TradeLogOptions;
import de.codingair.tradesystem.utils.database.migrations.SqlMigrations;
import de.codingair.tradesystem.utils.database.migrations.mysql.MysqlMigrations;
import de.codingair.tradesystem.utils.database.migrations.sqlite.SqLiteMigrations;

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

    private SqlMigrations getMigrationHandler(DatabaseType type) {
        switch (type) {
            case MYSQL:
                return MysqlMigrations.getInstance();
            case SQLITE:
                return SqLiteMigrations.getInstance();
            default:
                throw new RuntimeException("Invalid database type provided: " + type);
        }
    }
}
