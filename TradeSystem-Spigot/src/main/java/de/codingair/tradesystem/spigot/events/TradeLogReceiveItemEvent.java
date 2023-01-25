package de.codingair.tradesystem.spigot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TradeLogReceiveItemEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final Player receiver;
    private final String sender;
    private final Player sendingPlayer;
    private final ItemStack item;
    private String message;

    /**
     * Indicates a proxy trade.
     *
     * @param receiver The {@link Player} who received the item.
     * @param sender   The name of the player who trades the item.
     * @param item     The item being transferred.
     */
    public TradeLogReceiveItemEvent(@NotNull Player receiver, @NotNull String sender, @NotNull ItemStack item) {
        this.receiver = receiver;
        this.sender = sender;
        this.item = item;
        this.sendingPlayer = null;
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
        this.sendingPlayer = sendingPlayer;
        this.item = item;
        this.sender = sendingPlayer.getName();
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
    public @Nullable Player getSendingPlayer() {
        return this.sendingPlayer;
    }

    /**
     * @return {@link Boolean#TRUE} if the sender is on another server.
     */
    public boolean isProxyTrade() {
        return getSendingPlayer() == null;
    }

    /**
     * @return The name of the player who trades the item.
     */
    public @NotNull String getSender() {
        return this.sender;
    }

    /**
     * @return A copy of the item being transferred.
     */
    public @NotNull ItemStack getItem() {
        return this.item.clone();
    }

    /**
     * @return The message that should be logged for trading the given item. The default message will be used if 'message' is null.
     */
    public @Nullable String getMessage() {
        return message;
    }

    /**
     * @param message The message that should be logged for trading the given item. The default message will be used if 'message' is null.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
