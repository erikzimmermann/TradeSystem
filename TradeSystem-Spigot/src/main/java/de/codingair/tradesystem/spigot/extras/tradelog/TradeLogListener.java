package de.codingair.tradesystem.spigot.extras.tradelog;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class TradeLogListener implements Listener {

    @EventHandler
    public void onTradeFinish(AsyncPlayerPreLoginEvent e) {
        TradeLogService.registerOrUpdatePlayer(e.getUniqueId(), e.getName());
    }
}
