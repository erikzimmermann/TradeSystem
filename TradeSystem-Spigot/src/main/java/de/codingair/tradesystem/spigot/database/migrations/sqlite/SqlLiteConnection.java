package de.codingair.tradesystem.spigot.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.TradeSystem;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;

public class SqlLiteConnection {

    @NotNull
    public static Connection connect() throws Exception {
        //get correct case-sensitive plugins-folder name
        String name = TradeSystem.getInstance().getDataFolder().getParentFile().getName();
        String url = "jdbc:sqlite:" + name + "/TradeSystem/tradelog.db";
        return DriverManager.getConnection(url);
    }
}
