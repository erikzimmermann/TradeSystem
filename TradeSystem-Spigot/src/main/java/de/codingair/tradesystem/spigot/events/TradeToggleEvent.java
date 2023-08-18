package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called when a trade is either finished successfully or cancelled.
 */
public class TradeToggleEvent extends TradeEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final String playerName;
    private final UUID playerUUID;
    private final boolean status;

    /**
     * Indicates a proxy trade. Only called on the server of the receiving player.
     *
     * @param playerName      The name of the player.
     * @param playerUUID    The {@link UUID} of the player.
     * @param status Status of trade toggle.
     */
    public TradeToggleEvent(@NotNull String playerName, @NotNull UUID playerUUID, boolean status) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.status = status;
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
     * @return The name of the player.
     */
    @NotNull
    public String getPlayerName() {
        return this.playerName;
    }

    /**
     * @return The {@link UUID} of the player.
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * @return The toggle status
     */
    @Nullable
    public boolean getStatus() {
        return this.status;
    }

}
