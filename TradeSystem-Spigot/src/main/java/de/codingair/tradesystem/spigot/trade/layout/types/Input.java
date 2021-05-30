package de.codingair.tradesystem.spigot.trade.layout.types;

import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.layout.types.feedback.IconResult;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Input<G> {
    /**
     * @param input The entered text.
     * @return The converted generic G.
     */
    @Nullable G convertInput(@NotNull String input);

    /**
     * Processes the input of the SignGUI. You can use this function to send error messages like "not enough money".
     *
     * @param trade  The trade instance.
     * @param player The trading player.
     * @param input  The converted input.
     * @param origin The original input value.
     * @return An {@link IconResult} for the upcoming behavior. Use {@link IconResult#GUI} to reopen the anvil GUI.
     */
    IconResult processInput(@NotNull Trade trade, @NotNull Player player, @Nullable G input, @NotNull String origin);

    /**
     * @param current The current value.
     * @return A {@link String} which will be used to display the current value.
     */
    @NotNull String makeString(@Nullable G current);

    /**
     * @return The current value.
     */
    @Nullable G getValue();
}
