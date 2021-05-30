package de.codingair.tradesystem.spigot.trade.layout.types;

import de.codingair.tradesystem.spigot.trade.Trade;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Clickable {
    /**
     * @param trade      The {@link Trade} instance.
     * @param player     The trading {@link Player}.
     * @param other      The trading {@link Player}. Null, if this is a proxy trade.
     * @param othersName The name of 'other'. Useful for proxy trades.
     * @return {@link Boolean#TRUE} if the given TradeIcon can be clicked. {@link Boolean#FALSE} will block the click from being forwarded.
     */
    boolean isClickable(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName);
}
