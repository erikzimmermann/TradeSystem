package de.codingair.tradesystem.spigot.trade;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class represents the result of a trade. It contains all items and economy icons that were exchanged.
 * <p>
 * It also holds the information in case of cancelled trades which means that no goods were exchanged at all (see {@link de.codingair.tradesystem.spigot.events.TradeFinishEvent}).
 */
public class TradeResult {
    protected final UUID playerId;
    protected final Perspective perspective;
    protected final List<ItemStack> receivingItems = new ArrayList<>();
    protected final List<ItemStack> sendingItems = new ArrayList<>();
    protected final List<EconomyIcon<?>> economyIcons = new ArrayList<>();

    public TradeResult(@NotNull UUID playerId, @NotNull Perspective perspective) {
        this.playerId = playerId;
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
    public List<ItemStack> getReceivingItems() {
        return receivingItems;
    }

    /**
     * @return All items that were sent during the exchange.
     */
    @NotNull
    public List<ItemStack> getSendingItems() {
        return sendingItems;
    }

    /**
     * @return All (original) economy icons that were exchanged. Changing any values result in modified trades!
     */
    @NotNull
    public List<EconomyIcon<?>> getEconomyIcons() {
        return economyIcons;
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
