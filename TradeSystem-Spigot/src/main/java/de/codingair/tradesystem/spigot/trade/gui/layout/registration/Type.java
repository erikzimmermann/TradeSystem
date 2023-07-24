package de.codingair.tradesystem.spigot.trade.gui.layout.registration;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the type of {@link de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon TradeIcons}. This will be used to categorize the icons in the layout editor.
 */
public enum Type {
    BASIC("Basic", new ItemBuilder(XMaterial.ITEM_FRAME)),
    ECONOMY("Economy", new ItemBuilder(XMaterial.EMERALD)),
    COSMETICS("Cosmetics", new ItemBuilder(XMaterial.ORANGE_DYE));

    private final String name;
    private final ItemBuilder item;

    Type(@NotNull String name, @NotNull ItemBuilder item) {
        this.name = name;
        this.item = item;
    }

    public String getName() {
        return name;
    }

    public ItemBuilder getItem() {
        return item.clone();
    }
}
