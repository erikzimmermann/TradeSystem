package de.codingair.tradesystem.utils.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;

public class MySQLConnection {

    private static MySQLConnection instance;
    private static HikariDataSource datasource;

    private static final ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private static final FileConfiguration config = file.getConfig();

    public static MySQLConnection getInstance() {
        if(instance == null) {
            instance = new MySQLConnection();
        }
        return instance;
    }

    public void kill() {
        datasource.close();
    }

    public DataSource initDataSource() {
        getDataSource();
        return datasource;
    }

    public static DataSource getDatasource() {
        if(datasource == null){
            getDataSource();
        }
        return datasource;
    }

    private static void getDataSource() {
        if (datasource == null) {
            HikariConfig hikariConfig = new HikariConfig();
            String host =  config.getString("TradeSystem.Database.Db_host");
            int port =  config.getInt("TradeSystem.Database.Db_port");
            String db =  config.getString("TradeSystem.Database.Db_name");
            String user =  config.getString("TradeSystem.Database.Db_user");
            String password =  config.getString("TradeSystem.Database.Db_password");

            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useSSL=false");
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(5);
            hikariConfig.setLeakDetectionThreshold(2000);
            hikariConfig.setAutoCommit(true);
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            datasource = new HikariDataSource(hikariConfig);
        }
    }
}
