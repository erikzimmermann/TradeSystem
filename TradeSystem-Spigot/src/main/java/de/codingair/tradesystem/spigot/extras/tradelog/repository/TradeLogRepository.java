package de.codingair.tradesystem.spigot.extras.tradelog.repository;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TradeLogRepository {

    void log(String player1, String playerTo2, String message);

    long count(String player, String message);

    @Nullable
    List<TradeLog.Entry> getLogMessages(String playerName);

    boolean haveTraded(String player1, String player2);
}
