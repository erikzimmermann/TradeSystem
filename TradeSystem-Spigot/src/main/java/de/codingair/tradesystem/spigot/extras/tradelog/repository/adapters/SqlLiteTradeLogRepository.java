package de.codingair.tradesystem.spigot.extras.tradelog.repository.adapters;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.database.migrations.sqlite.SqlLiteConnection;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.TradeLogRepository;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlLiteTradeLogRepository implements TradeLogRepository {

    @Override
    public void registerOrUpdatePlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        String sql = "INSERT INTO trade_players(uuid, name) VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET name=?;";

        try (Connection con = SqlLiteConnection.connect();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setString(3, name);

            pstmt.executeUpdate();
        }
    }

    @Override
    public void log(String player1, String player2, String message) {
        String sql = "INSERT INTO tradelog(player1, player2, message, timestamp) VALUES(?,?,?,?)";

        try (Connection conn = SqlLiteConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player1);
            pstmt.setString(2, player2);
            pstmt.setString(3, message);
            pstmt.setLong(4, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            TradeSystem.getInstance().getLogger().severe("Could not log player '" + player1 + "' with player '" + player2 + "' ('" + message + "'): " + e.getMessage() + " [SQLite]");
        }
    }

    @Override
    public long count(String player, String message) {
        String sql = "SELECT COUNT(1) as count FROM tradelog WHERE (player1=? OR player2=?) AND message like ?;";

        try (Connection conn = SqlLiteConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player);
            pstmt.setString(2, player);
            pstmt.setString(3, message);

            ResultSet set = pstmt.executeQuery();
            return set.next() ? set.getLong("count") : 0;
        } catch (SQLException e) {
            TradeSystem.getInstance().getLogger().severe("Could not count player '" + player + "' with message '" + message + "': " + e.getMessage() + " [SQLite]");
            return 0;
        }
    }

    @Override
    public @Nullable List<TradeLog.Entry> getLogMessages(String playerName) {
        String sql = "SELECT id, player1, player2, message, timestamp FROM tradelog " +
                "WHERE player1=? OR player2=? ORDER BY timestamp DESC LIMIT 40;";

        try (Connection conn = SqlLiteConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, playerName);
            ResultSet rs = pstmt.executeQuery();

            List<TradeLog.Entry> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new TradeLog.Entry(
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        Instant.ofEpochMilli(rs.getLong(5)).atZone(ZoneId.systemDefault()).toLocalDateTime()
                ));
            }
            return result;
        } catch (SQLException e) {
            TradeSystem.getInstance().getLogger().severe("Could not access log messages for player '" + playerName + "': " + e.getMessage() + " [SQLite]");
            return null;
        }
    }

    @Override
    public boolean haveTraded(String player1, String player2) {
        String sql = "SELECT 1 FROM tradelog " +
                "WHERE player1=? AND player2=? OR player1=? AND player2=? LIMIT 1;";

        try (Connection conn = SqlLiteConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player1);
            pstmt.setString(2, player2);
            pstmt.setString(3, player2);
            pstmt.setString(4, player1);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            TradeSystem.getInstance().getLogger().severe("Could not check have traded '" + player1 + "' & '" + player2 + "': " + e.getMessage() + " [SQLite]");
            return false;
        }
    }
}
