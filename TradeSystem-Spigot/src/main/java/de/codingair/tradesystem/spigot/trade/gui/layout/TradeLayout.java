package de.codingair.tradesystem.spigot.trade.gui.layout;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import org.jetbrains.annotations.NotNull;

public class TradeLayout {
    private final Pattern pattern;
    private final TradeIcon[] icons;

    public TradeLayout(@NotNull Pattern pattern, @NotNull TradeIcon[] icons) {
        this.pattern = pattern;
        this.icons = icons;
    }

    public TradeLayout(@NotNull Pattern pattern, int size) {
        this(pattern, new TradeIcon[size]);
    }

    public TradeLayout(@NotNull Pattern pattern) {
        this(pattern, pattern.getSize());
    }

    /**
     * Some entries are null. These slots mean that there are no buttons placed as they will be handled exceptional (see {@link Pattern#build()}).
     *
     * @return An array of {@link TradeIcon}s.
     */
    public TradeIcon[] getIcons() {
        return icons;
    }

    /**
     * @param slot The specific slot.
     * @return true if the given {@link TradeIcon} is a TradeSlot.
     */
    public boolean canHoldPlayerItem(int slot) {
        return this.pattern.canHoldPlayerItem(slot);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public @NotNull <T extends TradeIcon> T getIcon(@NotNull Class<T> c) {
        for (TradeIcon icon : this.icons) {
            if (c.isInstance(icon)) {
                //noinspection unchecked
                return (T) icon;
            }
        }

        throw new IllegalStateException("Cannot find a TradeIcon with class " + c.getName());
    }

    public int getSlotOf(@NotNull TradeIcon icon) {
        for (int i = 0; i < this.icons.length; i++) {
            TradeIcon other = this.icons[i];
            if (icon.equals(other)) return i;
        }

        throw new IllegalStateException("Cannot find icon " + icon.getClass() + " in layout \"" + pattern.getName() + "\"");
    }

    public boolean areTradeIconsEmpty() {
        for (TradeIcon icon : this.icons) {
            if (icon != null && !icon.isEmpty()) return false;
        }

        return true;
    }
}
