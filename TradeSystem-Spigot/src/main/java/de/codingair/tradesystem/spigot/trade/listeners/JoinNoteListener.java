package de.codingair.tradesystem.spigot.trade.listeners;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.Permissions;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinNoteListener implements Listener {
    private static TextComponent note = null;

    public static void applyNote(TextComponent tc) {
        if (note == null) note = tc;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (note != null && e.getPlayer().hasPermission(Permissions.PERMISSION_NOTIFY)) {
            TradeSystem.getInstance().getScheduler().runTaskLaterAtEntity(e.getPlayer(), () -> e.getPlayer().spigot().sendMessage(note), 20 * 5);
        }
    }
}
