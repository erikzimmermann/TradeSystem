package de.codingair.tradesystem.spigot.trade.layout.utils;

import de.codingair.tradesystem.spigot.trade.layout.Function;
import de.codingair.tradesystem.spigot.trade.layout.Item;

import java.util.List;

public interface Pattern {

    /**
     * @return an array with 54 items
     */
    List<Item> getItems();

    String getName();

    boolean isStandard();

    default int getTradeSlotCount() {
        int i = 0;
        for (Item item : getItems()) {
            if (item.getFunction() == Function.EMPTY_FIRST_TRADER) i++;
        }
        return i;
    }
}
