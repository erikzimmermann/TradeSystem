package de.codingair.tradesystem.spigot.trade.gui_v2.layout;

import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl.TradeSlot;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.utils.IconData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pattern {
    private final String name;
    private final IconData[] icons;

    public Pattern(@NotNull String name, @NotNull IconData[] icons) {
        this.name = name;
        this.icons = icons;
    }

    public Pattern(@NotNull String name, int size) {
        this(name, new IconData[size]);
    }

    public Pattern(@NotNull String name) {
        this(name, 54);
    }

    public String getName() {
        return name;
    }

    public TradeLayout build() {
        TradeIcon[] icons = new TradeIcon[this.icons.length];

        for (int i = 0; i < this.icons.length; i++) {
            IconData data = this.icons[i];
            icons[i] = data.build();
        }

        return new TradeLayout(this, icons);
    }

    public int getAmountOf(@NotNull Class<? extends TradeIcon> c) {
        int i = 0;

        for (IconData icon : icons) {
            if (icon.getTradeIcon().equals(c)) i++;
        }

        return i;
    }

    public IconData[] getIcons() {
        return icons;
    }

    /**
     * @param slot The specific slot.
     * @return true if the given {@link TradeIcon} is a TradeSlot.
     */
    public boolean canHoldPlayerItem(int slot) {
        IconData data = icons[slot];
        return TradeSlot.class.equals(data.getTradeIcon());
    }

    public int getTradeSlotCount() {
        return getAmountOf(TradeSlot.class);
    }

    public List<Integer> getSlotsOf(@NotNull Class<? extends TradeIcon> c) {
        List<Integer> slots = new ArrayList<>();

        for (int slot = 0; slot < this.icons.length; slot++) {
            IconData icon = this.icons[slot];
            if (c.equals(icon.getTradeIcon())) slots.add(slot);
        }

        return slots;
    }
}
