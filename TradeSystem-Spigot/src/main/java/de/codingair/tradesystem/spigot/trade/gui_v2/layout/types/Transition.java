package de.codingair.tradesystem.spigot.trade.gui_v2.layout.types;

import org.jetbrains.annotations.NotNull;

/**
 * Used for icons which must share information to another icon. For instance, when you have an exp icon which displays the amount for the other trade on another icon.<p>
 * Only used for transitions from one icon of player 1 to another icon of player 2.
 *
 * @param <T> The target {@link TradeIcon}.
 * @param <G> The type of the value which will be transitioned.
 */
public interface Transition<T extends TradeIcon & Transition.Consumer<G>, G> {

    /**
     * Will be executed after this TradeIcon instance will be updated.<br>
     * Use {@link Consumer#applyTransition(Object)} to apply the update.
     *
     * @param icon The icon which must be informed.
     */
    void inform(@NotNull T icon);

    interface Consumer<G> {
        /**
         * Do NOT update the {@link org.bukkit.inventory.ItemStack} instance after getting this update. It will be updated automatically.
         *
         * @param value The updated value.
         */
        void applyTransition(G value);
    }
}
