package de.codingair.tradesystem.spigot.utils.database.migrations.mysql;

import de.codingair.tradesystem.spigot.utils.database.migrations.Migration;
import de.codingair.tradesystem.spigot.utils.database.migrations.SqlMigrations;

import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MysqlMigrations implements SqlMigrations {
    // Define all migrations in this list.
    private static final List<Migration> migrations = Arrays.asList(new CreateTradeLogTableMigration());
    private static MysqlMigrations instance;
    private final Connection connection;

    private MysqlMigrations() {
        connection = MySQLConnection.getConnection();
    }

    public static MysqlMigrations getInstance() {
        if (instance == null) {
            instance = new MysqlMigrations();
        }
        return instance;
    }

    @Override
    public void createMigrationTable() {
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS migrations (\n"
                    + "	id BIGINT PRIMARY KEY AUTO_INCREMENT,\n"
                    + "	version integer NOT NULL\n"
                    + ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void runMigrations() {
        try {
            int maxVersion = getMaxVersion();

            List<Migration> validMigrations = migrations.stream().filter(m -> m.getVersion() > maxVersion)
                    .sorted(Comparator.comparingInt(Migration::getVersion))
                    .collect(Collectors.toList());

            for (Migration migration : validMigrations) {
                try (Statement stmt = connection.createStatement()) {
                    String sql = migration.getStatement();
                    stmt.execute(sql);

                    PreparedStatement migrationStatement = connection.prepareStatement("INSERT INTO migrations (version) VALUES (?);");
                    migrationStatement.setInt(1, migration.getVersion());
                    migrationStatement.execute();

                    connection.commit();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getMaxVersion() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT max(version) as max from migrations");
            return resultSet.next() ? resultSet.getInt("max") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
