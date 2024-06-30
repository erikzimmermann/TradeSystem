package de.codingair.tradesystem.spigot.utils;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.Bukkit;

public class Permissions {
    public static final String PERMISSION_NOTIFY = "TradeSystem.Notify";
    public static final String PERMISSION_MODIFY = "TradeSystem.Modify";
    public static final String PERMISSION_LOG = "TradeSystem.Log";
    public static String PERMISSION = "TradeSystem.Trade";
    public static String PERMISSION_INITIATE = "TradeSystem.Trade.Initiate";

    private static final String[] PLUGINS = {
            "LuckPerms", "PermissionsEx", "GroupManager", "Vault", "bPermissions", "PermissionsBukkit",
            "zPermissions", "UltraPermissions", "PermissionsManager", "Permissions", "PermissionsPlus"
    };

    public static void checkPermissions(boolean firstSetup) {
        Runnable runnable = () -> {
            if (!arePermissionsEnabled()) {
                Permissions.PERMISSION = null;
                Permissions.PERMISSION_INITIATE = null;
            }
        };

        if (firstSetup) {
            UniversalScheduler.getScheduler(TradeSystem.getInstance()).runTask(
            () -> {
                if (!findPermissionsPlugin()) disableInConfig();
                runnable.run();
            });
        } else runnable.run();
    }

    private static boolean findPermissionsPlugin() {
        for (String plugin : PLUGINS) {
            if (Bukkit.getPluginManager().isPluginEnabled(plugin)) return true;
        }
        return false;
    }

    private static void disableInConfig() {
        ConfigFile configFile = TradeSystem.getInstance().getFileManager().getFile("Config");
        configFile.getConfig().set("TradeSystem.Permissions", false);
        configFile.saveConfig();
    }

    public static boolean arePermissionsEnabled() {
        return TradeSystem.getInstance().getFileManager().getFile("Config")
                .getConfig()
                .getBoolean("TradeSystem.Permissions", true);
    }
}
