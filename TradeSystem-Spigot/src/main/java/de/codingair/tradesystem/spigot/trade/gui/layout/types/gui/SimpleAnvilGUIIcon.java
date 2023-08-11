package de.codingair.tradesystem.spigot.trade.gui.layout.types.gui;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class SimpleAnvilGUIIcon<G> extends AnvilGUIIcon<G> {
    public SimpleAnvilGUIIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public @NotNull ItemStack buildAnvilItem(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        return new ItemBuilder(Material.PAPER)
                .setName(makeString(trade, perspective, viewer, getValue(), true))
                .getItem();
    }
}
