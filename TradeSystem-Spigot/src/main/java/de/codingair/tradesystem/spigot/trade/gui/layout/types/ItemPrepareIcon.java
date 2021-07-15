package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemPrepareIcon {

    /**
     * @param layout     The {@link ItemBuilder} which was chosen for the layout.
     * @param trade      The {@link Trade} instance.
     * @param player     The trading {@link Player}.
     * @param other      The trading {@link Player}. Null, if this is a proxy trade.
     * @param othersName The name of 'other'. Useful for proxy trades.
     * @return An {@link ItemBuilder} used to build an {@link ItemStack} to represent this trade icon in the trade GUI.
     */
    @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName);
}
