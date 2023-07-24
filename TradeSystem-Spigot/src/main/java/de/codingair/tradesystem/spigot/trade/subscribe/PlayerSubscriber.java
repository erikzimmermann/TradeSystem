package de.codingair.tradesystem.spigot.trade.subscribe;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Used to subscribe to {@link de.codingair.tradesystem.spigot.trade.Trade trade} updates.
 * <p>
 * Also adds the available {@link Player} to the viewing list to forward played sounds.
 */
public interface PlayerSubscriber extends Subscriber {

    /**
     * @return The player that should be added to the viewing list.
     */
    @NotNull Player getPlayer();

}
