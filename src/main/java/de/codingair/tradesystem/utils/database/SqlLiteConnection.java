package de.codingair.tradesystem.utils.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlLiteConnection {

    public static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:TradeSystem/tradelog.db";
        return DriverManager.getConnection(url);
    }
}
