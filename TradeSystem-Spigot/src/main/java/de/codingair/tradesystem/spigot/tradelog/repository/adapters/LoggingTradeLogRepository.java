package de.codingair.tradesystem.spigot.tradelog.repository.adapters;

import de.codingair.tradesystem.spigot.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.tradelog.repository.TradeLogRepository;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.List;

public class LoggingTradeLogRepository implements TradeLogRepository {

    @Override
    public void log(String player1, String player2, String message) {
        Bukkit.getLogger().info("TRADELOG [" + player1 + " , " + player2+ "] " + message);
    }

    @Override
    public List<TradeLog> getLogMessages(String player) {
        return Collections.emptyList();
    }
}
