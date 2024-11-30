package de.codingair.tradesystem.spigot.trade;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * This class represents the result of a trade. It contains all items and economy icons that were exchanged.
 * <p>
 * It also holds the information in case of cancelled trades which means that no goods were exchanged at all (see {@link de.codingair.tradesystem.spigot.events.TradeFinishEvent}).
 */
public class TradeResult {
    protected final UUID playerId;
    protected final String playerWorld;
    protected final String playerServer;
    protected final Perspective perspective;
    protected final List<ItemStack> receivingItems = new ArrayList<>();
    protected final List<ItemStack> sendingItems = new ArrayList<>();
    protected final List<EconomyIcon<?>> economyIcons = new ArrayList<>();

    public TradeResult(@NotNull UUID playerId, @NotNull String playerWorld, @Nullable String playerServer, @NotNull Perspective perspective) {
        this.playerId = playerId;
        this.playerWorld = playerWorld;
        this.playerServer = playerServer;
        this.perspective = perspective;
    }

    /**
     * @return The ID of the player who received the result.
     */
    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * @return The world name of the player who received the result.
     */
    @NotNull
    public String getPlayerWorld() {
        return playerWorld;
    }

    /**
     * @return The server name of the player who received the result if this was a proxy trade.
     */
    @Nullable
    public String getPlayerServer() {
        return playerServer;
    }

    /**
     * @return The perspective of the player who received the result.
     */
    @NotNull
    public Perspective getPerspective() {
        return perspective;
    }

    /**
     * @return All items that were received during the exchange.
     */
    @NotNull
    public @Unmodifiable List<ItemStack> getReceivingItems() {
        return Collections.unmodifiableList(receivingItems);
    }

    /**
     * @return All items that were sent during the exchange.
     */
    @NotNull
    public @Unmodifiable List<ItemStack> getSendingItems() {
        return Collections.unmodifiableList(sendingItems);
    }

    /**
     * @return All (original) economy icons that were exchanged. Changing any values result in modified trades!
     */
    @NotNull
    public @Unmodifiable List<EconomyIcon<?>> getEconomyIcons() {
        return Collections.unmodifiableList(economyIcons);
    }

    void add(@Nullable ItemStack item, boolean receive) {
        if (item == null) return;

        if (receive) receivingItems.add(item.clone());
        else sendingItems.add(item.clone());
    }

    void add(@Nullable TradeIcon icon) {
        if (icon == null) return;
        if (icon instanceof EconomyIcon) addEconomyIcon((EconomyIcon<?>) icon);
    }

    private void addEconomyIcon(@NotNull EconomyIcon<?> economyIcon) {
        economyIcons.add(economyIcon);
    }
}
