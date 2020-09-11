package de.codingair.tradesystem.utils.database.migrations.mysql;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;

public class MySQLConnection {

    private static MySQLConnection instance;
    private static MysqlDataSource datasource;

    private static final ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private static final FileConfiguration config = file.getConfig();

    public static MySQLConnection getInstance() {
        if(instance == null) {
            instance = new MySQLConnection();
        }
        return instance;
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
            datasource = new MysqlDataSource();
            String host =  config.getString("TradeSystem.Database.Db_host");
            int port =  config.getInt("TradeSystem.Database.Db_port");
            String db =  config.getString("TradeSystem.Database.Db_name");
            String user =  config.getString("TradeSystem.Database.Db_user");
            String password =  config.getString("TradeSystem.Database.Db_password");

            datasource.setURL("jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useSSL=false");
            datasource.setUser(user);
            datasource.setPassword(password);
            datasource.setDatabaseName(db);
        }
    }
}
