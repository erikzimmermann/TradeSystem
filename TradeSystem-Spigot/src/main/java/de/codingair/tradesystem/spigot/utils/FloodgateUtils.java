package de.codingair.tradesystem.spigot.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

public class FloodgateUtils {

    public static boolean isBedrockPlayer(@NotNull Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } else return false;
    }

}
