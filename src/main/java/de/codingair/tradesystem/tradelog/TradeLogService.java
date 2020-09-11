package de.codingair.tradesystem.tradelog;

import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.tradelog.repository.TradeLogRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class TradeLogService {

    private static TradeLogService instance;

    private TradeLogRepository tradeLogRepository = TradeSystem.getInstance().getTradeLogRepository();

    public static TradeLogService getTradeLog() {
        if (instance == null) {
            instance = new TradeLogService();
        }
        return instance;
    }

    private TradeLogService() {
    }

    public void log(Player player1, Player player2, String message) {
        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
            tradeLogRepository.log(player1, player2, message);
        });
    }

    public List<TradeLog> getLogMessages(String playerName) {
        return tradeLogRepository.getLogMessages(playerName);
    }

}
