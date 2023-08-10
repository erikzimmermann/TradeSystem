package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LayoutIcon {
    private final ItemStack itemStack;

    public LayoutIcon(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @NotNull
    public final ItemStack getItemStack() {
        return itemStack;
    }
}
