package de.codingair.tradesystem.spigot.extras.tradelog;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.TradeLogRepository;
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
        if (notConnected()) return;

        Runnable runnable = () -> tradeLogRepository.log(player1, player2, message);

        //it will throw an error if the plugin is not enabled
        if (TradeSystem.getInstance().isEnabled()) Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(), runnable);
        else runnable.run();
    }

    public void logLater(String player1, String player2, String message, long delay) {
        if (notConnected()) return;

        Runnable runnable = () -> tradeLogRepository.log(player1, player2, message);

        //it will throw an error if the plugin is not enabled
        if (TradeSystem.getInstance().isEnabled()) Bukkit.getScheduler().runTaskLaterAsynchronously(TradeSystem.getInstance(), runnable, delay);
        else runnable.run();
    }

    public List<TradeLog> getLogMessages(String playerName) {
        if (notConnected()) return new ArrayList<>();
        return tradeLogRepository.getLogMessages(playerName);
    }

    public boolean notConnected() {
        return tradeLogRepository == null || !TradeSystem.getInstance().getDatabaseInitializer().isRunning();
    }

}
