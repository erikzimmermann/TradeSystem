package de.codingair.tradesystem.spigot.extras.tradelog.repository.adapters;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.TradeLogRepository;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class BukkitTradeLogRepository implements TradeLogRepository {

    @Override
    public void log(String player1, String player2, String message) {
        Bukkit.getLogger().info("TradeLog [" + player1 + ", " + player2+ "] " + message);
    }

    @Override
    public @Nullable List<TradeLog.Entry> getLogMessages(String player) {
        return Collections.emptyList();
    }

    @Override
    public boolean haveTraded(String player1, String player2) {
        return false;
    }
}
