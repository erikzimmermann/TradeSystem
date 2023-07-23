package de.codingair.tradesystem.spigot.utils.database.migrations.mysql;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.Supplier;
import de.codingair.tradesystem.spigot.utils.database.migrations.SqlMigrations;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MysqlMigrations extends SqlMigrations {

    static {
        getInstance().register(TradeSystem.getInstance(),
                new CreateTradeLogTableMigration(),
                new AddIndexTradeLogTableMigration()
        );
    }

    private static MysqlMigrations instance;
    private final Supplier<Connection, SQLException> connection;

    private MysqlMigrations() {
        connection = MySQLConnection.getConnection();
    }

    @Override
    public void setVersion(@NotNull Connection connection, @NotNull String user, int version) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO migrations VALUES (?, ?) ON DUPLICATE KEY UPDATE version=?;")) {
            stmt.setString(1, user);
            stmt.setInt(2, version);
            stmt.setInt(3, version);
            stmt.executeUpdate();
        }
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return connection.get();
    }

    @NotNull
    public static MysqlMigrations getInstance() {
        if (instance == null) instance = new MysqlMigrations();
        return instance;
    }
}
