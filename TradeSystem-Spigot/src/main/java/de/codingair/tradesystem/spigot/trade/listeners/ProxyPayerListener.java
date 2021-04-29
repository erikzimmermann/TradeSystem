package de.codingair.tradesystem.spigot.trade.listeners;

import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProxyPayerListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (Bukkit.getOnlinePlayers().size() <= 1) {
            //remove all proxy players since we cannot synchronize without a single online player
            TradeSystem.proxy().clearPlayers();
        }
    }

}
