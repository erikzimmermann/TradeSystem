package de.codingair.tradesystem.spigot.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.database.migrations.SqlMigrations;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqLiteMigrations extends SqlMigrations {
    private static SqLiteMigrations instance;

    static {
        getInstance().register(TradeSystem.getInstance(),
                new CreateTradeLogTableMigration(),
                new AddIndexTradeLogTableMigration(),
                new CreatePlayerNameTableMigration()
        );
    }

    private SqLiteMigrations() {
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return SqlLiteConnection.connect();
    }

    @Override
    public void setVersion(@NotNull Connection connection, @NotNull String user, int version) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO migrations VALUES (?, ?) ON CONFLICT(user) DO UPDATE SET version=?;")) {
            stmt.setString(1, user);
            stmt.setInt(2, version);
            stmt.setInt(3, version);
            stmt.executeUpdate();
        }
    }

    @NotNull
    public static SqLiteMigrations getInstance() {
        if (instance == null) instance = new SqLiteMigrations();
        return instance;
    }
}
