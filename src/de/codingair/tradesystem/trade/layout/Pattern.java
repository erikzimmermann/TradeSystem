package de.codingair.tradesystem.trade.layout;

import java.util.List;

public interface Pattern {

    /**
     * @return an array with 54 items
     */
    List<Item> getItems();
    String getName();
}
