package de.codingair.tradesystem.spigot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a request expired.
 *
 * @author CodingAir
 */
public class TradeRequestExpireEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final String sender;
    private final Player sendingPlayer;
    private final String receiver;
    private final Player receivingPlayer;

    /**
     * @param sender          The name of the player who sends the request.
     * @param sendingPlayer   The {@link Player} who sends the request.
     * @param receiver        The name of the player who receives the request.
     * @param receivingPlayer The {@link Player} who receives the request.
     */
    public TradeRequestExpireEvent(@NotNull String sender, @Nullable Player sendingPlayer, @NotNull String receiver, @Nullable Player receivingPlayer) {
        this.sender = sender;
        this.sendingPlayer = sendingPlayer;
        this.receiver = receiver;
        this.receivingPlayer = receivingPlayer;
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
}
