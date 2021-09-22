package de.codingair.tradesystem.spigot.utils.database.migrations.mysql;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.Supplier;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    private static final ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private static final FileConfiguration config = file.getConfig();
    private static MySQLConnection instance;

    private final String host;
    private final int port;
    private final String db;
    private final String user;
    private final String password;

    private MySQLConnection() {
        host = config.getString("TradeSystem.Tradelog.Database.Db_host");
        port = config.getInt("TradeSystem.Tradelog.Database.Db_port");
        db = config.getString("TradeSystem.Tradelog.Database.Db_name");
        user = config.getString("TradeSystem.Tradelog.Database.Db_user");

        String password = config.getString("TradeSystem.Tradelog.Database.Db_password");
        if (password != null && password.equalsIgnoreCase("null")) password = null;
        this.password = password;
    }

    private static MySQLConnection getInstance() {
        if (instance == null) instance = new MySQLConnection();
        return instance;
    }

    public static Supplier<Connection, SQLException> getConnection() {
        return () -> getInstance().buildConnection();
    }

    public static void checkDataSource() throws SQLException {
        getConnection().get().close();
    }

    private Connection buildConnection() throws SQLException {
        if (host == null || port == 0 || db == null || user == null) return null;

        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useSSL=false", user, password);
    }
}
