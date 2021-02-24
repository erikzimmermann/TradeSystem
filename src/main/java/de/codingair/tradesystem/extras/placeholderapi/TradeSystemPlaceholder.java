package de.codingair.tradesystem.extras.placeholderapi;

import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.Trade;
import de.codingair.tradesystem.utils.Lang;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class TradeSystemPlaceholder extends PlaceholderExpansion {
    @Override
    public String getIdentifier() {
        return "tradesystem";
    }

    @Override
    public String getAuthor() {
        return "CodingAir";
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public String onPlaceholderRequest(Player p, String id) {
        switch (id.toLowerCase()) {
            case "trade_partner": {
                Trade t = TradeSystem.man().getTrade(p);
                if (t != null) return t.getOther(p).getDisplayName();
                break;
            }
            case "countdown": {
                Trade t = TradeSystem.man().getTrade(p);
                int remaining = (int) Math.ceil((TradeSystem.man().getCountdownInterval() * (TradeSystem.man().getCountdownRepetitions() - t.getCountdownTicks())) / 20);
                if (t != null && t.getCountdown() != null) return remaining + "";
                break;
            }
            case "countdown_fancy": {
                Trade t = TradeSystem.man().getTrade(p);
                if (t != null && t.getCountdown() != null) {
                    int remaining = (int) Math.ceil((TradeSystem.man().getCountdownInterval() * (TradeSystem.man().getCountdownRepetitions() - t.getCountdownTicks())) / 20);
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
