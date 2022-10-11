package de.codingair.tradesystem.spigot.utils.database;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogOptions;
import de.codingair.tradesystem.spigot.utils.database.migrations.SqlMigrations;
import de.codingair.tradesystem.spigot.utils.database.migrations.mysql.MysqlMigrations;
import de.codingair.tradesystem.spigot.utils.database.migrations.sqlite.SqLiteMigrations;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class DatabaseInitializer {
    private boolean running = false;

    public void initialize() {
        if (TradeLogOptions.isEnabled()) {
            TradeSystem.log("  > Queuing database initializing task");
            Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(), () -> {
                try {
                    DatabaseType type = DatabaseUtil.database().getType();
                    DatabaseUtil.database().check();

                    SqlMigrations sqlMigrations = getMigrationHandler(type);
                    sqlMigrations.createMigrationTable();
                    sqlMigrations.runMigrations();

                    TradeSystem.getInstance().getLogger().log(Level.INFO, "Database logging was started successfully.");
                    running = true;
                } catch (Exception ex) {
                    TradeSystem.getInstance().getLogger().log(Level.SEVERE, "Database logging could not be started: " + ex.getMessage());
                    running = false;
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

    public boolean isRunning() {
        return running;
    }
}
