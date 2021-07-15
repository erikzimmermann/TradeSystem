package de.codingair.tradesystem.spigot.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player offers an item. Can be cancelled if this item should be blocked.
 *
 * @author CodingAir
 */
public class TradeOfferItemEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private final String receiver;
    private final Player receivingPlayer;
    private final ItemStack itemStack;
    private boolean cancelled;

    /**
     * Indicates a proxy trade.
     *
     * @param player    The {@link Player} who places the item.
     * @param receiver  The name of the player who would receive the item.
     * @param itemStack The traded {@link ItemStack}. Cannot be modified.
     * @param cancelled {@link Boolean#TRUE} if this {@link ItemStack} is blacklisted (see {@link de.codingair.tradesystem.spigot.trade.TradeHandler#isBlocked(Player, String, ItemStack)}).
     */
    public TradeOfferItemEvent(@NotNull Player player, @NotNull String receiver, @NotNull ItemStack itemStack, boolean cancelled) {
        this.player = player;
        this.receiver = receiver;
        this.itemStack = itemStack;
        this.cancelled = cancelled;
        this.receivingPlayer = null;
    }

    /**
     * Indicates a bukkit trade.
     *
     * @param player          The {@link Player} who places the item.
     * @param receivingPlayer The {@link Player} who would receive the item.
     * @param itemStack       The traded {@link ItemStack}. Cannot be modified.
     * @param cancelled       {@link Boolean#TRUE} if this {@link ItemStack} is blacklisted (see {@link de.codingair.tradesystem.spigot.trade.TradeHandler#isBlocked(Player, String, ItemStack)}).
     */
    public TradeOfferItemEvent(@NotNull Player player, @NotNull Player receivingPlayer, @NotNull ItemStack itemStack, boolean cancelled) {
        this.player = player;
        this.receivingPlayer = receivingPlayer;
        this.itemStack = itemStack;
        this.cancelled = cancelled;
        this.receiver = receivingPlayer.getName();
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
     * @return The {@link Player} who places the item.
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * @return The name of the player who would receive the item.
     */
    public @NotNull String getReceiver() {
        return receiver;
    }

    /**
     * @return The {@link Player} who would receive the item. Is null if this is a proxy trade and the receiver is on another server.
     */
    public @Nullable Player getReceivingPlayer() {
        return this.receivingPlayer;
    }

    /**
     * @return {@link Boolean#TRUE} if the receiver is on another server.
     */
    public boolean isProxyTrade() {
        return getReceivingPlayer() == null;
    }

    /**
     * @return A copy of the traded {@link ItemStack}. Cannot be modified.
     */
    public @NotNull ItemStack getItemStack() {
        return itemStack.clone();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @param cancelled If this event should be cancelled. If {@link Boolean#TRUE}, the item will be marked as blocked. See usage of {@link de.codingair.tradesystem.spigot.trade.TradeHandler#isBlocked(Player, String, ItemStack)}.
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
