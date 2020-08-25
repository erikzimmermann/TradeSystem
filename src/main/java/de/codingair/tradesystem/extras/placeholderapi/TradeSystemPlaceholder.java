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
        if(id.equals("trade_partner")) {
            Trade t = TradeSystem.getInstance().getTradeManager().getTrade(p);
            if(t != null) return t.getOther(p).getDisplayName();
        } else if(id.equals("status")) {
            if(TradeSystem.getInstance().getTradeManager().isOffline(p)) return Lang.get("Offline");
            else return Lang.get("Online");
        }

        return null;
    }
}
