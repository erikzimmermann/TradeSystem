package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Clickable {
    /**
     * @param trade       The {@link Trade} instance.
     * @param perspective The {@link Perspective} of the trading player.
     * @param viewer      The player that is viewing the trade GUI. This is not necessarily the trading player.
     * @return {@link Boolean#TRUE} if the given TradeIcon can be clicked. {@link Boolean#FALSE} will block the click from being forwarded.
     */
    boolean isClickable(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer);
}
