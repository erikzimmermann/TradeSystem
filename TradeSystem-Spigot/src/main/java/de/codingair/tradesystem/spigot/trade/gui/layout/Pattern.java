package de.codingair.tradesystem.spigot.trade.gui.layout;

import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.codingapi.tools.io.utils.Serializable;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.exceptions.IconNotFoundException;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlot;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.IconData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Pattern implements Serializable, Iterable<IconData> {
    private String name;
    private IconData[] icons;

    public Pattern(@NotNull String name, @NotNull IconData[] icons) {
        this.name = name;
        this.icons = icons;
    }

    public Pattern() {
    }

    @Override
    public boolean read(DataMask mask) throws IconNotFoundException {
        this.name = deserializeName(mask);

        Map<?, ?> icons = mask.get("icons");
        this.icons = new IconData[54];

        IconNotFoundException exception = null;
        for (Map.Entry<?, ?> e : icons.entrySet()) {
            if (e.getKey() instanceof Integer && e.getValue() instanceof Map) {
                try {
                    JSON json = new JSON((Map<?, ?>) e.getValue());

                    IconData icon = new IconData();
                    icon.read(json);
                    this.icons[(Integer) e.getKey()] = icon;
                } catch (IconNotFoundException ex) {
                    if (exception == null) exception = ex;
                }
            }
        }

        if (exception != null) throw exception;
        return true;
    }

    public static String deserializeName(DataMask mask) {
        return mask.getString("name");
    }

    @Override
    public void write(DataMask mask) {
        Map<Integer, JSON> icons = new HashMap<>();

        for (int slot = 0; slot < this.icons.length; slot++) {
            IconData icon = this.icons[slot];

            if (icon != null) {
                JSON json = new JSON();
                icon.write(json);
                icons.put(slot, json);
            }
        }

        mask.put("name", name);
        mask.put("icons", icons);
    }

    public String getName() {
        return name;
    }

    public TradeLayout build() {
        TradeIcon[] icons = new TradeIcon[this.icons.length];

        for (int i = 0; i < this.icons.length; i++) {
            IconData data = this.icons[i];
            if (data != null) icons[i] = data.build();
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
            if (icon != null && c.equals(icon.getTradeIcon())) slots.add(slot);
        }

        return slots;
    }

    @NotNull
    @Override
    public Iterator<IconData> iterator() {
        return Arrays.stream(this.icons).iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pattern pattern = (Pattern) o;
        return Objects.equals(name, pattern.name) && Arrays.equals(icons, pattern.icons);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(icons);
        return result;
    }
}
