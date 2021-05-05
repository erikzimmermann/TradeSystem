package de.codingair.tradesystem.spigot.utils.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.TradeSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlLiteConnection {

    public static Connection connect() throws SQLException {
        //get correct case-sensitive plugins-folder name
        String name = TradeSystem.getInstance().getDataFolder().getParentFile().getName();
        String url = "jdbc:sqlite:" + name + "/TradeSystem/tradelog.db";
        return DriverManager.getConnection(url);
    }
}
