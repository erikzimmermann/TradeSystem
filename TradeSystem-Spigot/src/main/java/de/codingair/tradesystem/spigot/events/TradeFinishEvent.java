package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import de.codingair.tradesystem.spigot.trade.TradeResult;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called when a trade is either finished successfully or cancelled.
 */
public class TradeFinishEvent extends TradeEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final String sender;
    private final UUID senderId;
    private final Player sendingPlayer;
    private final TradeResult sendingPlayerResult;
    private final String receiver;
    private final UUID receiverId;
    private final Player receivingPlayer;
    private final TradeResult receivingPlayerResult;
    private final boolean tradeResult;

    /**
     * Indicates a proxy trade. Only called on the server of the receiving player.
     *
     * @param sender      The name of the player who sends the request.
     * @param senderId    The {@link UUID} of the player who sends the request.
     * @param receiver    The {@link Player} who receives the request.
     * @param tradeResult Whether the trade was successfully finished or not.
     * @param results     The {@link TradeResult} of the sending and receiving player.
     */
    public TradeFinishEvent(@NotNull String sender, @NotNull UUID senderId, @NotNull Player receiver, boolean tradeResult, @NotNull TradeResult @NotNull [] results) {
        this.sender = sender;
        this.senderId = senderId;
        this.sendingPlayer = null;
        this.receiver = receiver.getName();
        this.receivingPlayer = receiver;
        this.receiverId = receiver.getUniqueId();

        this.tradeResult = tradeResult;
        this.sendingPlayerResult = results[0];
        this.receivingPlayerResult = results[1];
    }

    /**
     * Indicates a proxy trade. Only called on the server of the sending player.
     *
     * @param sender      The {@link Player} who sends the request.
     * @param receiver    The name of the player who receives the request.
     * @param receiverId  The {@link UUID} of the player who receives the request.
     * @param tradeResult Whether the trade was successfully finished or not.
     * @param results     The {@link TradeResult} of the sending and receiving player.
     */
    public TradeFinishEvent(@NotNull Player sender, @NotNull String receiver, @NotNull UUID receiverId, boolean tradeResult, @NotNull TradeResult @NotNull [] results) {
        this.sender = sender.getName();
        this.senderId = sender.getUniqueId();
        this.sendingPlayer = sender;
        this.receiver = receiver;
        this.receiverId = receiverId;
        this.receivingPlayer = null;

        this.tradeResult = tradeResult;
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
        this.senderId = sender.getUniqueId();
        this.sendingPlayer = sender;
        this.receiver = receiver.getName();
        this.receiverId = receiver.getUniqueId();
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
    @Nullable
    public Player getSendingPlayer() {
        return this.sendingPlayer;
    }

    /**
     * @return The name of the player who sent the request.
     */
    @NotNull
    public String getSender() {
        return this.sender;
    }

    /**
     * @return The {@link UUID} of the player who sent the request.
     */
    @NotNull
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * @return The {@link Player} who received the request. Is null if this is a proxy trade and the receiver is on another server.
     */
    @Nullable
    public Player getReceivingPlayer() {
        return this.receivingPlayer;
    }

    /**
     * @return The name of the player who received the request.
     */
    @NotNull
    public String getReceiver() {
        return this.receiver;
    }

    /**
     * @return The {@link UUID} of the player who received the request.
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
