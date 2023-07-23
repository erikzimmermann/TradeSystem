package de.codingair.tradesystem.spigot.utils.database;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.database.migrations.mysql.MySQLConnection;
import de.codingair.tradesystem.spigot.utils.database.migrations.sqlite.SqlLiteConnection;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseUtil {
    private static DatabaseUtil instance;
    private final DatabaseType databaseType;

    private DatabaseUtil() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();

        String databaseType = config.getString("TradeSystem.Database.Type", "MYSQL");

        this.databaseType = DatabaseType.byName(databaseType);
        if (this.databaseType == null) throw new IllegalStateException("Invalid database type configured: " + databaseType);
    }

    public static DatabaseUtil database() {
        if (instance == null) instance = new DatabaseUtil();
        return instance;
    }

    public void check() throws Exception {
        if (databaseType == DatabaseType.MYSQL) {
            MySQLConnection.checkDataSource();
        } else if (databaseType == DatabaseType.SQLITE) {
            SqlLiteConnection.connect().close();
        }
    }

    public DatabaseType getType() {
        return databaseType;
    }
}
