package de.codingair.tradesystem.utils.database.migrations.mysql;

import de.codingair.tradesystem.utils.database.MySQLConnection;
import de.codingair.tradesystem.utils.database.migrations.Migration;
import de.codingair.tradesystem.utils.database.migrations.SqlMigrations;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MysqlMigrations implements SqlMigrations {

    private static MysqlMigrations instance;
    // Define all migrations in this list.
    private static List<Migration> migrations = Arrays.asList(new CreateTradeLogTableMigration());
    private final DataSource datasource;

    private MysqlMigrations() {
        datasource = MySQLConnection.getDatasource();
    }

    public static MysqlMigrations getInstance() {
        if (instance == null) {
            instance = new MysqlMigrations();
        }
        return instance;
    }

    @Override
    public void createMigrationTable() {
        try (Connection connect = datasource.getConnection()) {
            Bukkit.getLogger().info("Creating migration table");

            // SQL statement for creating a new table
            String sql = "CREATE TABLE IF NOT EXISTS migrations (\n"
                    + "	id BIGINT PRIMARY KEY AUTO_INCREMENT,\n"
                    + "	version integer NOT NULL\n"
                    + ");";
            Statement stmt = connect.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failure creating migration table: " + e.getMessage());
        }

    }

    @Override
    public void runMigrations() {
        try (Connection connect = datasource.getConnection()) {
            Bukkit.getLogger().info("Starting migrations");
            connect.setAutoCommit(false);
            int maxVersion = getMaxVersion();

            List<Migration> validMigrations = migrations.stream().filter(m -> m.getVersion() > maxVersion)
                    .sorted(Comparator.comparingInt(Migration::getVersion))
                    .collect(Collectors.toList());

            for (Migration migration : validMigrations) {
                String sql = migration.getStatement();
                Statement stmt = connect.createStatement();
                stmt.execute(sql);

                PreparedStatement migrationStatement = connect.prepareStatement("INSERT INTO migrations (version) VALUES (?);");
                migrationStatement.setInt(1, migration.getVersion());
                migrationStatement.execute();

                connect.commit();
            }
            // SQL statement for creating a new table
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failure executing migrations: " + e.getMessage());
        }
    }

    private int getMaxVersion() {
        try (Connection connect = datasource.getConnection()) {
            Statement stmt = connect.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT max(version) as max from migrations");
            int max = resultSet.next() ? resultSet.getInt("max") : 0;
            Bukkit.getLogger().info("Latest migration version = " + max);
            return max;

            // SQL statement for creating a new table
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failure retrieving max migration version: " + e.getMessage());
        }
        return 0;
    }
}
