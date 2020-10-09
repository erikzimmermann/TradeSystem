package de.codingair.tradesystem.utils.database.migrations.mysql;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    private static MySQLConnection instance;
    private static Connection connection;

    private static final ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private static final FileConfiguration config = file.getConfig();

    public static MySQLConnection getInstance() {
        if(instance == null) {
            instance = new MySQLConnection();
        }
        return instance;
    }

    public Connection initDataSource() {
        try {
            getDataSource();
        } catch(SQLException throwables) {
            throwables.printStackTrace();
        }

        return connection;
    }

    public static Connection getConnection() {
        if(connection == null) {
            try {
                getDataSource();
            } catch(SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return connection;
    }

    private static void getDataSource() throws SQLException {
        if(connection == null) {
            String host = config.getString("TradeSystem.Tradelog.Database.Db_host");
            int port = config.getInt("TradeSystem.Tradelog.Database.Db_port");
            String db = config.getString("TradeSystem.Tradelog.Database.Db_name");
            String user = config.getString("TradeSystem.Tradelog.Database.Db_user");
            String password = config.getString("TradeSystem.Tradelog.Database.Db_password");
            if(password != null && password.equalsIgnoreCase("null")) password = null;

            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useSSL=false", user, password);
        }
    }
}
