package de.codingair.tradesystem.spigot.trade.gui;

import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.TradeLayout;
import de.codingair.tradesystem.spigot.trade.gui.layout.shulker.ShulkerPeekGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlot;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeGUIListener implements Listener {

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        Trade trade = TradeSystem.man().getTrade(e.getPlayer());
        if (trade != null && !TradeSystem.man().isDropItems()) e.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            Trade trade = TradeSystem.man().getTrade(player);

            if (trade != null && trade.inMainGUI(player)) {
                if (e.getNewItems().isEmpty()) return;

                boolean onlyLowerInventory = e.getRawSlots().stream().allMatch(i -> i >= 54);
                if (onlyLowerInventory) return;

                Player other = trade.getOther(player).orElse(null);
                String othersName = trade.getOther(player.getName());

                for (ItemStack item : e.getNewItems().values()) {
                    //check if it's blocked
                    if (TradeSystem.getInstance().getTradeManager().isBlocked(player, other, othersName, item)) {
                        e.setCancelled(true);
                        player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Placed_Blocked_Item", player));
                        return;
                    }
                }

                if (!TradeSystem.getInstance().getTradeManager().isDropItems() && !trade.fitsTrade(player, e.getNewItems().values())) {
                    player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space", player));
                    TradeSystem.getInstance().getTradeManager().playBlockSound(player);
                    e.setCancelled(true);
                } else {
                    e.setCancelled(false);
                    for (Integer rawSlot : e.getRawSlots()) {
                        if (rawSlot < 54) {
                            if (!trade.getSlots().contains(rawSlot)) {
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }

                    trade.updateLater(1);
                }
            }
        }
    }

    //use higher priority than the GUI listener
    @EventHandler (priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            Trade trade = TradeSystem.man().getTrade(player);

            if (trade != null && trade.inMainGUI(player)) {
                TradeLayout layout = trade.getLayout()[trade.getId(player)];

                if (e.getClickedInventory() == null && e.getCursor() != null) {
                    e.setCancelled(false);
                    onDrop(player, trade, e);
                } else if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                    e.setCancelled(true);
                    //do not allow
                    onCursorCollect(trade, layout, e);
                } else {
                    if (e.isCancelled()) return;  // prevent trading items that are blocked by other plugins

                    if (e.getClickedInventory() == e.getView().getBottomInventory()) {
                        onClickBottomInventory(player, trade, e);
                    } else {
                        //top inventory
                        if (trade.getSlots().contains(e.getSlot())) {
                            //own slots

                            // shulker peeking
                            if (e.getClick() == ClickType.RIGHT && Version.atLeast(11) && ShulkerPeekGUI.isShulkerBox(e.getCurrentItem())) {
                                TradingGUI tradingGUI = trade.getGUIs()[trade.getId(player)];
                                try {
                                    tradingGUI.openNestedGUI(new ShulkerPeekGUI(player, e.getCurrentItem(), e.getSlot()), true, true);
                                } catch (AlreadyOpenedException | NoPageException | IsWaitingException ex) {
                                    throw new RuntimeException(ex);
                                }
                                return;
                            }

                            e.setCancelled(false);
                            onTopInventoryClick(player, trade, e);
                        } else {
                            e.setCancelled(true);

                            boolean tradePartner = trade.getOtherSlots().contains(e.getSlot());
                            if (tradePartner) {
                                // shulker peeking
                                if (Version.atLeast(11) && ShulkerPeekGUI.isShulkerBox(e.getCurrentItem())) {
                                    TradingGUI tradingGUI = trade.getGUIs()[trade.getId(player)];
                                    try {
                                        tradingGUI.openNestedGUI(new ShulkerPeekGUI(player, e.getCurrentItem(), e.getSlot()), true, true);
                                    } catch (AlreadyOpenedException | NoPageException | IsWaitingException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void onDrop(Player player, Trade trade, InventoryClickEvent e) {
        if (!TradeSystem.getInstance().getTradeManager().isDropItems()) {
            //check for cursor
            updateWaitForPickup(trade, e, player);
        }
    }

    private void onClickBottomInventory(Player player, Trade trade, InventoryClickEvent e) {
        if (!TradeSystem.getInstance().getTradeManager().isDropItems()) {
            //check for cursor
            trade.getWaitForPickup()[trade.getId(player)] = true;
            Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
                trade.getCursor()[trade.getId(player)] = e.getCursor() != null && e.getCursor().getType() != Material.AIR;
                trade.getWaitForPickup()[trade.getId(player)] = false;
                trade.cancelItemOverflow(trade.getOtherId(player));
            }, 1);
        }

        ItemStack item = e.getCurrentItem();
        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                item != null && item.getType() != Material.AIR) {
            e.setCancelled(true);

            //check if it's blocked
            if (TradeSystem.getInstance().getTradeManager().isBlocked(player, trade.getOther(player).orElse(null), trade.getOther(player.getName()), item)) {
                player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Placed_Blocked_Item", player));
                TradeSystem.getInstance().getTradeManager().playBlockSound(player);
            } else if (!TradeSystem.getInstance().getTradeManager().isDropItems() && trade.doesNotFit(player, item)) {
                player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space", player));
                TradeSystem.getInstance().getTradeManager().playBlockSound(player);
            } else {
                //move to own slots
                List<Integer> slots = trade.getSlots();
                Inventory top = e.getView().getTopInventory();

                Integer empty = null;
                for (Integer slot : slots) {
                    ItemStack i = top.getItem(slot);
                    if (i == null || i.getType() == Material.AIR) {
                        if (empty == null) empty = slot;
                        continue;
                    }

                    int allowed = i.getMaxStackSize() - i.getAmount();
                    if (allowed > 0 && item.isSimilar(i)) {
                        if (allowed >= item.getAmount()) {
                            i.setAmount(i.getAmount() + item.getAmount());
                            item.setAmount(0);
                            e.getView().getBottomInventory().setItem(e.getSlot(), null);
                            break;
                        } else {
                            i.setAmount(i.getMaxStackSize());
                            item.setAmount(item.getAmount() - allowed);
                        }
                    }
                }

                if (empty != null && item.getAmount() > 0) {
                    top.setItem(empty, item);
                    e.getView().getBottomInventory().setItem(e.getSlot(), null);
                }

                trade.updateLater(1);
            }
        }
    }

    private void onCursorCollect(Trade trade, TradeLayout layout, InventoryClickEvent e) {
        ItemStack cursor = e.getCursor();
        assert cursor != null;

        Inventory inv = e.getView().getTopInventory();
        int startSize = cursor.getAmount();

        for (Integer slot : layout.getPattern().getSlotsOf(TradeSlot.class)) {
            if (collectSlotToCursor(inv, cursor, slot)) break;
        }

        Inventory bottom = e.getView().getBottomInventory();

        for (int slot = 0; slot < bottom.getSize(); slot++) {
            if (collectSlotToCursor(bottom, cursor, slot)) break;
        }

        if (cursor.getAmount() > startSize) {
            //update!
            trade.updateLater(1);
        }
    }

    /**
     * Collects the current item in the clicked slot to the cursor.
     *
     * @param inv    The current inventory.
     * @param cursor The current cursor.
     * @param slot   The applying slot.
     * @return true if the cursor has reached its maximum stack size.
     */
    private boolean collectSlotToCursor(Inventory inv, ItemStack cursor, Integer slot) {
        ItemStack other = inv.getItem(slot);

        if (other != null && cursor.isSimilar(other)) {
            int amount = cursor.getAmount();

            if (amount < cursor.getMaxStackSize()) {
                applyAmount(inv, cursor, other, slot, amount);
            } else return true;
        }
        return false;
    }

    /**
     * Moves the given amount from 'other' to 'cursor'.
     *
     * @param bottom The bottom inventory.
     * @param cursor The current cursor.
     * @param other  The other ItemStack which should be collected to the cursor.
     * @param slot   The clicked slot.
     * @param amount The amount to collect.
     */
    private void applyAmount(Inventory bottom, ItemStack cursor, ItemStack other, int slot, int amount) {
        int a = cursor.getMaxStackSize() - amount;

        if (other.getAmount() > a) {
            other.setAmount(other.getAmount() - a);
            cursor.setAmount(cursor.getMaxStackSize());
        } else {
            cursor.setAmount(cursor.getAmount() + other.getAmount());
            bottom.setItem(slot, new ItemStack(Material.AIR));
        }
    }

    private void onTopInventoryClick(Player player, Trade trade, InventoryClickEvent e) {
        //cancel faster --> fix dupe glitch
        if (e.getView().getTopInventory().equals(e.getClickedInventory()) && trade.getSlots().contains(e.getSlot()) && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
            trade.onTradeOfferChange(true);
        }

        if (e.getClick().name().equals("SWAP_OFFHAND")) {
            if (e.getView().getTopInventory().equals(e.getClickedInventory())) {
                e.setCancelled(true);
                return;
            }
        }

        //check if it's blocked
        ItemStack blockedItem = null;
        switch (e.getAction().name()) {
            case "SWAP_WITH_CURSOR":
            case "PLACE_ALL":
            case "PLACE_ONE": {
                //check cursor
                blockedItem = e.getCursor();
                break;
            }

            case "MOVE_TO_OTHER_INVENTORY": {
                //check current
                blockedItem = e.getCurrentItem();
                break;
            }

            case "HOTBAR_SWAP":
            case "HOTBAR_MOVE_AND_READD": {
                //check hotbar
                blockedItem = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                break;
            }
        }

        if (blockedItem != null && TradeSystem.getInstance().getTradeManager().isBlocked(player, trade.getOther(player).orElse(null), trade.getOther(player.getName()), blockedItem)) {
            e.setCancelled(true);
            player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Placed_Blocked_Item", player));
            TradeSystem.getInstance().getTradeManager().playBlockSound(player);
            return;
        }

        boolean fits = true;
        if (!TradeSystem.getInstance().getTradeManager().isDropItems()) {
            //check for cursor
            updateWaitForPickup(trade, e, player);

            if (!e.isCancelled()) {
                //check if fits
                switch (e.getAction().name()) {
                    case "PLACE_ONE": {
                        ItemStack item = e.getCurrentItem();
                        List<Integer> remove = new ArrayList<>();

                        if (item != null && item.getType() != Material.AIR) {
                            item = item.clone();
                            item.setAmount(item.getAmount() + 1);
                            remove.add(e.getSlot());
                        } else {
                            assert e.getCursor() != null;
                            item = e.getCursor().clone();
                            item.setAmount(1);
                        }

                        if (trade.doesNotFit(player, remove, item)) fits = false;
                        break;
                    }

                    case "PLACE_SOME": {
                        assert e.getCurrentItem() != null;
                        ItemStack item = e.getCurrentItem().clone();
                        item.setAmount(item.getMaxStackSize());

                        List<Integer> remove = new ArrayList<>();
                        remove.add(e.getSlot());

                        if (trade.doesNotFit(player, remove, item)) fits = false;
                        break;
                    }

                    case "PLACE_ALL":
                        assert e.getCursor() != null;
                        if (trade.doesNotFit(player, e.getCursor().clone())) fits = false;
                        break;

                    case "HOTBAR_SWAP": {
                        ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                        if (item != null && trade.doesNotFit(player, item.clone())) fits = false;
                        break;
                    }

                    case "HOTBAR_MOVE_AND_READD": {
                        ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                        List<Integer> remove = new ArrayList<>();
                        remove.add(e.getSlot());

                        if (item != null && trade.doesNotFit(player, remove, item.clone())) fits = false;
                        else {
                            e.setCancelled(true);

                            ItemStack current = e.getView().getTopInventory().getItem(e.getSlot());
                            assert current != null;
                            ItemStack top = current.clone();

                            current = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                            assert current != null;
                            ItemStack bottom = current.clone();

                            e.getView().getTopInventory().setItem(e.getSlot(), bottom);
                            e.getView().getBottomInventory().setItem(e.getHotbarButton(), top);
                        }
                        break;
                    }

                    case "SWAP_WITH_CURSOR": {
                        List<Integer> remove = new ArrayList<>();
                        remove.add(e.getSlot());

                        assert e.getCursor() != null;
                        if (trade.doesNotFit(player, remove, e.getCursor().clone())) fits = false;
                        break;
                    }

                    case "DROP_ALL_CURSOR":
                    case "DROP_ALL_SLOT":
                    case "DROP_ONE_CURSOR":
                    case "DROP_ONE_SLOT":
                        assert e.getCurrentItem() != null;
                        if (trade.doesNotFit(player, e.getCurrentItem().clone())) fits = false;
                        break;
                }
            }
        }

        if (!fits) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space", player));
            TradeSystem.getInstance().getTradeManager().playBlockSound(player);
            e.setCancelled(true);
        } else trade.updateLater(1);
    }

    private void updateWaitForPickup(Trade trade, InventoryClickEvent e, Player player) {
        trade.getWaitForPickup()[trade.getId(player)] = true;
        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
            trade.getCursor()[trade.getId(player)] = e.getCursor() != null && e.getCursor().getType() != Material.AIR;
            trade.getWaitForPickup()[trade.getId(player)] = false;
        }, 1);
    }
}
