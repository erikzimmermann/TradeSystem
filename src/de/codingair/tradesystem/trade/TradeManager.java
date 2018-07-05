package de.codingair.tradesystem.trade;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TradeManager {
    private List<Trade> tradeList = new ArrayList<>();

    public void startTrade(Player player, Player other) {
        Trade trade = new Trade(other, player);
        this.tradeList.add(trade);
        trade.start();
    }

    public void cancelAll() {
        List<Trade> tradeList = new ArrayList<>(this.tradeList);

        for(Trade trade : tradeList) {
            trade.cancel();
        }

        tradeList.clear();
    }

    public List<Trade> getTradeList() {
        return tradeList;
    }
}
