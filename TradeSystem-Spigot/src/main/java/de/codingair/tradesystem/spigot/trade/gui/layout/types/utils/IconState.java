package de.codingair.tradesystem.spigot.trade.gui.layout.types.utils;

import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IconState {
    private boolean disabled = false;
    private TriFunction<Trade, Perspective, Player, String> onClickMessage = null;

    /**
     * Sends the given message to the given player.
     *
     * @param player  The player to send the message to.
     * @param message The message to send. The prefix will be prepended automatically.
     */
    private void sendMessage(@NotNull Player player, @Nullable String message) {
        if (message == null || message.isEmpty()) return;
        player.sendMessage(Lang.getPrefix() + message);
    }

    /**
     * Checks the state of this icon and automatically sends a message to the player if it's disabled.
     *
     * @param trade       The trade instance.
     * @param perspective The perspective of the trading player.
     * @param viewer      The player that is viewing the trade GUI. This is not necessarily the trading player.
     * @return {@link Boolean#TRUE} if the given TradeIcon can be clicked. {@link Boolean#FALSE} will block the click from being forwarded.
     */
    public boolean checkState(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        if (disabled) {
            if (onClickMessage != null)
                sendMessage(viewer, onClickMessage.apply(trade, perspective, viewer));
            return false;
        } else return true;
    }

    /**
     * Enables this icon, so it can be clicked again.
     */
    public void enable() {
        disabled = false;
        onClickMessage = null;
    }

    /**
     * Disables this icon, so it cannot be clicked anymore.
     *
     * @param onClickMessage The message to send to the player when he clicks on this icon when it's disabled. If null, no message will be sent.
     */
    public void disable(@Nullable TriFunction<Trade, Perspective, Player, String> onClickMessage) {
        this.disabled = true;
        this.onClickMessage = onClickMessage;
    }

    /**
     * @return {@link Boolean#TRUE} if this icon is disabled. {@link Boolean#FALSE} if not.
     */
    public boolean isDisabled() {
        return disabled;
    }
}
