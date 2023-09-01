package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.events.utils.TradeEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when a player toggled their status of receiving or blocking trade requests.
 */
public class TradeToggleEvent extends TradeEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final UUID playerUUID;
    private final String playerName;
    private final boolean status;

    /**
     * @param playerUUID The {@link UUID} of the player.
     * @param playerName The name of the player.
     * @param status     Status of trade toggle.
     */
    public TradeToggleEvent(@NotNull UUID playerUUID, @NotNull String playerName, boolean status) {
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
    public boolean getStatus() {
        return this.status;
    }

}
