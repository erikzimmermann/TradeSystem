package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import de.codingair.tradesystem.spigot.trade.TradeResult;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a trade is either finished successfully or cancelled.
 */
public class TradeFinishEvent extends TradeEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final String sender;
    private final Player sendingPlayer;
    private final TradeResult sendingPlayerResult;
    private final String receiver;
    private final Player receivingPlayer;
    private final TradeResult receivingPlayerResult;
    private final boolean tradeResult;

    /**
     * Indicates a proxy trade. Only called on the server of the receiving player.
     *
     * @param sender      The name of the player who sends the request.
     * @param receiver    The {@link Player} who receives the request.
     * @param tradeResult Whether the trade was successfully finished or not.
     * @param results     The {@link TradeResult} of the sending and receiving player.
     */
    public TradeFinishEvent(@NotNull String sender, @NotNull Player receiver, boolean tradeResult, @NotNull TradeResult @NotNull [] results) {
        this.sender = sender;
        this.tradeResult = tradeResult;
        this.sendingPlayer = null;
        this.receiver = receiver.getName();
        this.receivingPlayer = receiver;

        this.sendingPlayerResult = results[0];
        this.receivingPlayerResult = results[1];
    }

    /**
     * Indicates a proxy trade. Only called on the server of the sending player.
     *
     * @param sender      The {@link Player} who sends the request.
     * @param receiver    The name of the player who receives the request.
     * @param tradeResult Whether the trade was successfully finished or not.
     * @param results     The {@link TradeResult} of the sending and receiving player.
     */
    public TradeFinishEvent(@NotNull Player sender, @NotNull String receiver, boolean tradeResult, @NotNull TradeResult @NotNull [] results) {
        this.sender = sender.getName();
        this.sendingPlayer = sender;
        this.receiver = receiver;
        this.tradeResult = tradeResult;
        this.receivingPlayer = null;

        this.sendingPlayerResult = results[0];
        this.receivingPlayerResult = results[1];
    }

    /**
     * Indicates a bukkit trade.
     *
     * @param sender      The {@link Player} who sends the request.
     * @param receiver    The {@link Player} who receives the request.
     * @param tradeResult Whether the trade was successfully finished or not.
     * @param results     The {@link TradeResult} of the sending and receiving player.
     */
    public TradeFinishEvent(@NotNull Player sender, @NotNull Player receiver, boolean tradeResult, @NotNull TradeResult @NotNull [] results) {
        this.sender = sender.getName();
        this.sendingPlayer = sender;
        this.receiver = receiver.getName();
        this.receivingPlayer = receiver;
        this.tradeResult = tradeResult;

        this.sendingPlayerResult = results[0];
        this.receivingPlayerResult = results[1];
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
     * @return The {@link Player} who sent the request.
     */
    public @Nullable Player getSendingPlayer() {
        return this.sendingPlayer;
    }

    /**
     * @return The name of the player who sent the request.
     */
    public @NotNull String getSender() {
        return this.sender;
    }

    /**
     * @return The {@link Player} who received the request. Is null if this is a proxy trade and the receiver is on another server.
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
     * @return The name of the player who received the request.
     */
    public @NotNull String getReceiver() {
        return this.receiver;
    }

    /**
     * @return Whether the trade was successfully finished or not.
     */
    public boolean getTradeResult() {
        return tradeResult;
    }

    /**
     * @return The {@link TradeResult} of the player that sent the trade request. In case of a cancelled trade, this result instance also holds information about the last state of the trade.
     */
    @NotNull
    public TradeResult getSendingPlayerResult() {
        return sendingPlayerResult;
    }

    /**
     * @return The {@link TradeResult} of the player that received the trade request. In case of a cancelled trade, this result instance also holds information about the last state of the trade.
     */
    @NotNull
    public TradeResult getReceivingPlayerResult() {
        return receivingPlayerResult;
    }
}
