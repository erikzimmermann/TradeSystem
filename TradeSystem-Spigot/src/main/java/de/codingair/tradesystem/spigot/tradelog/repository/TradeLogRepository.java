package de.codingair.tradesystem.spigot.tradelog.repository;

import de.codingair.tradesystem.spigot.tradelog.TradeLog;

import java.util.List;

public interface TradeLogRepository {

    void log(String player1, String playerTo2, String message);

    List<TradeLog> getLogMessages(String playerName);
}
