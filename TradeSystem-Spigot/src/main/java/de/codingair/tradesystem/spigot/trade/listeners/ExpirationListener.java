package de.codingair.tradesystem.spigot.trade.listeners;

import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ExpirationListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        TradeSystem.invitations().cancelAll(e.getPlayer());
    }

}
