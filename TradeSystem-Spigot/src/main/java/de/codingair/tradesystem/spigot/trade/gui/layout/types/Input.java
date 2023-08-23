package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Input<G> extends TradeIcon {
    /**
     * @param input The entered text.
     * @return The converted generic G.
     */
    @Nullable G convertInput(@NotNull String input);

    /**
     * Processes the input of the SignGUI. You can use this function to send error messages like "not enough money".
     *
     * @param trade       The trade instance.
     * @param perspective The perspective of the trading player.
     * @param viewer      The player that is viewing the trade GUI. This is not necessarily the trading player.
     * @param input       The converted input.
     * @param origin      The original input value.
     * @return An {@link IconResult} for the upcoming behavior. Use {@link IconResult#GUI} to reopen the anvil GUI.
     */
    IconResult processInput(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable G input, @NotNull String origin);

    /**
     * @param trade       The trade instance.
     * @param perspective The perspective of the trading player.
     * @param viewer      The player that is viewing the trade GUI. This is not necessarily the trading player.
     * @param current     The current value.
     * @param payment     Whether the current value is the pay-value or the receive-value.
     * @return A {@link String} which will be used to display the current value.
     */
    @NotNull String makeString(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable G current, boolean payment);

    /**
     * @return The current value.
     */
    @Nullable G getValue();

    /**
     * @param value The new value.
     */
    void setValue(G value);

    /**
     * @return The default value.
     */
    G getDefault();

    /**
     * Resets the value to the default value.
     */
    default void reset() {
        setValue(getDefault());
    }

    /**
     * Resets the value to the default value and updates the trade.
     */
    default void reset(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Perspective viewer, @NotNull GUI gui) {
        setValue(getDefault());
        trade.handleClickResult(this, perspective, viewer, gui, IconResult.UPDATE);
    }
}
