package de.codingair.tradesystem.spigot.trade.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class AntiGUIDupeListener implements Listener {
    private static final long MIN_GUI_DELAY_DIFFERENCE = 200;
    private static final Cache<String, Long> LAST_GUI = CacheBuilder.newBuilder().expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

    private static Long lastGUI(@NotNull Player player) {
        return LAST_GUI.getIfPresent(player.getName());
    }

    public static boolean isNotAllowed(@NotNull Player player) {
        Long last = lastGUI(player);
        if (last == null) return false;

        long diff = System.currentTimeMillis() - last;
        return diff < MIN_GUI_DELAY_DIFFERENCE;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player) {
            Player player = (Player) e.getPlayer();

            Trade trade = TradeSystem.man().getTrade(player);
            if (trade != null) {
                int id = trade.getId(player);
                TradingGUI gui = trade.getGUIs()[id];

                if (gui != null) {
                    if (e.getView().getType() == InventoryType.CHEST) {
                        boolean sameInventory = e.getView().getTopInventory().equals(gui.getInventory());
                        if (sameInventory) return;
                    } else {
                        boolean currentlyInAnotherGUI = gui.isWaiting();
                        if (currentlyInAnotherGUI) return;
                    }
                }

                trade.cancelDueToGUIError();
            }

            LAST_GUI.put(player.getName(), System.currentTimeMillis());
        }
    }
}
