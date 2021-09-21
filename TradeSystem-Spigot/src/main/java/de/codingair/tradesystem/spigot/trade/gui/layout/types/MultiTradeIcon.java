package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.StatusIcon;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * MultiTradeIcons allow one slot to hold more than one TradeIcon.<p>
 * In order to work with MultiTradeIcons you will need to create your own custom constructor with the ItemStack[] parameter to apply the layout items for the underlying TradeIcons.<p>
 * See {@link StatusIcon} as example.
 */
public abstract class MultiTradeIcon implements TradeIcon {
    private final TradeIcon[] icons;

    public MultiTradeIcon(TradeIcon... icons) {
        this.icons = icons;
    }

    @Override
    public @NotNull Button getButton(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        return currentTradeIcon(trade, player, other, othersName).getButton(trade, player, other, othersName);
    }

    @Override
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        return currentTradeIcon(trade, player, other, othersName).tryFinish(trade, player, other, othersName, initiationServer);
    }

    @Override
    public void onFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        currentTradeIcon(trade, player, other, othersName).onFinish(trade, player, other, othersName, initiationServer);
    }

    /**
     * @param trade      The trade instance.
     * @param player     The trading player.
     * @param other      The trading partner. Null, if this is a proxy trade.
     * @param othersName The name of 'other'. Useful for proxy trades.
     * @return The current {@link TradeIcon} which represents this {@link MultiTradeIcon} at the moment.
     */
    public abstract @NotNull TradeIcon currentTradeIcon(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName);

    protected @NotNull TradeIcon getIcon(Class<? extends TradeIcon> c) {
        for (TradeIcon icon : this.icons) {
            if (c.isInstance(icon)) return icon;
        }

        throw new IllegalStateException("The TradeIcon class " + c.getName() + " was not registered in the MultiTradeIcon " + getClass().getName());
    }

    public TradeIcon[] getIcons() {
        return icons;
    }
}
