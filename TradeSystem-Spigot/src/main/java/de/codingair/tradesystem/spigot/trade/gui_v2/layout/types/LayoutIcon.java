package de.codingair.tradesystem.spigot.trade.gui_v2.layout.types;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LayoutIcon {
    protected final ItemStack itemStack;

    public LayoutIcon(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
