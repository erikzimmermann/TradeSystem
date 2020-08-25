package de.codingair.tradesystem.extras.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PAPI {
    private static Boolean enabled = null;

    public static boolean isEnabled() {
        if(enabled == null) enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        return enabled;
    }

    public static String convert(String s, Player player) {
        if(isEnabled()) return PlaceholderAPI.setPlaceholders(player, s);
        else return s;
    }

    public static void register() {
        if(isEnabled()) new TradeSystemPlaceholder().register();
    }
}
