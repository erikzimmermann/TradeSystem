package de.codingair.tradesystem.utils.database;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;

public class DatabaseUtil {

    private static DatabaseUtil instance;
    private ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private FileConfiguration config = file.getConfig();
    private static final String MYSQL_STRING = "MYSQL";

    private DatabaseType databaseType;

    public static DatabaseUtil database() {
        if(instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }

    private DatabaseUtil() {
        String databaseType = config.getString("TradeSystem.Database.Type", MYSQL_STRING);
        if (MYSQL_STRING.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseType.MYSQL;
        } else {
            throw new RuntimeException("Invalid database type configured: " + databaseType);
        }
    }

    public DataSource init() {
        if(databaseType == DatabaseType.MYSQL) {
            return MySQLConnection.getInstance().initDataSource();
        }
        throw new RuntimeException("No database configured");
    }

    public void close() {
        if(databaseType == DatabaseType.MYSQL) {
            MySQLConnection.getInstance().kill();
        }
        throw new RuntimeException("No database configured");
    }

    public DatabaseType getType() {
        return databaseType;
    }
}
