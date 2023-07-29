package de.codingair.tradesystem.spigot.trade.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for projecting inventory {@link Actions actions}.
 */
public interface InventoryMask {

    /**
     * @param slot The slot to set the item in.
     * @param item The item to set.
     */
    void setItem(int slot, @Nullable ItemStack item);

    /**
     * @param slot The slot to get the item from.
     * @return The item in the slot.
     */
    @Nullable ItemStack getItem(int slot);

    /**
     * Updates the current item in the slot.
     *
     * @param slot The slot to update.
     */
    default void update(int slot) {
        setItem(slot, getItem(slot));
    }

    /**
     * @return The instance that holds the original data.
     */
    @NotNull Object getHolder();

    default boolean equals(@Nullable Inventory inventory) {
        Object holder = getHolder();
        if (holder instanceof Inventory) return holder.equals(inventory);
        return false;
    }

    @NotNull
    static InventoryMask of(@NotNull Inventory inventory) {
        return new InventoryMask() {
            @Override
            public void setItem(int slot, @Nullable ItemStack item) {
                inventory.setItem(slot, item);
            }

            @Nullable
            @Override
            public ItemStack getItem(int slot) {
                return inventory.getItem(slot);
            }

            @Override
            public @NotNull Object getHolder() {
                return inventory;
            }
        };
    }

}
