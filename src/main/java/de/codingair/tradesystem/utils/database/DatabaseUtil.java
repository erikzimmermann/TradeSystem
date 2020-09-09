package de.codingair.tradesystem.utils.database;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseUtil {

    private static DatabaseUtil instance;
    private ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private FileConfiguration config = file.getConfig();
    private static final String SQLITE_STRING = "SQLITE";

    private DatabaseType databaseType;

    public static DatabaseUtil database() {
        if(instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }

    private DatabaseUtil() {
        String databaseType = config.getString("TradeLog.database", SQLITE_STRING);
        if (SQLITE_STRING.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseType.SQLITE;
        } else {
            throw new RuntimeException("Invalid database type configured: " + databaseType);
        }
    }

    public DatabaseType getType() {
        return databaseType;
    }
}
