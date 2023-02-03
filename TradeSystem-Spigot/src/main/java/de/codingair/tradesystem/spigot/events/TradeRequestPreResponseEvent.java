package de.codingair.tradesystem.spigot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player is about to respond to a trade request.
 */
public class TradeRequestPreResponseEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final String sender;
    private final Player sendingPlayer;
    private final String receiver;
    private final Player receivingPlayer;
    private final boolean accepted;
    private boolean cancelled = false;

    /**
     * @param sender          The name of the player who sends the request.
     * @param sendingPlayer   The {@link Player} who sends the request.
     * @param receiver        The name of the player who receives the request.
     * @param receivingPlayer The {@link Player} who receives the request.
     * @param accepted        True if this request was accepted. False if this request was declined.
     */
    public TradeRequestPreResponseEvent(@NotNull String sender, @Nullable Player sendingPlayer, @NotNull String receiver, @Nullable Player receivingPlayer, boolean accepted) {
        this.sender = sender;
        this.sendingPlayer = sendingPlayer;
        this.receiver = receiver;
        this.receivingPlayer = receivingPlayer;
        this.accepted = accepted;
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
     * @return The name of the player who sends the request.
     */
    public @NotNull String getSender() {
        return this.sender;
    }

    /**
     * @return The {@link Player} who sends the request. Is null if this is a proxy trade and the sender is on another server.
     */
    public @Nullable Player getSendingPlayer() {
        return this.sendingPlayer;
    }

    /**
     * @return The name of the player who receives the request.
     */
    public @NotNull String getReceiver() {
        return this.receiver;
    }

    /**
     * @return The {@link Player} who receives the request. Is null if this is a proxy trade and the receiver is on another server.
     */
    public @Nullable Player getReceivingPlayer() {
        return this.receivingPlayer;
    }

    /**
     * @return {@link Boolean#TRUE} if one of both traders is on another server.
     */
    public boolean isProxyTrade() {
        return sendingPlayer == null || receivingPlayer == null;
    }

    /**
     * Returns whether the trade request was accepted or not. The trade will be started if returning true.
     *
     * @return True if this request was accepted. False if this request was declined.
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * @return True if the event is cancelled.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @param cancelled True if the event should be cancelled.
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
