package de.codingair.tradesystem.spigot.extras.tradelog;

import de.codingair.codingapi.tools.Callback;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.database.DatabaseType;
import de.codingair.tradesystem.spigot.database.migrations.mysql.MySQLConnection;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.TradeLogRepository;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.adapters.MysqlTradeLogRepository;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.adapters.SqlLiteTradeLogRepository;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeLogService {
    private static TradeLogService instance;
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

    public static void registerOrUpdatePlayer(@NotNull UUID uniqueId, @NotNull String name) {
        if (!connected()) return;

        try {
            getTradeLogRepository().registerOrUpdatePlayer(uniqueId, name);
        } catch (Exception e) {
            TradeSystem.getInstance().getLogger().severe("Failed to register or update player information: " + e.getMessage());
        }
    }

    public static long count(@NotNull String player, @NotNull String message) {
        if (!connected()) return 0;

        return getTradeLogRepository().count(player, message);
    }

    public static void log(@NotNull String player1, @NotNull String player2, @Nullable String message) {
        logLater(player1, player2, message, 0);
    }

    public static void logLater(@NotNull String player1, @NotNull String player2, @Nullable String message, long delay) {
        if (message == null || !connected()) return;

        Runnable runnable = () -> {
            if (getTradeLog().bukkitLogger)
                Bukkit.getLogger().info("TradeLog [" + player1 + ", " + player2 + "] " + message);
            getTradeLogRepository().log(player1, player2, message);
        };

        //it will throw an error if the plugin is not enabled
        if (TradeSystem.getInstance().isEnabled())
            Bukkit.getScheduler().runTaskLaterAsynchronously(TradeSystem.getInstance(), runnable, delay);
        else runnable.run();
    }

    public static List<TradeLog.Entry> getLogMessages(String playerName) {
        if (!connected()) return new ArrayList<>();
        return getTradeLogRepository().getLogMessages(playerName);
    }

    public static boolean haveTraded(@NotNull String player1, @NotNull String player2) {
        if (!connected()) return false;
        return getTradeLogRepository().haveTraded(player1, player2);
    }

    public static void haveTraded(@NotNull String player1, @NotNull String player2, @NotNull Callback<Boolean> callback) {
        if (!connected()) {
            callback.accept(false);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(),
                () -> callback.accept(getTradeLogRepository().haveTraded(player1, player2))
        );
    }

    public static boolean connected() {
        return TradeLog.isEnabled() && TradeSystem.getInstance().getDatabaseInitializer().isRunning();
    }

    @NotNull
    private static TradeLogRepository getTradeLogRepository() {
        if (!TradeLog.isEnabled()) throw new NullPointerException("TradeLog is not enabled.");

        DatabaseType type = TradeSystem.database().getType();
        switch (type) {
            case MYSQL:
                return new MysqlTradeLogRepository();
            case SQLITE:
                return new SqlLiteTradeLogRepository();
            default:
                throw new RuntimeException("Invalid database type provided: " + type);
        }
    }

}
