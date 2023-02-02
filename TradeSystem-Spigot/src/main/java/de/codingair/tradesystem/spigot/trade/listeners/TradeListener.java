package de.codingair.tradesystem.spigot.trade.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.codingapi.player.chat.ChatButtonListener;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.managers.RequestManager;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TradeListener implements Listener, ChatButtonListener {
    private final Cache<UUID, Boolean> players = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    @Override
    public boolean onAsyncClick(Player player, UUID id, String type) {
        if (type != null && type.equals("TRADE_TOGGLE")) {
            player.performCommand("trade toggle");
            return true;
        }

        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        TradeSystem.man().join(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        TradeSystem.man().quit(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (!TradeSystem.getInstance().getTradeManager().isRequestOnShiftRightClick() || players.getIfPresent(e.getPlayer().getUniqueId()) != null || !e.getPlayer().isSneaking()) return;

        if (e.getRightClicked() instanceof Player) {
            Player p = e.getPlayer();
            Player other = (Player) e.getRightClicked();

            if (!other.isOnline()) return; //npc
            if (!p.canSee(other)) return;

            players.put(p.getUniqueId(), false);
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
        Trade t = TradeSystem.man().getTrade(p);

        if (t != null) {
            if (!TradeSystem.man().isDropItems()) {
                //does it fit?
                if (t.doesNotFit(p, e.getItem().getItemStack())) e.setCancelled(true);
            }
        }
    }

}
