package de.codingair.tradesystem.utils.database.migrations.mysql;

import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.utils.database.migrations.Migration;
import de.codingair.tradesystem.utils.database.migrations.SqlMigrations;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MysqlMigrations implements SqlMigrations {

    private static MysqlMigrations instance;
    // Define all migrations in this list.
    private static List<Migration> migrations = Arrays.asList(new CreateTradeLogTableMigration());
    private final Connection connection;

    private MysqlMigrations() {
        connection = MySQLConnection.getConnection();
    }

    public static MysqlMigrations getInstance() {
        if(instance == null) {
            instance = new MysqlMigrations();
        }
        return instance;
    }

    @Override
    public void createMigrationTable() {
        try(Statement stmt = connection.createStatement()) {
            TradeSystem.log("    > Check table");

            String sql = "CREATE TABLE IF NOT EXISTS migrations (\n"
                    + "	id BIGINT PRIMARY KEY AUTO_INCREMENT,\n"
                    + "	version integer NOT NULL\n"
                    + ");";
            stmt.execute(sql);
        } catch(SQLException e) {
            Bukkit.getLogger().severe("    > Failure creating migration table: " + e.getMessage());
        }

    }

    @Override
    public void runMigrations() {
        try {
            TradeSystem.log("    > Starting migrations");
            connection.setAutoCommit(false);
            int maxVersion = getMaxVersion();

            List<Migration> validMigrations = migrations.stream().filter(m -> m.getVersion() > maxVersion)
                    .sorted(Comparator.comparingInt(Migration::getVersion))
                    .collect(Collectors.toList());

            for(Migration migration : validMigrations) {
                try(Statement stmt = connection.createStatement()) {
                    String sql = migration.getStatement();
                    stmt.execute(sql);

                    PreparedStatement migrationStatement = connection.prepareStatement("INSERT INTO migrations (version) VALUES (?);");
                    migrationStatement.setInt(1, migration.getVersion());
                    migrationStatement.execute();

                    connection.commit();
                }
            }
        } catch(SQLException e) {
            Bukkit.getLogger().severe("    > Failure executing migrations: " + e.getMessage());
        }
    }

    private int getMaxVersion() {
        try(Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT max(version) as max from migrations");
            int max = resultSet.next() ? resultSet.getInt("max") : 0;
            TradeSystem.log("    > Latest migration version = " + max);
            return max;
        } catch(SQLException e) {
            Bukkit.getLogger().severe("    > Failure retrieving max migration version: " + e.getMessage());
        }
        return 0;
    }
}
