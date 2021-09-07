package de.codingair.tradesystem.spigot.trade.gui.layout.utils;

import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.codingapi.tools.io.utils.Serializable;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.IconHandler;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.exceptions.IconNotFoundException;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;

public class IconData implements Serializable {
    private Class<? extends TradeIcon> tradeIcon;
    private ItemStack[] items;

    public IconData(@NotNull Class<? extends TradeIcon> tradeIcon, @NotNull ItemStack... items) {
        this.tradeIcon = tradeIcon;
        this.items = items;
    }

    public IconData() {
    }

    @SuppressWarnings ({"unchecked", "SuspiciousToArrayCall"})
    @Override
    public boolean read(DataMask mask) throws IconNotFoundException {
        String name = mask.getString("icon");
        this.tradeIcon = IconHandler.getIcon(name);
        JSONArray items = mask.getList("items");
        if (items != null) this.items = (ItemStack[]) items.toArray(new ItemStack[0]);

        return true;
    }

    @Override
    public void write(DataMask mask) {
        mask.put("icon", tradeIcon.getSimpleName());
        if (items != null) mask.put("items", Arrays.asList(items));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IconData iconData = (IconData) o;
        return Objects.equals(tradeIcon, iconData.tradeIcon) && Arrays.equals(items, iconData.items);
    }

    @Override
    public int hashCode() {
        if (tradeIcon == null) return 0;
        return tradeIcon.getName().hashCode();
    }
}
