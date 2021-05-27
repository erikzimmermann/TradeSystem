package de.codingair.tradesystem.spigot.trade.layout.types.feedback;

import de.codingair.tradesystem.spigot.trade.layout.types.Transition;

public enum IconResult {
    /**
     * Used to continue normal trading.
     */
    PASS,

    /**
     * Extension of {@link IconResult#PASS} which additionally updates the trade GUI.<p>
     * Also sets the ready flag to false and updates TradeIcons connected by a {@link Transition}.
     */
    UPDATE,

    /**
     * Extension of {@link IconResult#UPDATE} which delays the update with 1 MC tick. The ready flag will be changed to false immediately.<p>
     * Will <bold>NOT</bold> update icons connected by a {@link Transition} since they must be updated directly through {@link IconResult#UPDATE}.
     */
    UPDATE_LATER,

    /**
     * This will only pause the own trade GUI and not the GUI of the trade partner.
     * It indicates the listener to not listen on events like {@link org.bukkit.event.inventory.InventoryCloseEvent} when changing the GUI.
     */
    GUI,

    READY,
    NOT_READY,
    CANCEL
}
