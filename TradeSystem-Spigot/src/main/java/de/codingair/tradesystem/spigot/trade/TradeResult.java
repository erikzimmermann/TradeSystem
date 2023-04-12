package de.codingair.tradesystem.spigot.trade;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class represents the result of a trade. It contains all items and economy icons that were exchanged.
 * <p>
 * It also holds the information in case of cancelled trades which means that no goods were exchanged at all (see {@link de.codingair.tradesystem.spigot.events.TradeFinishEvent}).
 */
public class TradeResult {
    protected final int playerId;
    protected final LinkedHashMap<ItemStack, Boolean> items = new LinkedHashMap<>();
    protected final List<EconomyIcon<?>> economyIcons = new ArrayList<>();

    public TradeResult(int playerId) {
        this.playerId = playerId;
    }

    /**
     * @return The id of the current player. Either 0 or 1.
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * @return ALl items that were exchanged. The boolean value indicates whether the item was received (true) or given (false).
     */
    public LinkedHashMap<ItemStack, Boolean> getItems() {
        return items;
    }

    /**
     * @return All (original) economy icons that were exchanged. Changing any values result in modified trades!
     */
    public List<EconomyIcon<?>> getEconomyIcons() {
        return economyIcons;
    }

    void add(@Nullable ItemStack item, boolean receive) {
        if (item == null) return;
        items.put(item.clone(), receive);
    }

    void add(@Nullable TradeIcon icon) {
        if (icon == null) return;
        if (icon instanceof EconomyIcon) addEconomyIcon((EconomyIcon<?>) icon);
    }

    private void addEconomyIcon(@NotNull EconomyIcon<?> economyIcon) {
        economyIcons.add(economyIcon);
    }
}
