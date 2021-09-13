package de.codingair.tradesystem.spigot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a request was accepted or declined.
 *
 * @author CodingAir
 */
public class TradeRequestResponseEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final String sender;
    private final Player sendingPlayer;
    private final String receiver;
    private final Player receivingPlayer;
    private final boolean accepted;

    /**
     * @param sender          The name of the player who sends the request.
     * @param sendingPlayer   The {@link Player} who sends the request.
     * @param receiver        The name of the player who receives the request.
     * @param receivingPlayer The {@link Player} who receives the request.
     * @param accepted        True if this request was accepted. False if this request was declined.
     */
    public TradeRequestResponseEvent(@NotNull String sender, @Nullable Player sendingPlayer, @NotNull String receiver, @Nullable Player receivingPlayer, boolean accepted) {
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
}
