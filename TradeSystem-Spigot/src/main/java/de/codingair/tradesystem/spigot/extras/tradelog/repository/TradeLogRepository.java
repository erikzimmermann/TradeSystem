package de.codingair.tradesystem.spigot.extras.tradelog.repository;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;

import java.util.List;

public interface TradeLogRepository {

    void log(String player1, String playerTo2, String message);

    List<TradeLog> getLogMessages(String playerName);
}
