package de.codingair.tradesystem.spigot.trade.gui.layout.registration;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import org.jetbrains.annotations.NotNull;

public enum Type {
    BASIC("Basic", new ItemBuilder(XMaterial.ITEM_FRAME)),
    ECONOMY("Economy", new ItemBuilder(XMaterial.EMERALD));

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
