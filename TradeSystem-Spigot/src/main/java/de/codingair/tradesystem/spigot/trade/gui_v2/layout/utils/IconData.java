package de.codingair.tradesystem.spigot.trade.gui_v2.layout.utils;

import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl.TradeSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class IconData {
    private final Class<? extends TradeIcon> tradeIcon;
    private final ItemStack[] items;

    public IconData(Class<? extends TradeIcon> tradeIcon, ItemStack... items) {
        this.tradeIcon = tradeIcon;
        this.items = items;
    }

    public TradeIcon build() {
        try {
            if (MultiTradeIcon.class.isAssignableFrom(tradeIcon)) {
                return tradeIcon.getConstructor(ItemStack[].class).newInstance((Object) items);
            } else {
                if (TradeSlot.class.isAssignableFrom(tradeIcon)) return tradeIcon.newInstance();
                return tradeIcon.getConstructor(ItemStack.class).newInstance(items[0]);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //we won't get here since we try that while registering these icons
            throw new IllegalStateException("The TradeIcon " + tradeIcon.getName() + " could not be initiated.", e);
        }
    }

    public Class<? extends TradeIcon> getTradeIcon() {
        return tradeIcon;
    }

    public ItemStack[] getItems() {
        return items;
    }
}
