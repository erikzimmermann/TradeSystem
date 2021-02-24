package de.codingair.tradesystem.tradelog;

import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.tradelog.repository.TradeLogRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public void log(Player player1, Player player2, String message) {
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
