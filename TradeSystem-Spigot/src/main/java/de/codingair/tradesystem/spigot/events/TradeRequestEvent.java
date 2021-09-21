package de.codingair.tradesystem.spigot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player requests a trade with another player. This event is only fired if the sender does not violate against rules (see {@link de.codingair.tradesystem.spigot.trade.managers.RuleManager}).
 *
 * @author CodingAir
 */
public class TradeRequestEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final String sender;
    private final Player sendingPlayer;
    private final String receiver;
    private final Player receivingPlayer;
    private final int expiresIn;
    private boolean cancelled = false;

    /**
     * Indicates a proxy trade. Only called on the server of the receiving player.
     *
     * @param sender    The name of the player who sends the request.
     * @param receiver  The {@link Player} who receives the request.
     * @param expiresIn The expiration time of the request in seconds.
     */
    public TradeRequestEvent(@NotNull String sender, @NotNull Player receiver, int expiresIn) {
        this.sender = sender;
        this.sendingPlayer = null;
        this.receiver = receiver.getName();
        this.receivingPlayer = receiver;
        this.expiresIn = expiresIn;
    }

    /**
     * Indicates a proxy trade. Only called on the server of the sending player.
     *
     * @param sender    The {@link Player} who sends the request.
     * @param receiver  The name of the player who receives the request.
     * @param expiresIn The expiration time of the request in seconds.
     */
    public TradeRequestEvent(@NotNull Player sender, @NotNull String receiver, int expiresIn) {
        this.sender = sender.getName();
        this.sendingPlayer = sender;
        this.receiver = receiver;
        this.expiresIn = expiresIn;
        this.receivingPlayer = null;
    }

    /**
     * Indicates a bukkit trade.
     *
     * @param sender    The {@link Player} who sends the request.
     * @param receiver  The {@link Player} who receives the request.
     * @param expiresIn The expiration time of the request in seconds.
     */
    public TradeRequestEvent(@NotNull Player sender, @NotNull Player receiver, int expiresIn) {
        this.sender = sender.getName();
        this.sendingPlayer = sender;
        this.receiver = receiver.getName();
        this.receivingPlayer = receiver;
        this.expiresIn = expiresIn;
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
     * @return The {@link Player} who sends the request.
     */
    public @Nullable Player getSendingPlayer() {
        return this.sendingPlayer;
    }

    /**
     * @return The name of the player who sends the request.
     */
    public @NotNull String getSender() {
        return this.sender;
    }

    /**
     * @return The {@link Player} who receives the request. Is null if this is a proxy trade and the receiver is on another server.
     */
    public @Nullable Player getReceivingPlayer() {
        return this.receivingPlayer;
    }

    /**
     * @return {@link Boolean#TRUE} if the sender or receiver is on another server.
     */
    public boolean isProxyTrade() {
        return getSendingPlayer() == null || getReceivingPlayer() == null;
    }

    /**
     * @return The name of the player who receives the request.
     */
    public @NotNull String getReceiver() {
        return this.receiver;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @param cancelled If this event should be cancelled. If {@link Boolean#TRUE}, the request will be declined without sending a message to the {@link TradeRequestEvent#sender}.
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @return The expiration time of the request in seconds.
     */
    public int getExpiresIn() {
        return expiresIn;
    }
}
