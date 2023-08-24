package de.codingair.tradesystem.spigot.database.migrations;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.ext.Extensions;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteException;

import java.sql.*;
import java.util.*;

public abstract class SqlMigrations {
    private final Map<String, List<Migration>> migrations = new HashMap<>();

    /**
     * @return A connection to the database.
     * @throws SQLException If a database access error occurs.
     */
    @NotNull
    public abstract Connection getConnection() throws SQLException;

    /**
     * @param connection The connection to the database.
     * @param user       The user to set the version for.
     * @param version    The version to set.
     * @throws SQLException If a database access error occurs.
     */
    public abstract void setVersion(@NotNull Connection connection, @NotNull String user, int version) throws SQLException;

    /**
     * Creates the migration table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs.
     */
    public void createMigrationTable() throws SQLException {
        try (Connection connection = getConnection()) {
            int migrationVersion = getOldMigrationVersion(connection);
            createNewMigrationTable(connection, migrationVersion);
        }
    }

    private static int getOldMigrationVersion(@NotNull Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // include `id` to be sure this is the old table
            String sql = "SELECT max(version) as max FROM migrations WHERE id IS NOT NULL;";
            ResultSet set = stmt.executeQuery(sql);

            if (set.next()) return set.getInt("max");
        } catch (SQLSyntaxErrorException | SQLiteException e) {
            // "Table 'tradesystem.migrations' doesn't exist" or
            // "Unknown column 'id' in 'where clause'"
            // -> just create new table
        }
        return 0;
    }

    /**
     * Runs all migrations that are not yet applied.
     *
     * @throws SQLException If a database access error occurs.
     */
    public void runMigrations() throws SQLException {
        try (Connection connect = getConnection()) {
            connect.setAutoCommit(false);

            Map<String, List<Migration>> migrations = getRelevantMigrations();

            // run TradeSystem migrations first
            String pluginUser = TradeSystem.getInstance().getName().toLowerCase();
            runMigrations(connect, pluginUser, migrations.remove(pluginUser));

            // run external migrations
            for (Map.Entry<String, List<Migration>> e : migrations.entrySet()) {
                runMigrations(connect, e.getKey(), e.getValue());
            }

            connect.commit();
        }
    }

    private void runMigrations(@NotNull Connection connect, @NotNull String user, @Nullable List<Migration> migrations) throws SQLException {
        if (migrations == null || migrations.isEmpty()) return;

        for (Migration migration : migrations) {
            String[] commands;
            if (migration instanceof Migration.MultiMigration) {
                commands = ((Migration.MultiMigration) migration).getStatements();
            } else commands = new String[]{migration.getStatement()};

            for (String command : commands) {
                try (Statement stmt = connect.createStatement()) {
                    stmt.execute(command);
                }
            }
        }

        setVersion(connect, user, this.migrations.get(user.toLowerCase()).size());
    }

    private void createNewMigrationTable(@NotNull Connection connection, int previousVersion) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            if (previousVersion > 0) stmt.execute("DROP TABLE IF EXISTS migrations;");
            stmt.execute("CREATE TABLE IF NOT EXISTS migrations (user VARCHAR(25) PRIMARY KEY, version integer NOT NULL);");
        }

        if (previousVersion > 0) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO migrations VALUES (?, ?);")) {
                stmt.setString(1, TradeSystem.getInstance().getDescription().getName().toLowerCase());
                stmt.setInt(2, previousVersion);
                stmt.executeUpdate();
            }
        }
    }

    private @NotNull Map<String, Integer> getVersions() throws SQLException {
        Map<String, Integer> versions = new HashMap<>();

        try (Connection connect = getConnection();
             Statement stmt = connect.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM migrations;");

            while (resultSet.next()) {
                String name = resultSet.getString("user");
                int version = resultSet.getInt("version");
                versions.put(name, version);
            }
        }
        return versions;
    }

    @NotNull
    private Map<String, List<Migration>> getRelevantMigrations() throws SQLException {
        Map<String, List<Migration>> relevant = new HashMap<>();

        for (Map.Entry<String, List<Migration>> e : migrations.entrySet()) {
            List<Migration> m = e.getValue();
            if (m == null) continue;

            // version starts at 1
            int version = getVersions().getOrDefault(e.getKey(), 0);
            if (m.size() <= version) continue;

            relevant.put(e.getKey(), m.subList(version, m.size()));
        }

        return relevant;
    }

    /**
     * @param plugin     The plugin that registers the migrations. This is used to identify the version of the given migrations.
     * @param migrations The migrations to register. Migrations will be executed in the order they are passed.
     */
    public void register(@NotNull JavaPlugin plugin, @NotNull Migration @NotNull ... migrations) {
        if (!plugin.equals(TradeSystem.getInstance()) && !Extensions.isSupported(plugin)) {
            TradeSystem.getInstance().getLogger().warning("Plugin '" + plugin.getName() + "' is not a registered extension by TradeSystem. " +
                    "Migrations will not be executed.");
            return;
        }

        String name = plugin.getName().toLowerCase();
        this.migrations.compute(name, (k, v) -> {
            if (v == null) v = new ArrayList<>();
            Collections.addAll(v, migrations);
            return v;
        });
    }
}
