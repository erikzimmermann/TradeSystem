package de.codingair.tradesystem.spigot.database;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.database.migrations.SqlMigrations;
import de.codingair.tradesystem.spigot.database.migrations.mysql.MySQLConnection;
import de.codingair.tradesystem.spigot.database.migrations.mysql.MysqlMigrations;
import de.codingair.tradesystem.spigot.database.migrations.sqlite.SqLiteMigrations;
import de.codingair.tradesystem.spigot.database.migrations.sqlite.SqlLiteConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class DatabaseHandler {
    private DatabaseType databaseType;
    private boolean running = false;

    public void load() {
        loadType();

        TradeSystem.log("  > Queuing database initializing task");
        Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    checkCredentials();

                    SqlMigrations sqlMigrations = getMigrationHandler();

                    // we don't essentially have migrations if we don't have a database
                    if (sqlMigrations != null) {
                        sqlMigrations.createMigrationTable();
                        sqlMigrations.runMigrations();
                    }

                    TradeSystem.getInstance().getLogger().log(Level.INFO, "Database was started successfully.");
                    running = true;
                } catch (Exception ex) {
                    running = false;

                    boolean switchToSQLite = databaseType != DatabaseType.SQLITE;
                    boolean persist = defaultCredentials();

                    TradeSystem.getInstance().getLogger().log(persist ? Level.WARNING : Level.SEVERE, "Database could not be started: " + ex.getMessage());
                    if (switchToSQLite) {
                        TradeSystem.getInstance().getLogger().warning("Switching to SQLite database.");
                        setTypeToSQLite(persist);
                        this.run();
                    }
                }
            }
        });
    }

    private boolean defaultCredentials() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();

        String defUser = "root";
        String defPassword = "password";
        return defUser.equals(config.getString("TradeSystem.Database.MySQL.User")) &&
                defPassword.equals(config.getString("TradeSystem.Database.MySQL.Password"));
    }

    private void setTypeToSQLite(boolean persist) {
        this.databaseType = DatabaseType.SQLITE;

        if (persist) {
            ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
            FileConfiguration config = file.getConfig();

            config.set("TradeSystem.Database.Type", DatabaseType.SQLITE.getName());
            file.saveConfig();
        }
    }

    private void loadType() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();

        String databaseType = config.getString("TradeSystem.Database.Type", "SQLITE");

        this.databaseType = DatabaseType.byName(databaseType);
        if (this.databaseType == null) {
            TradeSystem.getInstance().getLogger().warning("Invalid database type configured: " + databaseType + ". Using SQLite instead.");
            this.databaseType = DatabaseType.SQLITE;
        }
    }

    private void checkCredentials() throws Exception {
        switch (databaseType) {
            case MYSQL:
                MySQLConnection.checkDataSource();
                break;
            case SQLITE:
                SqlLiteConnection.connect().close();
                break;
        }
    }

    @Nullable
    private SqlMigrations getMigrationHandler() {
        switch (databaseType) {
            case MYSQL:
                return MysqlMigrations.getInstance();
            case SQLITE:
                return SqLiteMigrations.getInstance();
            default:
                return null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    @NotNull
    public DatabaseType getType() {
        return databaseType;
    }
}
