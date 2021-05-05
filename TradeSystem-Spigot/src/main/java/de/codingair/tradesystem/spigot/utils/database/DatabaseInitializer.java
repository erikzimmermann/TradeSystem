package de.codingair.tradesystem.spigot.utils.database;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.tradelog.TradeLogOptions;
import de.codingair.tradesystem.spigot.utils.database.migrations.SqlMigrations;
import de.codingair.tradesystem.spigot.utils.database.migrations.mysql.MysqlMigrations;
import de.codingair.tradesystem.spigot.utils.database.migrations.sqlite.SqLiteMigrations;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class DatabaseInitializer {

    public void initialize() {
        if (TradeLogOptions.isEnabled()) {
            TradeSystem.log("  > Queuing database initializing task");
            Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(), () -> {
                try {
                    DatabaseType type = DatabaseUtil.database().getType();
                    DatabaseUtil.database().init();

                    SqlMigrations sqlMigrations = getMigrationHandler(type);
                    sqlMigrations.createMigrationTable();
                    sqlMigrations.runMigrations();
                    TradeSystem.getInstance().getLogger().log(Level.INFO, "Database logging was started successfully.");
                } catch (Exception ex) {
                    TradeSystem.getInstance().getLogger().log(Level.SEVERE, "Database logging could not be started. For more information see error below: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } else TradeSystem.log("  > Database logging is disabled");
    }

    private SqlMigrations getMigrationHandler(DatabaseType type) {
        switch (type) {
            case MYSQL:
                return MysqlMigrations.getInstance();
            case SQLITE:
                return SqLiteMigrations.getInstance();
            default:
                throw new IllegalStateException("Invalid database type provided: " + type);
        }
    }
}
