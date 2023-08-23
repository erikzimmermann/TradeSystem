package de.codingair.tradesystem.spigot.transfer;

import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ProxyDataListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        TradeSystem.proxy().checkForServerName();
    }

}
