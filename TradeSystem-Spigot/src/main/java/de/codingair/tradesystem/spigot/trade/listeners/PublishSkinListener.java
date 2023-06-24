package de.codingair.tradesystem.spigot.trade.listeners;

import de.codingair.codingapi.player.data.GameProfileUtils;
import de.codingair.tradesystem.proxy.packets.PublishSkinPacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PublishSkinListener implements Listener {
    public PublishSkinListener() {
        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(PublishSkinListener::sync), 5L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        sync(e.getPlayer());
    }

    private static void sync(@NotNull Player player) {
        String skin = GameProfileUtils.extractSkinId(GameProfileUtils.getGameProfile(player));
        TradeSystem.proxyHandler().send(new PublishSkinPacket(player.getName(), skin), player);
    }

}
