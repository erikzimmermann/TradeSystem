package de.codingair.tradesystem.spigot.extras.external.placeholderapi;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeFinishEvent;
import de.codingair.tradesystem.spigot.extras.external.PluginDependency;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.utils.Lang;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderDependency implements PluginDependency, Listener {
    private static PlaceholderDependency instance;
    static final String IDENTIFIER = "tradesystem";
    static final Pattern PATTERN = Pattern.compile("%" + IDENTIFIER + "_[a-z_]+%", Pattern.CASE_INSENSITIVE);
    private TradeSystemPlaceholder placeholder;
    private static final Map<UUID, Long> successfulTrades = new HashMap<>();

    public PlaceholderDependency() {
        instance = this;
    }

    @EventHandler
    public void onTradeFinish(TradeFinishEvent e) {
        if (e.getTradeResult() && TradeLogService.connected()) {
            if (e.getSendingPlayer() != null) increaseSuccessfulTrades(e.getSenderId());
            if (e.getReceivingPlayer() != null) increaseSuccessfulTrades(e.getReceiverId());
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        long trades = TradeLogService.count(e.getName(), TradeLog.FINISHED.get());
        if (trades > 0) successfulTrades.put(e.getUniqueId(), trades);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        successfulTrades.remove(e.getPlayer().getUniqueId());
    }

    private void increaseSuccessfulTrades(@NotNull UUID senderId) {
        long trades = successfulTrades.getOrDefault(senderId, 0L);
        successfulTrades.put(senderId, trades + 1);
    }

    @NotNull
    public static String convert(@NotNull String s, @NotNull Player player) {
        if (instance.isAvailable()) return PlaceholderAPI.setPlaceholders(player, s);
        else {
            Matcher m = PATTERN.matcher(s);
            while (m.find()) {
                String group = m.group();                                                      // includes "tradesystem" identifier
                String id = group.substring(1 + IDENTIFIER.length() + 1, group.length() - 1);  // remove plugin identifier

                String replacement = apply(player, id);
                if (replacement == null) continue;

                s = s.substring(0, m.start()) + replacement + s.substring(m.end());
            }

            return s;
        }
    }

    @Override
    public void onEnable() {
        placeholder = new TradeSystemPlaceholder();
        placeholder.register();
    }

    @Override
    public void onDisable() {
        if (placeholder != null) placeholder.unregister();
    }

    @Nullable
    static String apply(@NotNull Player player, @NotNull String id) {
        Trade t = TradeSystem.handler().getTrade(player);

        switch (id.toLowerCase()) {
            case "partner":
            case "trade_partner":
                if (t != null) return t.getOther(player.getName());
                else return "";

            case "countdown":
                if (t != null && t.getCountdown() != null) {
                    int remaining = (int) Math.ceil((TradeSystem.handler().getCountdownInterval() * (TradeSystem.handler().getCountdownRepetitions() - t.getCountdownTicks())) / 20F);
                    return String.valueOf(remaining);
                } else return "";

            case "countdown_fancy":
                if (t != null && t.getCountdown() != null) {
                    int remaining = (int) Math.ceil((TradeSystem.handler().getCountdownInterval() * (TradeSystem.handler().getCountdownRepetitions() - t.getCountdownTicks())) / 20F);
                    return Lang.get("Fancy_Countdown").replace("%seconds%", remaining + "");
                }
                return "";

            case "status":
                if (TradeSystem.handler().isOffline(player)) return Lang.get("Offline");
                else return Lang.get("Online");

            case "is_trading":
                return t != null ? "true" : "false";

            case "num_trades":
                return String.valueOf(successfulTrades.getOrDefault(player.getUniqueId(), 0L));
        }

        return null;
    }

    @Override
    public @NotNull String getPluginName() {
        return "PlaceholderAPI";
    }
}
