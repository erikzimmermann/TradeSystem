package de.codingair.tradesystem.spigot.tradelog;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.tradelog.repository.TradeLogRepository;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class TradeLogService {
    private static TradeLogService instance;
    private final TradeLogRepository tradeLogRepository = TradeSystem.getInstance().getTradeLogRepository();

    private TradeLogService() {
    }

    public static TradeLogService getTradeLog() {
        if (instance == null) instance = new TradeLogService();
        return instance;
    }

    public void log(String player1, String player2, String message) {
        if (tradeLogRepository == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(), () -> {
            tradeLogRepository.log(player1, player2, message);
        });
    }

    public List<TradeLog> getLogMessages(String playerName) {
        if (tradeLogRepository == null) return new ArrayList<>();
        return tradeLogRepository.getLogMessages(playerName);
    }

}
