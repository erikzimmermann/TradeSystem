package de.codingair.tradesystem.tradelog.repository;

import de.codingair.tradesystem.tradelog.TradeLog;
import org.bukkit.entity.Player;

import java.util.List;

public interface TradeLogRepository {

    void log(Player player1, Player playerTo2, String message);

    List<TradeLog> getLogMessages(String playerName);
}
