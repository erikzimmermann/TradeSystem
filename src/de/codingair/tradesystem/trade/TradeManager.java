package de.codingair.tradesystem.trade;

import de.codingair.tradesystem.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TradeManager {
    private List<Trade> tradeList = new ArrayList<>();
    private int cooldown = 60;

    public void load() {
        FileConfiguration config = TradeSystem.getInstance().getFileManager().getFile("Config").getConfig();

        cooldown = config.getInt("TradeSystem.Request_Cooldown_In_Sek", 60);
    }

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

    public int getCooldown() {
        return cooldown;
    }
}
