package de.codingair.tradesystem.extras.placeholderapi;

import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.Trade;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class TradePartnerPlaceholder extends PlaceholderExpansion {
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
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player p, String id) {
        if(id.equals("trade_partner")) {
            Trade t = TradeSystem.getInstance().getTradeManager().getTrade(p);
            if(t != null) return t.getOther(p).getDisplayName();
        }

        return null;
    }
}
