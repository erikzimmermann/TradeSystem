package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Called on each countdown tick when finishing a trade. This is mainly introduced to allow developers to modify open inventories when running countdowns (see <a href="https://github.com/erikzimmermann/TradeSystem/issues/65">#65</a>). Changed inventories will be reset to normal when a countdown is canceled.
 */
public class TradeCountdownEvent extends TradeEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final Trade trade;
    private final Perspective perspective;
    private final Player viewer;
    private final Inventory openInventory;
    private final int intervals;
    private final int intervalTickDuration;
    private final int remainingIntervals;

    public TradeCountdownEvent(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @NotNull Inventory openInventory, int intervals, int intervalTickDuration, int remainingIntervals) {
        this.trade = trade;
        this.perspective = perspective;
        this.viewer = viewer;
        this.openInventory = openInventory;
        this.intervals = intervals;
        this.intervalTickDuration = intervalTickDuration;
        this.remainingIntervals = remainingIntervals;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * @return The ongoing trade instance.
     */
    @NotNull
    public Trade getTrade() {
        return trade;
    }

    /**
     * @return The perspective of the player viewing the trade.
     */
    @NotNull
    public Perspective getPerspective() {
        return perspective;
    }

    /**
     * @return The viewing player.
     */
    @NotNull
    public Player getViewer() {
        return viewer;
    }

    /**
     * @return The watched inventory of the given player. This inventory can be modified whose changes are instantly shown to the player. Keep in mind to avoid modifying trade slots that are used by trading players as those items are not locked from being moved to own inventories.
     */
    @NotNull
    public Inventory getOpenInventory() {
        return openInventory;
    }

    /**
     * @return The number of intervals this countdown is going to run.
     */
    public int getIntervals() {
        return intervals;
    }

    /**
     * @return The number of ticks a single interval delays the countdown.
     */
    public int getIntervalTickDuration() {
        return intervalTickDuration;
    }

    /**
     * @return The number of remaining intervals for this ongoing countdown.
     */
    public int getRemainingIntervals() {
        return remainingIntervals;
    }
}
