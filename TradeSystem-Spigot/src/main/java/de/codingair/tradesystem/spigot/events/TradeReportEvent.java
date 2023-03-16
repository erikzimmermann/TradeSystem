package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import de.codingair.tradesystem.spigot.trade.TradeResult;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Called when a player receives an item from another player after a trade was completed.
 */
public class TradeReportEvent extends TradeEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Player receiver;
    private final String other;
    private final Player otherPlayer;
    private final TradeResult result;
    private List<String> itemReport;
    private List<String> economyReport;
    private boolean playFinishSound = true;
    private boolean cancelled = false;

    /**
     * Indicates a proxy trade.
     *
     * @param receiver The {@link Player} who received the item.
     * @param other    The name of the player who trades the item.
     * @param result   The {@link TradeResult} of the trade.
     */
    public TradeReportEvent(@NotNull Player receiver, @NotNull String other, @NotNull TradeResult result) {
        this.receiver = receiver;
        this.other = other;
        this.result = result;
        this.otherPlayer = null;
    }

    /**
     * Indicates a bukkit trade.
     *
     * @param receiver    The {@link Player} who received the item.
     * @param otherPlayer The {@link Player} who trades the item.
     * @param result      The {@link TradeResult} of the trade.
     */
    public TradeReportEvent(@NotNull Player receiver, @NotNull Player otherPlayer, @NotNull TradeResult result) {
        this.receiver = receiver;
        this.otherPlayer = otherPlayer;
        this.result = result;
        this.other = otherPlayer.getName();
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
     * @return The {@link Player} who receives the item.
     */
    public @NotNull Player getReceiver() {
        return this.receiver;
    }

    /**
     * @return The {@link Player} who trades the item. Is null if this is a proxy trade and the sender is on another server.
     */
    public @Nullable Player getOtherPlayer() {
        return this.otherPlayer;
    }

    /**
     * @return {@link Boolean#TRUE} if the sender is on another server.
     */
    public boolean isProxyTrade() {
        return getOtherPlayer() == null;
    }

    /**
     * @return The name of the player who trades the item.
     */
    public @NotNull String getOther() {
        return this.other;
    }

    /**
     * @return The {@link TradeResult result} of the trade for the given player.
     */
    @NotNull
    public TradeResult getResult() {
        return result;
    }

    /**
     * @return The current item report of the trade.
     */
    @Nullable
    public List<String> getItemReport() {
        return itemReport;
    }

    /**
     * @param itemReport The new item report of the trade. If null, the default report will be used.
     */
    public void setItemReport(@Nullable List<String> itemReport) {
        this.itemReport = itemReport;
    }

    /**
     * @return The current economy report of the trade.
     */
    @Nullable
    public List<String> getEconomyReport() {
        return economyReport;
    }

    /**
     * @param economyReport The new economy report of the trade. If null, the default report will be used.
     */
    public void setEconomyReport(@Nullable List<String> economyReport) {
        this.economyReport = economyReport;
    }

    /**
     * @return True if the finish sound will be played.
     */
    public boolean isPlayFinishSound() {
        return playFinishSound;
    }

    /**
     * @param playFinishSound True if you wish to play the finish sound. Default is true.
     */
    public void setPlayFinishSound(boolean playFinishSound) {
        this.playFinishSound = playFinishSound;
    }

    /**
     * @return True if this event is cancelled.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @param cancel True if you wish to cancel this event. This will prevent the player from getting any report messages including that the trade was finished. The finish sound is not affected by this.
     */
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
