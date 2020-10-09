package de.codingair.tradesystem.extras.placeholderapi;

import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.Trade;
import de.codingair.tradesystem.utils.Lang;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PAPI {
    private static Boolean enabled = null;

    public static boolean isEnabled() {
        if(enabled == null) enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        return enabled;
    }

    public static String convert(String s, Player player) {
        if(isEnabled()) return PlaceholderAPI.setPlaceholders(player, s);
        else {
            Trade t = TradeSystem.man().getTrade(player);

            if(t != null) {
                int remaining = (int) Math.ceil((TradeSystem.man().getCountdownInterval() * (TradeSystem.man().getCountdownRepetitions() - t.getCountdownTicks())) / 20);

                return s
                        .replaceAll("(?i:%tradesystem_trade_partner%)", t.getOther(player).getDisplayName())
                        .replaceAll("(?i:%tradesystem_countdown%)", t.getCountdown() == null ? "" : remaining + "")
                        .replaceAll("(?i:%tradesystem_countdown_fancy%)", t.getCountdown() == null ? "" : Lang.get("Fancy_Countdown").replace("%seconds%", remaining + "") + "")
                        .replaceAll("(?i:%tradesystem_status%)", TradeSystem.man().isOffline(player) ? Lang.get("Offline") : Lang.get("Online"))
                        ;
            }

            return s.replaceAll("(?i:%tradesystem_status%)", TradeSystem.man().isOffline(player) ? Lang.get("Offline") : Lang.get("Online"));
        }
    }

    public static void register() {
        if(isEnabled()) new TradeSystemPlaceholder().register();
    }
}
