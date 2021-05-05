package de.codingair.tradesystem.spigot.tradelog;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;

public class TradeLogOptions {
    private static final ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private static final FileConfiguration config = file.getConfig();
    private static final boolean enabled = config.getBoolean("TradeSystem.TradeLog.Enabled", false);

    public static boolean isEnabled() {
        return enabled;
    }
}
