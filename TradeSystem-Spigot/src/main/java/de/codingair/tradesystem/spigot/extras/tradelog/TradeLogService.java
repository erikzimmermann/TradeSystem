package de.codingair.tradesystem.spigot.extras.tradelog;

import de.codingair.codingapi.tools.Callback;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.TradeLogRepository;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.adapters.MysqlTradeLogRepository;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.adapters.SqlLiteTradeLogRepository;
import de.codingair.tradesystem.spigot.utils.database.DatabaseType;
import de.codingair.tradesystem.spigot.utils.database.DatabaseUtil;
import de.codingair.tradesystem.spigot.utils.database.migrations.mysql.MySQLConnection;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TradeLogService {
    private static TradeLogService instance;
    private final TradeLogRepository tradeLogRepository = getTradeLogRepository();
    private final boolean bukkitLogger;

    private TradeLogService() {
        bukkitLogger = TradeSystem.getInstance().getFileManager()
                .getFile("Config").getConfig()
                .getBoolean("TradeSystem.TradeLog.Bukkit_logger", false);
    }

    private static TradeLogService getTradeLog() {
        if (instance == null) instance = new TradeLogService();
        return instance;
    }

    public static void log(@NotNull String player1, @NotNull String player2, @Nullable String message) {
        logLater(player1, player2, message, 0);
    }

    public static void logLater(@NotNull String player1, @NotNull String player2, @Nullable String message, long delay) {
        if (message == null || notConnected()) return;

        Runnable runnable = () -> {
            if (getTradeLog().bukkitLogger)
                Bukkit.getLogger().info("TradeLog [" + player1 + ", " + player2 + "] " + message);
            getTradeLog().tradeLogRepository.log(player1, player2, message);
        };

        //it will throw an error if the plugin is not enabled
        if (TradeSystem.getInstance().isEnabled())
            Bukkit.getScheduler().runTaskLaterAsynchronously(TradeSystem.getInstance(), runnable, delay);
        else runnable.run();
    }

    public static List<TradeLog.Entry> getLogMessages(String playerName) {
        if (notConnected()) return new ArrayList<>();
        return getTradeLog().tradeLogRepository.getLogMessages(playerName);
    }

    public static boolean haveTraded(@NotNull String player1, @NotNull String player2) {
        if (notConnected()) return false;
        return getTradeLog().tradeLogRepository.haveTraded(player1, player2);
    }

    public static void haveTraded(@NotNull String player1, @NotNull String player2, @NotNull Callback<Boolean> callback) {
        if (notConnected()) {
            callback.accept(false);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(),
                () -> callback.accept(getTradeLog().tradeLogRepository.haveTraded(player1, player2))
        );
    }

    public static boolean notConnected() {
        return getTradeLog().tradeLogRepository == null || !TradeSystem.getInstance().getDatabaseInitializer().isRunning();
    }

    public TradeLogRepository getTradeLogRepository() {
        if (!TradeLog.isEnabled()) {
            return null;
        }

        DatabaseType type = DatabaseUtil.database().getType();
        switch (type) {
            case MYSQL:
                return new MysqlTradeLogRepository(MySQLConnection.getConnection());
            case SQLITE:
                return new SqlLiteTradeLogRepository();
            default:
                throw new RuntimeException("Invalid database type provided: " + type);
        }
    }

}
