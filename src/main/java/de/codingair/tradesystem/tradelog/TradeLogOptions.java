package de.codingair.tradesystem.tradelog;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;

public class TradeLogOptions {
    private static ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private static FileConfiguration config = file.getConfig();
    private static boolean enabled = config.getBoolean("TradeSystem.TradeLog.Enabled", false);

    public static boolean isEnabled() {
        return enabled;
    }
}
