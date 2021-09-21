package de.codingair.tradesystem.spigot.utils;

import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;

public class ShulkerBoxHelper {

    /**
     * Cannot be used in Minecraft v1.10 and below.
     *
     * @param item The ItemStack that will be analyzed.
     * @return The array of ItemStacks that is contained in this shulker box or an empty array if this is not a shulker box.
     */
    @NotNull
    public static ItemStack[] getItems(@NotNull ItemStack item) {
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta im = (BlockStateMeta) item.getItemMeta();

            if (im.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulker = (ShulkerBox) im.getBlockState();

                return shulker.getInventory().getContents();
            }
        }

        return new ItemStack[0];
    }

}
