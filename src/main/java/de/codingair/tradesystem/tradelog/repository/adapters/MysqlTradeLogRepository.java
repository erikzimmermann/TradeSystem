package de.codingair.tradesystem.tradelog.repository.adapters;

import de.codingair.tradesystem.tradelog.TradeLog;
import de.codingair.tradesystem.tradelog.repository.TradeLogRepository;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class MysqlTradeLogRepository implements TradeLogRepository {
    private final Connection connection;

    public MysqlTradeLogRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void log(Player player1, Player player2, String message) {
        String sql = "INSERT INTO tradelog(player1, player2, message, timestamp) VALUES(?,?,?,?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player1.getName());
            pstmt.setString(2, player2.getName());
            pstmt.setString(3, message);
            pstmt.setLong(4, System.currentTimeMillis());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<TradeLog> getLogMessages(String playerName) {
        String sql = "SELECT id, player1, player2, message, timestamp FROM tradelog " +
                "WHERE player1=? OR player2=? ORDER BY timestamp DESC LIMIT 20;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, playerName);
            ResultSet rs = pstmt.executeQuery();

            List<TradeLog> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new TradeLog(
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        Instant.ofEpochMilli(rs.getLong(5)).atZone(ZoneId.systemDefault()).toLocalDateTime()
                ));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
