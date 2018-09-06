package de.codingair.tradesystem.trade;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TradeManager {
    private List<Trade> tradeList = new ArrayList<>();
    private int cooldown = 60;
    private int distance = 50;

    private boolean requestOnRightclick = false;
    private boolean shiftclick = false;

    public void load() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();

        boolean save = false;
        cooldown = config.getInt("TradeSystem.Request_Cooldown_In_Sek", 60);
        if(cooldown <= 10) {
            config.set("TradeSystem.Request_Cooldown_In_Sek", 10);
            save = true;
        }

        if(config.getBoolean("TradeSystem.Trade_Distance.enabled", true)) {
            this.distance = config.getInt("TradeSystem.Trade_Distance.distance_in_blocks", 50);
            if(this.distance < 1) {
                config.set("TradeSystem.Trade.distance_in_blocks", 1);
                save = true;
            }
        } else this.distance = -1;

        this.requestOnRightclick = config.getBoolean("TradeSystem.Action_To_Request.Rightclick", false);
        this.shiftclick = config.getBoolean("TradeSystem.Action_To_Request.Shiftclick", true);

        if(save) file.saveConfig();
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

        TradeSystem.getInstance().getTradeCMD().getInvites().clear();
    }

    public List<Trade> getTradeList() {
        return tradeList;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isRequestOnRightclick() {
        return requestOnRightclick;
    }

    public boolean isShiftclick() {
        return shiftclick;
    }
}
