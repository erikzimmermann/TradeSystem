package de.codingair.tradesystem.spigot.extras.placeholderapi;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.utils.Lang;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAPI {
    private static Boolean enabled = null;
    static final String IDENTIFIER = "tradesystem";
    static final Pattern PATTERN = Pattern.compile("%" + IDENTIFIER + "_[a-z_]+%", Pattern.CASE_INSENSITIVE);

    public static boolean isEnabled() {
        if (enabled == null) enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        return enabled;
    }

    @NotNull
    public static String convert(@NotNull String s, @NotNull Player player) {
        if (isEnabled()) return PlaceholderAPI.setPlaceholders(player, s);
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

    public static void register() {
        if (isEnabled()) new TradeSystemPlaceholder().register();
    }

    @Nullable
    static String apply(@NotNull Player player, @NotNull String id) {
        Trade t = TradeSystem.man().getTrade(player);

        switch (id.toLowerCase()) {
            case "partner":
            case "trade_partner": {
                if (t != null) return t.getOther(player.getName());
                else return "";
            }
            case "countdown": {
                if (t != null && t.getCountdown() != null) {
                    int remaining = (int) Math.ceil((TradeSystem.man().getCountdownInterval() * (TradeSystem.man().getCountdownRepetitions() - t.getCountdownTicks())) / 20F);
                    return remaining + "";
                } else return "";
            }
            case "countdown_fancy": {
                if (t != null && t.getCountdown() != null) {
                    int remaining = (int) Math.ceil((TradeSystem.man().getCountdownInterval() * (TradeSystem.man().getCountdownRepetitions() - t.getCountdownTicks())) / 20F);
                    return Lang.get("Fancy_Countdown").replace("%seconds%", remaining + "");
                }
                return "";
            }
            case "status":
                if (TradeSystem.man().isOffline(player)) return Lang.get("Offline");
                else return Lang.get("Online");
        }

        return null;
    }
}
