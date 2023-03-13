package de.codingair.tradesystem.spigot.events.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TradeEvent extends Event {

    /**
     * @param player     The player who receives the message. Can be null in case of a proxy trade.
     * @param playerName The name of the player that should receive the message. Will be used for a proxy message if 'player' is null.
     * @param message    The message that should be sent.
     */
    public void sendMessage(@Nullable Player player, @NotNull String playerName, @NotNull String message) {
        if (player != null) player.sendMessage(message);
        else {
            Player any = Bukkit.getOnlinePlayers().stream().findAny().orElseThrow(() -> new IllegalStateException("No online players found!"));
            String name = TradeSystem.proxy().getCaseSensitive(playerName);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(name);
            out.writeUTF(message);

            any.sendPluginMessage(TradeSystem.getInstance(), "BungeeCord", out.toByteArray());
        }
    }
}
