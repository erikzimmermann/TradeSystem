package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.utils.TriFunction;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface StateHolder {

    /**
     * If this icon is disabled, the click will be blocked.
     *
     * @return {@link Boolean#TRUE} if this icon is disabled. {@link Boolean#FALSE} if not.
     */
    boolean isDisabled();

    /**
     * Enables this icon, so it can be clicked again.
     */
    void enable();

    /**
     * Disables this icon, so it cannot be clicked anymore.
     */
    default void disable() {
        disable(null);
    }

    /**
     * Disables this icon, so it cannot be clicked anymore.
     *
     * @param onClickMessage The message to send to the player when he clicks on this icon when it's disabled. If null, no message will be sent.
     */
    void disable(@Nullable TriFunction<Trade, Perspective, Player, String> onClickMessage);
}
