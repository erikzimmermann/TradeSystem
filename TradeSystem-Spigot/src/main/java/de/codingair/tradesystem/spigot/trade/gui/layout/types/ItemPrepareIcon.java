package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemPrepareIcon {

    /**
     * @param layout      The {@link ItemBuilder} which was chosen for the layout.
     * @param trade       The {@link Trade} instance.
     * @param perspective The {@link Perspective} of the trading player.
     * @param viewer      The player that is viewing the trade GUI. This is not necessarily the trading player.
     * @return An {@link ItemBuilder} used to build an {@link ItemStack} to represent this trade icon in the trade GUI.
     */
    @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer);
}
