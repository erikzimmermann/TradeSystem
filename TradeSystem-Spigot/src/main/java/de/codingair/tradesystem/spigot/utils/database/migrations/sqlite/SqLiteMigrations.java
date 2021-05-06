package de.codingair.tradesystem.spigot.utils.database.migrations.sqlite;

import de.codingair.tradesystem.spigot.utils.database.migrations.Migration;
import de.codingair.tradesystem.spigot.utils.database.migrations.SqlMigrations;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SqLiteMigrations implements SqlMigrations {

    private static SqLiteMigrations instance;
    // Define all migrations in this list.
    private static final List<Migration> migrations = Collections.singletonList(new CreateTradeLogTableMigration());

    private SqLiteMigrations() {
    }

    public static SqLiteMigrations getInstance() {
        if (instance == null) {
            instance = new SqLiteMigrations();
        }
        return instance;
    }

    @Override
    public void createMigrationTable() throws SQLException {
        try (Connection connect = SqlLiteConnection.connect();
             Statement stmt = connect.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS migrations (\n"
                    + "	id integer PRIMARY KEY,\n"
                    + "	version integer NOT NULL\n"
                    + ");";
            stmt.execute(sql);
        }
    }

    @Override
    public void runMigrations() throws SQLException {
        try (Connection connect = SqlLiteConnection.connect()) {
            connect.setAutoCommit(false);
            int maxVersion = getMaxVersion();

            List<Migration> validMigrations = migrations.stream().filter(m -> m.getVersion() > maxVersion)
                    .sorted(Comparator.comparingInt(Migration::getVersion))
                    .collect(Collectors.toList());

            for (Migration migration : validMigrations) {
                try (Statement stmt = connect.createStatement()) {
                    String sql = migration.getStatement();
                    stmt.execute(sql);
                    PreparedStatement migrationStatement = connect.prepareStatement("INSERT INTO migrations (version) VALUES (?);");
                    migrationStatement.setInt(1, migration.getVersion());
                    migrationStatement.execute();

                    connect.commit();
                }
            }
        }
    }

    private int getMaxVersion() {
        try (Connection connect = SqlLiteConnection.connect();
             Statement stmt = connect.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT max(version) as max from migrations");
            return resultSet.next() ? resultSet.getInt("max") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
