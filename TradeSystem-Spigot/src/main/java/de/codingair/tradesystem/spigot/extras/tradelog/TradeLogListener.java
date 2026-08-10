package de.codingair.tradesystem.spigot.extras.tradelog;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class TradeLogListener implements Listener {

    @EventHandler
    public void onTradeFinish(AsyncPlayerPreLoginEvent e) {
        UniversalScheduler.getScheduler(TradeSystem.getInstance()).runTaskAsynchronously(() ->{
            TradeLogService.registerOrUpdatePlayer(e.getUniqueId(), e.getName());
        });
    }
}
