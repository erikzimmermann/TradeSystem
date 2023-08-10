package de.codingair.tradesystem.spigot.trade.listeners;

import de.codingair.codingapi.player.chat.ChatButtonListener;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.trade.managers.RequestManager;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeListener implements Listener, ChatButtonListener {
    private static final long BUFFER_TIME = 1000;
    private final Map<String, Long> invitationBuffer = new HashMap<>();

    @Override
    public boolean onAsyncClick(Player player, UUID id, String type) {
        if (type != null && type.equals("TRADE_TOGGLE")) {
            player.performCommand("trade toggle");
            return true;
        }

        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        TradeSystem.handler().quit(e.getPlayer());
    }

    private boolean isBuffered(@NotNull Player player) {
        long time = System.currentTimeMillis();
        Long last = invitationBuffer.put(player.getName(), time);
        return last != null && time - last <= BUFFER_TIME;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (!TradeSystem.getInstance().getTradeManager().isRequestOnShiftRightClick() || !e.getPlayer().isSneaking()) return;

        if (e.getRightClicked() instanceof Player) {
            if (isBuffered(e.getPlayer())) return;

            Player p = e.getPlayer();
            Player other = (Player) e.getRightClicked();

            if (!other.isOnline()) return; //npc
            if (!p.canSee(other)) return;

            RequestManager.request(p, other);
        }
    }

    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            Trade trade = TradeSystem.getInstance().getTradeManager().getTrade(player);

            if (trade != null) {
                double finalDamage = e.getFinalDamage();
                if ((TradeSystem.getInstance().getTradeManager().isCancelOnDamage() && finalDamage > 0) || (player.getHealth() - e.getFinalDamage() <= 0))
                    trade.cancel(Lang.getPrefix() + Lang.get("Trade_cancelled_by_attack", player));
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        Trade t = TradeSystem.handler().getTrade(p);

        if (t != null) {
            Perspective perspective = t.getPerspective(p);
            if (!TradeSystem.handler().isDropItems()) {
                //does it fit?
                if (t.doesNotFit(perspective, e.getItem().getItemStack())) e.setCancelled(true);
            }
        }
    }

}
