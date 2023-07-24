package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.StatusIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull Button getButton(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        return currentTradeIcon(trade, perspective, viewer).getButton(trade, perspective, viewer);
    }

    @Override
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
        return currentTradeIcon(trade, perspective, viewer).tryFinish(trade, perspective, viewer, initiationServer);
    }

    @Override
    public void onFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
        currentTradeIcon(trade, perspective, viewer).onFinish(trade, perspective, viewer, initiationServer);
    }

    /**
     * @param trade       The trade instance.
     * @param perspective The perspective of the viewer.
     * @param viewer      The player that is viewing the trade GUI. This is not necessarily the trading player.
     * @return The current {@link TradeIcon} which represents this {@link MultiTradeIcon} at the moment.
     */
    public abstract @NotNull TradeIcon currentTradeIcon(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer);

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
