package de.codingair.tradesystem.utils.updates;

import de.codingair.tradesystem.TradeSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NotifyListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        TradeSystem.getInstance().notifyPlayers(p);
    }

}
