package de.codingair.tradesystem.spigot.trade.gui;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is used for applying inventory interactions directly to the inventory without waiting to the end of
 * the event. This allows other plugins do interact with changes within the interact events without overwriting any
 * actions.
 */
public class Actions {
    /**
     * The configuration for all available projections.
     */
    public static class Configuration {
        /**
         * Maps the original slot to a slot of the given inventory.
         */
        public @NotNull Function<Integer, Integer> slotMapper;
        /**
         * Function to return target slots. The player can only interact with items in these slots. This slot order is
         * also the order of collecting items to the cursor in case of
         * {@link org.bukkit.event.inventory.InventoryAction#COLLECT_TO_CURSOR COLLECT_TO_CURSOR}.
         */
        public @NotNull Function<InventoryInteractEvent, List<Integer>> targetSlots;
        /**
         * Maps the {@link InventoryInteractEvent} and the original slot to the inventory that should be used to
         * project any changes to.
         */
        public @NotNull BiFunction<InventoryInteractEvent, Integer, Inventory> inventoryMapper;
        /**
         * Used for {@link org.bukkit.event.inventory.InventoryAction#COLLECT_TO_CURSOR COLLECT_TO_CURSOR} to make sure
         * if it is allowed to collect items from both inventories.
         */
        public boolean collectFromBothInventories;
        /**
         * Used to check if added items are allowed in the top inventory. This will not be applied to
         * {@link org.bukkit.event.inventory.InventoryAction#PLACE_ALL PLACE_ALL},
         * {@link org.bukkit.event.inventory.InventoryAction#PLACE_SOME PLACE_SOME} and
         * {@link org.bukkit.event.inventory.InventoryAction#PLACE_ONE PLACE_ONE}.
         */
        public @NotNull BiFunction<List<ItemStack>, List<Integer>, Boolean> isItemAllowedInInventory;

        public Configuration(@NotNull Function<Integer, Integer> slotMapper, @NotNull Function<InventoryInteractEvent, List<Integer>> targetSlots, @NotNull BiFunction<InventoryInteractEvent, Integer, Inventory> inventoryMapper, boolean collectFromBothInventories, @NotNull BiFunction<List<ItemStack>, List<Integer>, Boolean> isItemAllowedInInventory) {
            this.slotMapper = slotMapper;
            this.targetSlots = targetSlots;
            this.inventoryMapper = inventoryMapper;
            this.collectFromBothInventories = collectFromBothInventories;
            this.isItemAllowedInInventory = isItemAllowedInInventory;
        }

        @NotNull
        public static Configuration DEFAULT() {
            return new Configuration(
                    Function.identity(),
                    e -> IntStream.range(0, e.getView().getTopInventory().getSize()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll),
                    (e, slot) -> e.getView().getTopInventory(),
                    true,
                    (item, slot) -> true
            );
        }
    }

    /**
     * Due to API limitations, this method does not project any changes the inventory within this event.
     * Instead, it only checks the configuration if the changes made by the given player during this event are valid.
     *
     * @param event         The {@link InventoryDragEvent} to project.
     * @param configuration The {@link Configuration} to use.
     * @return Whether the top inventory was changed.
     */
    public static boolean projectResult(@NotNull InventoryDragEvent event, @NotNull Configuration configuration) {
        int topSize = event.getView().getTopInventory().getSize();

        boolean onlyInBottom = event.getRawSlots().stream().allMatch(i -> i >= topSize);
        if (onlyInBottom) return false;

        // check target slots before blacklisted items
        List<Integer> targetSlots = configuration.targetSlots.apply(event);

        // in bottom inventory or in target slot
        boolean invalidSlot = event.getRawSlots().stream().anyMatch(i -> i < topSize && !targetSlots.contains(i));
        if (invalidSlot) {
            // make sure to cancel the drag event since this is not done before
            event.setCancelled(true);
            return false;
        }

        // check blacklisted items
        List<Integer> slots = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> e : event.getNewItems().entrySet()) {
            if (e.getKey() >= topSize) continue;

            slots.add(e.getKey());
            items.add(e.getValue());
        }
        if (!configuration.isItemAllowedInInventory.apply(items, slots)) {
            // make sure to cancel the drag event since this is not done before
            event.setCancelled(true);
            return false;
        }

        // add items to the inventory
        for (Map.Entry<Integer, ItemStack> e : event.getNewItems().entrySet()) {
            Inventory inventory = configuration.inventoryMapper.apply(event, e.getKey());

            // only set item if it is not the top inventory
            if (inventory.equals(event.getView().getTopInventory())) continue;

            inventory.setItem(configuration.slotMapper.apply(e.getKey() % topSize), e.getValue());
        }

        // Bukkit does not allow to cancel the event and to make changes to the inventory at the same time.
        // Therefore, just go ahead and use bukkit.
        return event.getRawSlots().stream().anyMatch(i -> i < topSize);
    }

    /**
     * Projects the result of the given {@link InventoryClickEvent} to the inventory. When the top inventory equals
     * the inventory of the {@link InventoryClickEvent} the event must be cancelled manually.
     *
     * @param event         The {@link InventoryClickEvent} to project.
     * @param configuration The {@link Configuration} to use.
     * @return Whether the top inventory was changed.
     */
    public static boolean projectResult(@NotNull InventoryClickEvent event, @NotNull Configuration configuration) {
        int slot = configuration.slotMapper.apply(event.getSlot());
        Inventory inventory = configuration.inventoryMapper.apply(event, event.getSlot());

        switch (event.getAction()) {
            case NOTHING:
            case UNKNOWN:
                break;

            case PICKUP_ONE:
            case PICKUP_HALF:
            case PICKUP_SOME:
            case PICKUP_ALL:
                return handlePickUp(event, inventory, slot, configuration);

            case PLACE_ONE:
            case PLACE_SOME:
            case PLACE_ALL:
                return handlePlace(event, inventory, slot, configuration);

            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
                return handleDrop(event, inventory, slot, configuration);

            case COLLECT_TO_CURSOR:
                return handleCollect(event, inventory, configuration);

            case HOTBAR_MOVE_AND_READD:
            case HOTBAR_SWAP:
                return handleSwap(event, inventory, slot, configuration);

            case MOVE_TO_OTHER_INVENTORY:
                return handleMove(event, inventory, slot, configuration);

            case CLONE_STACK:
                handleClone(event, inventory, slot);
                break;

            case SWAP_WITH_CURSOR:
                return handleExchange(event, inventory, slot, configuration);
        }

        return false;
    }

    private static boolean handlePickUp(@NotNull InventoryClickEvent event, @NotNull Inventory inventory, int slot, @NotNull Configuration configuration) {
        boolean topInventory = event.getView().getTopInventory().equals(event.getClickedInventory());
        if (topInventory && !configuration.targetSlots.apply(event).contains(event.getRawSlot())) return false;

        ItemStack currentItem = topInventory ? inventory.getItem(slot) : event.getCurrentItem();
        boolean changed = false;

        switch (event.getAction()) {
            case PICKUP_ALL:
            case PICKUP_SOME: {
                if (nullOrAir(currentItem)) break;
                ItemStack cursor = event.getCursor();

                if (nullOrAir(cursor)) {
                    event.getView().setCursor(currentItem);
                    if (topInventory) {
                        inventory.setItem(slot, null);
                        changed = true;
                    } else event.setCurrentItem(null);
                } else {
                    // pickup until cursor is full
                    if (cursor.isSimilar(currentItem)) {
                        if (place(cursor, currentItem, cursor.getMaxStackSize() - cursor.getAmount())) {
                            if (currentItem.getAmount() == 0) {
                                if (topInventory) {
                                    inventory.setItem(slot, null);
                                    changed = true;
                                } else event.setCurrentItem(null);
                            }
                        }
                    }
                }

                break;
            }

            case PICKUP_HALF: {
                if (nullOrAir(currentItem)) break;
                ItemStack copy = currentItem.clone();
                copy.setAmount((int) Math.ceil(copy.getAmount() / 2F));
                currentItem.setAmount(currentItem.getAmount() - copy.getAmount());

                event.getView().setCursor(copy);
                changed = topInventory;
                break;
            }

            case PICKUP_ONE: {
                if (nullOrAir(currentItem)) break;

                if (currentItem.getAmount() == 1) {
                    event.getView().setCursor(currentItem);
                    if (topInventory) {
                        inventory.setItem(slot, null);
                        changed = true;
                    } else event.setCurrentItem(null);
                } else {
                    ItemStack copy = currentItem.clone();
                    copy.setAmount(1);

                    event.getView().setCursor(copy);
                    currentItem.setAmount(currentItem.getAmount() - 1);
                    changed = topInventory;
                }

                break;
            }
        }

        return changed;
    }

    private static boolean handlePlace(@NotNull InventoryClickEvent event, @NotNull Inventory inventory, int slot, @NotNull Configuration configuration) {
        boolean topInventory = event.getView().getTopInventory().equals(event.getClickedInventory());
        if (topInventory && !configuration.targetSlots.apply(event).contains(event.getRawSlot())) return false;

        ItemStack currentItem = topInventory ? inventory.getItem(slot) : event.getCurrentItem();
        boolean changed = false;

        switch (event.getAction()) {
            case PLACE_SOME:
            case PLACE_ALL: {
                ItemStack cursor = event.getView().getCursor();
                if (nullOrAir(cursor)) break;

                if (topInventory && !configuration.isItemAllowedInInventory.apply(Collections.singletonList(cursor), Collections.singletonList(event.getSlot()))) break;

                if (currentItem != null) {
                    if (cursor.isSimilar(currentItem)) {
                        if (place(currentItem, cursor, cursor.getAmount())) {
                            if (cursor.getAmount() == 0) event.getView().setCursor(null);
                            changed = topInventory;
                        }
                    }
                } else {
                    if (topInventory) {
                        inventory.setItem(slot, cursor);
                        changed = true;
                    } else event.setCurrentItem(cursor);
                    event.getView().setCursor(null);
                }

                break;
            }

            case PLACE_ONE: {
                ItemStack cursor = event.getView().getCursor();
                if (nullOrAir(cursor)) break;

                if (topInventory && !configuration.isItemAllowedInInventory.apply(Collections.singletonList(cursor), Collections.singletonList(event.getSlot()))) break;

                if (currentItem != null) {
                    if (cursor.isSimilar(currentItem)) {
                        if (place(currentItem, cursor, 1)) {
                            if (cursor.getAmount() == 0) event.getView().setCursor(null);
                            changed = topInventory;
                        }
                    }
                } else {
                    ItemStack copy = cursor.clone();

                    cursor.setAmount(cursor.getAmount() - 1);
                    copy.setAmount(1);

                    if (cursor.getAmount() == 0) event.getView().setCursor(null);

                    if (topInventory) {
                        inventory.setItem(slot, copy);
                        changed = true;
                    } else event.setCurrentItem(copy);
                }

                break;
            }
        }

        return changed;
    }

    private static boolean handleDrop(@NotNull InventoryClickEvent event, @NotNull Inventory inventory, int slot, @NotNull Configuration configuration) {
        boolean topInventory = event.getView().getTopInventory().equals(event.getClickedInventory());
        if (topInventory && !configuration.targetSlots.apply(event).contains(event.getRawSlot())) return false;

        ItemStack currentItem = topInventory ? inventory.getItem(slot) : event.getCurrentItem();
        boolean changed = false;

        switch (event.getAction()) {
            case DROP_ALL_CURSOR: {
                ItemStack item = event.getCursor();
                if (nullOrAir(item)) break;

                event.getView().setCursor(null);
                dropItem((Player) event.getWhoClicked(), item);
                break;
            }

            case DROP_ONE_CURSOR: {
                ItemStack item = event.getCursor();
                if (nullOrAir(item)) break;

                ItemStack copy = item.clone();
                copy.setAmount(1);

                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() == 0) event.getView().setCursor(null);

                dropItem((Player) event.getWhoClicked(), copy);
                break;
            }

            case DROP_ALL_SLOT: {
                if (nullOrAir(currentItem)) break;

                if (topInventory) {
                    inventory.setItem(slot, null);
                    changed = true;
                } else event.setCurrentItem(null);

                dropItem((Player) event.getWhoClicked(), currentItem);
                break;
            }

            case DROP_ONE_SLOT: {
                if (nullOrAir(currentItem)) break;

                ItemStack copy = currentItem.clone();
                copy.setAmount(1);

                currentItem.setAmount(currentItem.getAmount() - 1);
                if (currentItem.getAmount() == 0) {
                    if (topInventory) {
                        inventory.setItem(slot, null);
                        changed = true;
                    } else event.setCurrentItem(null);
                }

                dropItem((Player) event.getWhoClicked(), copy);
                break;
            }
        }

        return changed;
    }

    private static boolean handleCollect(@NotNull InventoryClickEvent event, @NotNull Inventory inventory, @NotNull Configuration configuration) {
        if (nullOrAir(event.getCursor())) return false;

        ItemStack cursor = event.getCursor();

        List<Integer> target = new ArrayList<>(configuration.targetSlots.apply(event));
        sortByAmount(cursor, target, inventory);
        boolean changed = false;

        boolean topInventory = event.getView().getTopInventory().equals(event.getClickedInventory());
        if (topInventory || configuration.collectFromBothInventories) {
            for (int slot : target) {
                if (collectTo(inventory, configuration.slotMapper.apply(slot), cursor)) break;
                changed = true;
            }
        }

        boolean bottomInventory = event.getView().getBottomInventory().equals(event.getClickedInventory());
        if (bottomInventory || configuration.collectFromBothInventories) {
            Inventory bottom = event.getView().getBottomInventory();

            target = IntStream
                    .range(0, bottom.getSize())
                    .boxed()
                    .collect(Collectors.toList());
            sortByAmount(cursor, target, bottom);

            for (int slot : target) {
                if (collectTo(bottom, slot, cursor)) break;
            }
        }

        return changed;
    }

    private static void sortByAmount(@NotNull ItemStack cursor, @NotNull List<Integer> target, @NotNull Inventory bottom) {
        target.sort((slot1, slot2) -> {
            ItemStack item1 = bottom.getItem(slot1);
            ItemStack item2 = bottom.getItem(slot2);

            int amount1 = item1 != null && item1.isSimilar(cursor) ? item1.getAmount() : 0;
            int amount2 = item2 != null && item2.isSimilar(cursor) ? item2.getAmount() : 0;

            return Integer.compare(amount1, amount2);
        });
    }

    private static boolean handleSwap(@NotNull InventoryClickEvent event, @NotNull Inventory inventory, int slot, @NotNull Configuration configuration) {
        boolean topInventory = event.getView().getTopInventory().equals(event.getClickedInventory());
        if (topInventory && !configuration.targetSlots.apply(event).contains(event.getRawSlot())) return false;

        ItemStack currentItem = topInventory ? inventory.getItem(slot) : event.getCurrentItem();
        int switchTo = event.getHotbarButton();
        ItemStack switched = event.getView().getBottomInventory().getItem(switchTo);
        boolean changed = false;

        if (topInventory && switched != null && !configuration.isItemAllowedInInventory.apply(Collections.singletonList(switched), Collections.singletonList(event.getSlot())))
            return false;
        if (Objects.equals(currentItem, switched)) return false;

        if (topInventory) {
            inventory.setItem(slot, switched);
            changed = true;
        } else event.setCurrentItem(switched);
        event.getView().getBottomInventory().setItem(switchTo, currentItem);

        return changed;
    }

    private static boolean handleMove(@NotNull InventoryClickEvent event, @NotNull Inventory inventory, int slot, @NotNull Configuration configuration) {
        boolean topInventory = event.getView().getTopInventory().equals(event.getClickedInventory());
        if (topInventory && !configuration.targetSlots.apply(event).contains(event.getRawSlot())) return false;

        ItemStack clickedItem = topInventory ? inventory.getItem(slot) : event.getCurrentItem();
        if (nullOrAir(clickedItem)) return false;

        if (!topInventory && !configuration.isItemAllowedInInventory.apply(Collections.singletonList(clickedItem), Collections.singletonList(event.getRawSlot())))
            return false;
        boolean changed = false;

        if (topInventory) {
            int amountBefore = clickedItem.getAmount();
            ItemStack left = event.getView().getBottomInventory().addItem(clickedItem).values().stream().findAny().orElse(null);
            if (nullOrAir(left)) {
                inventory.setItem(slot, null);
                changed = true;
            } else if (left.getAmount() != amountBefore)
                changed = true;
        } else {
            List<Integer> targetSlots = configuration.targetSlots.apply(event);
            int freeSlot = -1;

            for (int currentSlot : targetSlots) {
                currentSlot = configuration.slotMapper.apply(currentSlot);
                ItemStack current = inventory.getItem(currentSlot);
                if (nullOrAir(current) && freeSlot == -1) freeSlot = currentSlot;
                if (nullOrAir(current) || !current.isSimilar(clickedItem)) continue;

                changed = true;

                if (transferAmount(current, clickedItem)) {
                    event.setCurrentItem(null);
                    return changed;
                }
            }

            if (freeSlot >= 0) {
                inventory.setItem(freeSlot, clickedItem);
                event.setCurrentItem(null);

                changed = true;
            } else if (clickedItem.getAmount() <= 0)
                event.setCurrentItem(null);
        }

        return changed;
    }

    private static void handleClone(@NotNull InventoryClickEvent event, @NotNull Inventory inventory, int slot) {
        if (event.getWhoClicked().getGameMode() != GameMode.CREATIVE && event.getWhoClicked().getGameMode() != GameMode.SPECTATOR)
            return;

        boolean topInventory = event.getView().getTopInventory().equals(event.getClickedInventory());
        ItemStack clickedItem = topInventory ? inventory.getItem(slot) : event.getCurrentItem();

        if (nullOrAir(clickedItem)) return;
        ItemStack copy = clickedItem.clone();
        copy.setAmount(copy.getMaxStackSize());
        event.getView().setCursor(copy);
    }

    private static boolean handleExchange(@NotNull InventoryClickEvent event, @NotNull Inventory inventory, int slot, @NotNull Configuration configuration) {
        boolean topInventory = event.getView().getTopInventory().equals(event.getClickedInventory());
        if (topInventory && !configuration.targetSlots.apply(event).contains(event.getRawSlot())) return false;

        ItemStack clickedItem = topInventory ? inventory.getItem(slot) : event.getCurrentItem();
        boolean changed = false;

        ItemStack cursor = event.getCursor();
        if (cursor != null && topInventory && !configuration.isItemAllowedInInventory.apply(Collections.singletonList(cursor), Collections.singletonList(event.getSlot())))
            return false;

        if (topInventory) {
            inventory.setItem(slot, cursor);
            changed = true;
        } else event.setCurrentItem(cursor);
        event.getView().setCursor(clickedItem);

        return changed;
    }


    /**
     * Tries to collect the item in the given slot to the given {@link ItemStack}. Checks for similarity first.
     *
     * @param inventory The {@link Inventory} to collect from.
     * @param slot      The slot to collect from.
     * @param to        The {@link ItemStack} to collect to.
     * @return Whether the item 'to' was fully merged.
     */
    private static boolean collectTo(@NotNull Inventory inventory, int slot, @NotNull ItemStack to) {
        ItemStack from = inventory.getItem(slot);

        if (from != null && to.isSimilar(from)) {
            int amount = to.getAmount();

            if (amount < to.getMaxStackSize()) {
                if (transferAmount(to, from)) inventory.setItem(slot, null);
            } else return true;
        }
        return false;
    }

    /**
     * @param to   The {@link ItemStack} to transfer to.
     * @param from The {@link ItemStack} to transfer from.
     * @return Whether the item 'from' was fully merged.
     */
    private static boolean transferAmount(@NotNull ItemStack to, @NotNull ItemStack from) {
        int space = to.getMaxStackSize() - to.getAmount();

        if (from.getAmount() > space) {
            from.setAmount(from.getAmount() - space);
            to.setAmount(to.getMaxStackSize());
            return false;
        } else {
            to.setAmount(to.getAmount() + from.getAmount());
            return true;
        }
    }

    private static boolean place(@NotNull ItemStack to, @NotNull ItemStack from, int place) {
        int max = to.getMaxStackSize();
        int amount = to.getAmount();
        int cursorAmount = from.getAmount();
        int move = Math.min(place, cursorAmount);

        if (max == amount) return false;

        if (amount + move <= max) {
            to.setAmount(amount + move);
            from.setAmount(cursorAmount - move);
        } else {
            to.setAmount(max);
            from.setAmount(amount + cursorAmount - max);
        }

        return true;
    }

    private static void dropItem(@NotNull Player player, @NotNull ItemStack item) {
        player.getWorld().dropItem(player.getEyeLocation(), item, i -> {
            i.setVelocity(player.getEyeLocation().getDirection().multiply(0.3D));
            i.setPickupDelay(40);
        });
    }

    private static boolean nullOrAir(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

}
