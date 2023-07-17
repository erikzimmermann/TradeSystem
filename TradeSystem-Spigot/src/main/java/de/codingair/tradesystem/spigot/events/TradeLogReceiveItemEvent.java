package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called when a player receives an item from another player after a trade was completed.
 */
public class TradeLogReceiveItemEvent extends TradeEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final Player receiver;
    private final String sender;
    private final UUID senderId;
    private final Player sendingPlayer;
    private final ItemStack item;
    private String message;

    /**
     * Indicates a proxy trade.
     *
     * @param receiver The {@link Player} who received the item.
     * @param sender   The name of the player who trades the item.
     * @param senderId The {@link UUID} of the player who trades the item.
     * @param item     The item being transferred.
     */
    public TradeLogReceiveItemEvent(@NotNull Player receiver, @NotNull String sender, @NotNull UUID senderId, @NotNull ItemStack item) {
        this.receiver = receiver;
        this.sender = sender;
        this.senderId = senderId;
        this.sendingPlayer = null;

        this.item = item;
    }

    /**
     * Indicates a bukkit trade.
     *
     * @param receiver      The {@link Player} who received the item.
     * @param sendingPlayer The {@link Player} who trades the item.
     * @param item          The item being transferred.
     */
    public TradeLogReceiveItemEvent(@NotNull Player receiver, @NotNull Player sendingPlayer, @NotNull ItemStack item) {
        this.receiver = receiver;
        this.sender = sendingPlayer.getName();
        this.senderId = sendingPlayer.getUniqueId();
        this.sendingPlayer = sendingPlayer;

        this.item = item;
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
    @NotNull
    public Player getReceiver() {
        return this.receiver;
    }

    /**
     * @return The {@link Player} who trades the item. Is null if this is a proxy trade and the sender is on another server.
     */
    @Nullable
    public Player getSendingPlayer() {
        return this.sendingPlayer;
    }

    /**
     * @return The name of the player who trades the item.
     */
    @NotNull
    public String getSender() {
        return this.sender;
    }

    /**
     * @return The {@link UUID} of the player who trades the item.
     */
    @NotNull
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * @return {@link Boolean#TRUE} if the sender is on another server.
     */
    public boolean isProxyTrade() {
        return getSendingPlayer() == null;
    }

    /**
     * @return A copy of the item being transferred.
     */
    @NotNull
    public ItemStack getItem() {
        return this.item.clone();
    }

    /**
     * @return The message that should be logged for trading the given item. The default message will be used if 'message' is null.
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * Apply a message for the second variable replacement in the {@link de.codingair.tradesystem.spigot.extras.tradelog.TradeLog#RECEIVED RECEIVED} message.
     *
     * @param message The message that should be logged for trading the given item. The default message will be used if 'message' is null.
     */
    public void setMessage(@Nullable String message) {
        this.message = message;
    }
}
