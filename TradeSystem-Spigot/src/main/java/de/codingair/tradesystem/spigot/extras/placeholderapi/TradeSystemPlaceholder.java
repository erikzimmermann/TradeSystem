package de.codingair.tradesystem.spigot.extras.placeholderapi;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.utils.Lang;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TradeSystemPlaceholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "tradesystem";
    }

    @Override
    public @NotNull String getAuthor() {
        return "CodingAir";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public String onPlaceholderRequest(Player p, String id) {
        switch (id.toLowerCase()) {
            case "trade_partner": {
                Trade t = TradeSystem.man().getTrade(p);
                if (t != null) return t.getOther(p.getName());
                break;
            }
            case "countdown": {
                Trade t = TradeSystem.man().getTrade(p);
                if (t != null) {
                    int remaining = (int) Math.ceil((TradeSystem.man().getCountdownInterval() * (TradeSystem.man().getCountdownRepetitions() - t.getCountdownTicks())) / 20F);
                    if (t.getCountdown() != null) return remaining + "";
                }
                break;
            }
            case "countdown_fancy": {
                Trade t = TradeSystem.man().getTrade(p);
                if (t != null && t.getCountdown() != null) {
                    int remaining = (int) Math.ceil((TradeSystem.man().getCountdownInterval() * (TradeSystem.man().getCountdownRepetitions() - t.getCountdownTicks())) / 20F);
                    return Lang.get("Fancy_Countdown").replace("%seconds%", remaining + "");
                } else return "";
            }
            case "status":
                if (TradeSystem.man().isOffline(p)) return Lang.get("Offline");
                else return Lang.get("Online");
        }

        return null;
    }
}
