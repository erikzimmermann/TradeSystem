package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Called when a player receives something that was traded by an economy icon from another player after a trade was completed.
 */
public class TradeReceiveEconomyEvent extends TradeEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final Player receiver;
    private final String sender;
    private final Player sendingPlayer;
    private final BigDecimal balance;
    private final String nameSingular;
    private final String namePlural;

    /**
     * Indicates a proxy trade.
     *
     * @param receiver     The {@link Player} who received the item.
     * @param sender       The name of the player who trades the item.
     * @param balance      The balance being transferred.
     * @param nameSingular The name of the currency in singular.
     * @param namePlural   The name of the currency in plural.
     */
    public TradeReceiveEconomyEvent(@NotNull Player receiver, @NotNull String sender, @NotNull BigDecimal balance, @NotNull String nameSingular, @NotNull String namePlural) {
        this.receiver = receiver;
        this.sender = sender;
        this.balance = balance;
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
        this.sendingPlayer = null;
    }

    /**
     * Indicates a bukkit trade.
     *
     * @param receiver      The {@link Player} who received the item.
     * @param sendingPlayer The {@link Player} who trades the item.
     * @param balance       The balance being transferred.
     * @param nameSingular  The name of the currency in singular.
     * @param namePlural    The name of the currency in plural.
     */
    public TradeReceiveEconomyEvent(@NotNull Player receiver, @NotNull Player sendingPlayer, @NotNull BigDecimal balance, @NotNull String nameSingular, @NotNull String namePlural) {
        this.receiver = receiver;
        this.sendingPlayer = sendingPlayer;
        this.balance = balance;
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
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
     * @return The balance being transferred.
     */
    public @NotNull BigDecimal getBalance() {
        return this.balance;
    }

    /**
     * @return The name of the currency in singular.
     */
    public @NotNull String getNameSingular() {
        return this.nameSingular;
    }

    /**
     * @return The name of the currency in plural.
     */
    public @NotNull String getNamePlural() {
        return this.namePlural;
    }
}
