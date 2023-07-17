package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called when a player requests a trade with another player. This event is only fired if the sender does not violate against rules (see {@link de.codingair.tradesystem.spigot.trade.managers.RuleManager}).
 */
public class TradeRequestEvent extends TradeEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final String sender;
    private final UUID senderId;
    private final Player sendingPlayer;
    private final String receiver;
    private final UUID receiverId;
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
    public TradeRequestEvent(@NotNull String sender, @NotNull UUID senderId, @NotNull Player receiver, int expiresIn) {
        this.sender = sender;
        this.senderId = senderId;
        this.sendingPlayer = null;
        this.receiver = receiver.getName();
        this.receiverId = receiver.getUniqueId();
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
    public TradeRequestEvent(@NotNull Player sender, @NotNull String receiver, @NotNull UUID receiverId, int expiresIn) {
        this.sender = sender.getName();
        this.senderId = sender.getUniqueId();
        this.sendingPlayer = sender;
        this.receiver = receiver;
        this.receiverId = receiverId;
        this.receivingPlayer = null;

        this.expiresIn = expiresIn;
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
        this.senderId = sender.getUniqueId();
        this.sendingPlayer = sender;
        this.receiver = receiver.getName();
        this.receiverId = receiver.getUniqueId();
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
    @Nullable
    public Player getSendingPlayer() {
        return this.sendingPlayer;
    }

    /**
     * @return The name of the player who sends the request.
     */
    @NotNull
    public String getSender() {
        return this.sender;
    }

    /**
     * @return The {@link UUID} of the player who sends the request.
     */
    @NotNull
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * @return The {@link Player} who receives the request. Is null if this is a proxy trade and the receiver is on another server.
     */
    @Nullable
    public Player getReceivingPlayer() {
        return this.receivingPlayer;
    }

    /**
     * @return The name of the player who receives the request.
     */
    @NotNull
    public String getReceiver() {
        return this.receiver;
    }

    /**
     * @return The {@link UUID} of the player who receives the request.
     */
    @NotNull
    public UUID getReceiverId() {
        return receiverId;
    }

    /**
     * @return {@link Boolean#TRUE} if the sender or receiver is on another server.
     */
    public boolean isProxyTrade() {
        return getSendingPlayer() == null || getReceivingPlayer() == null;
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
