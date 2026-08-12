package de.codingair.tradesystem.spigot.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

public class FloodgateUtils {

    public static boolean isBedrockPlayer(@NotNull Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            boolean isFloodgatePlayer = FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
            if (!isFloodgatePlayer) {

                //temp lame fix, seems on <= 1.20.4 floodgate isnt loaded correctly
                //It will close the trade if 1 or 2 players are bedrock users
                //ToDO Fix me?
                if (player.getUniqueId().toString().startsWith("00000000-0000-0000")) {
                    return true;
                }
            }
            return isFloodgatePlayer;
        } else return false;
    }

    public static boolean isNonBedrockPlayer(@NotNull Player player) {
        return !isBedrockPlayer(player);
    }

}
