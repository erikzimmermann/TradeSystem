package de.codingair.tradesystem.spigot.extras.tradelog.repository;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface TradeLogRepository {

    void registerOrUpdatePlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException;

    void log(String player1, String playerTo2, String message);

    long count(String player, String message);

    @Nullable
    List<TradeLog.Entry> getLogMessages(String playerName);

    boolean haveTraded(String player1, String player2);
}
