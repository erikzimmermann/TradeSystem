package de.codingair.tradesystem.spigot.trade.gui;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.shulker.ShulkerPeekGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.utils.CompatibilityUtilEvent;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TradeGUIListener implements Listener {

    @NotNull
    private Actions.Configuration getConfiguration(@NotNull Player player, @NotNull Trade trade, @NotNull Perspective perspective) {
        Actions.Configuration configuration = Actions.Configuration.DEFAULT();

        Perspective other = perspective.flip();

        configuration.targetSlots = e -> trade.getSlots();
        configuration.isItemAllowedInInventory = (items, slots) -> {
            for (ItemStack item : items) {
                if (TradeSystem.getInstance().getTradeManager().isBlocked(trade, player, trade.getPlayer(other), trade.getNames()[other.id()], trade.getUniqueId(other), item)) {
                    Lang.send(player, "Trade_Placed_Blocked_Item");
                    TradeSystem.getInstance().getTradeManager().playBlockSound(player);
                    return false;
                }
            }

            if (!TradeSystem.getInstance().getTradeManager().isDropItems() && !trade.fitsTrade(perspective, items)) {
                Lang.send(player, "Trade_Partner_No_Space");
                TradeSystem.getInstance().getTradeManager().playBlockSound(player);
                return false;
            }

            return true;
        };

        return configuration;
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        Trade trade = TradeSystem.handler().getTrade(e.getPlayer());
        if (trade != null && !TradeSystem.handler().isDropItems()) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            Trade trade = TradeSystem.handler().getTrade(player);

            if (trade != null && trade.inMainGUI(player)) {
                // Cancelling the drag event resets the cursor in a later tick.
                // Therefore, simply remove all new items added during this event.
                e.setCancelled(false);

                Perspective perspective = trade.getPerspective(player);

                // project click event on trading GUI directly to allow modifications in another listener
                boolean offerChange = Actions.projectResult(e, getConfiguration(player, trade, perspective));
                handleResult(offerChange, trade, perspective);
            }
        }
    }

    // Use the lowest priority to mark this inventory click event as canceled for other plugins if the user tries to
    // interact with a forbidden slot.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onForbiddenClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            Trade trade = TradeSystem.handler().getTrade(player);

            if (trade != null && trade.inMainGUI(player)) {
                Perspective perspective = trade.getPerspective(player);

                boolean topInventory = CompatibilityUtilEvent.getTopInventory(e).equals(e.getClickedInventory());
                boolean ownSlots = getConfiguration(player, trade, perspective).isTargetSlot(e);
                boolean forbidden = topInventory && !ownSlots;
                if (forbidden) e.setCancelled(true);
            }
        }
    }

    // Use the lowest priority to mark this inventory click event as canceled for other plugins if the user tries to
    // interact with a forbidden slot.
    @EventHandler
    public void onShulkerBoxClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            Trade trade = TradeSystem.handler().getTrade(player);

            if (trade != null && trade.inMainGUI(player)) {
                // check shulker box first because this event might already be cancelled
                Perspective perspective = trade.getPerspective(player);
                if (checkForShulkerBoxes(e, trade, player, perspective)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    // use higher priority than the GUI listener
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            Trade trade = TradeSystem.handler().getTrade(player);

            if (trade != null && trade.inMainGUI(player)) {
                // allow blocking by other plugins
                if (e.isCancelled()) return;

                // cancel everything and project changes later
                e.setCancelled(true);

                // project click event on trading GUI directly to allow modifications in another listener
                Perspective perspective = trade.getPerspective(player);
                boolean offerChange = Actions.projectResult(e, getConfiguration(player, trade, perspective));
                handleResult(offerChange, trade, perspective);
            }
        }
    }

    private void handleResult(boolean offerChange, @NotNull Trade trade, @NotNull Perspective perspective) {
        // balance items from other player before updating
        boolean cannotDropItems = !TradeSystem.getInstance().getTradeManager().isDropItems();
        if (cannotDropItems) {
            trade.cancelItemOverflow(perspective.flip());
        }

        // update trade
        if (offerChange) {
            trade.onTradeOfferChange(false);
            trade.updateLater();
        }

        // item overflow will be invoked by synchronization later
        UniversalScheduler.getScheduler(TradeSystem.getInstance()).runTask(() -> {
            // update own inventory later
            trade.synchronizePlayerInventory(perspective);
        });
    }

    private boolean checkForShulkerBoxes(@NotNull InventoryClickEvent event, @NotNull Trade trade, @NotNull Player player, @NotNull Perspective perspective) {
        boolean topInventory = CompatibilityUtilEvent.getTopInventory(event).equals(event.getClickedInventory());
        if (topInventory) {
            boolean ownSlots = trade.getSlots().contains(event.getSlot());
            if (ownSlots) {
                // shulker peeking
                if (event.getClick() == ClickType.RIGHT && Version.atLeast(11) && ShulkerPeekGUI.isShulkerBox(event.getCurrentItem())) {
                    openShulkerPeekingGUI(event, player, trade, perspective, perspective);
                    return true;
                }
            } else {
                boolean otherSlots = trade.getOtherSlots().contains(event.getSlot());
                if (otherSlots) {
                    // shulker peeking
                    if (Version.atLeast(11) && ShulkerPeekGUI.isShulkerBox(event.getCurrentItem())) {
                        openShulkerPeekingGUI(event, player, trade, perspective, perspective.flip());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void openShulkerPeekingGUI(@NotNull InventoryClickEvent e, @NotNull Player player, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Perspective owner) {
        TradingGUI tradingGUI = trade.getGUIs()[perspective.id()];
        try {
            assert e.getCurrentItem() != null;
            tradingGUI.openNestedGUI(new ShulkerPeekGUI(player, e.getCurrentItem(), owner), true, true);
        } catch (AlreadyOpenedException | NoPageException | IsWaitingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
