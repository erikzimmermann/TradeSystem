package de.codingair.tradesystem.spigot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called when a Player trades an item with another player between two servers bridged with a proxy.
 * @author CodingAir
 */
public class ProxyTradeItemEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private final Player receiver;
    private final String sender;
    private final ItemStack item;

    /**
     * @param receiver The player who received the item.
     * @param sender The player who gave their item away.
     * @param item The item being transferred.
     */
    public ProxyTradeItemEvent(Player receiver, String sender, ItemStack item) {
        this.receiver = Objects.requireNonNull(receiver, "receiver must not be null!");
        this.sender = Objects.requireNonNull(sender, "sender must not be null!");
        this.item = Objects.requireNonNull(item, "item must not be null!");
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * @return The player who received the item.
     */
    public Player getReceiver() {
        return this.receiver;
    }

    /**
     * @return The player who gave their item away.
     */
    public String getSender() {
        return this.sender;
    }

    /**
     * @return The item being transferred.
     */
    public ItemStack getItem() {
        return this.item;
    }
}