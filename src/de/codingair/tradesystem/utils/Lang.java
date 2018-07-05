package de.codingair.tradesystem.utils;

import de.codingair.tradesystem.TradeSystem;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Lang {
    public static String getPrefix() {
        String prefix = getConfig().getString("Prefix", "&8» &r");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        return prefix;
    }

    public static String getLanguageKey() {
        return getConfig().getString("Language", "ENG");
    }

    public static String get(String key) {
        String s = getConfig().getString(getLanguageKey() + "." + key, null);
        return s == null ? null : ChatColor.translateAlternateColorCodes('&', s);
    }

    private static FileConfiguration getConfig() {
        return TradeSystem.getInstance().getFileManager().getFile("Language").getConfig();
    }
}
