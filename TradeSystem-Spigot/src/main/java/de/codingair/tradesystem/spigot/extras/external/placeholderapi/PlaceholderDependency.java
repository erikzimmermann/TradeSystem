package de.codingair.tradesystem.spigot.extras.external.placeholderapi;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.external.PluginDependency;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.utils.Lang;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderDependency implements PluginDependency {
    private static PlaceholderDependency instance;
    static final String IDENTIFIER = "tradesystem";
    static final Pattern PATTERN = Pattern.compile("%" + IDENTIFIER + "_[a-z_]+%", Pattern.CASE_INSENSITIVE);
    private TradeSystemPlaceholder placeholder;

    public PlaceholderDependency() {
        instance = this;
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
                    return remaining + "";
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
        }

        return null;
    }

    @Override
    public @NotNull String getPluginName() {
        return "PlaceholderAPI";
    }
}
