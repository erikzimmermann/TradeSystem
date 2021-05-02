package de.codingair.tradesystem.spigot.utils.money;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.money.adapters.Essentials;
import de.codingair.tradesystem.spigot.utils.money.adapters.ExpCurrency;
import de.codingair.tradesystem.spigot.utils.money.adapters.Vault;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public enum AdapterType {
    ESSENTIALS(Bukkit.getPluginManager().isPluginEnabled("Essentials") ? new Essentials() : null),
    VAULT(Bukkit.getPluginManager().isPluginEnabled("Vault") ? new Vault() : null),
    EXP(new ExpCurrency()),
    NONE(null),
    ;

    private static AdapterType USE = null;
    private final Adapter adapter;

    AdapterType(Adapter adapter) {
        this.adapter = adapter;
    }

    public static AdapterType getActiveType() {
        //initialize
        getActive();

        return USE;
    }

    public static Adapter getActive() {
        if (USE != null) {
            if (USE.adapter == null) return null;
            if (!USE.adapter.valid()) return null;

            return USE.adapter;
        }

        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();

        for (String s : config.getStringList("TradeSystem.Economy_priority")) {
            AdapterType type = AdapterType.getByName(s);
            if (type != null && type.getAdapter() != null) {
                USE = type;
                break;
            }
        }

        if (USE == null) USE = NONE;
        return getActive();
    }

    private static AdapterType getByName(String name) {
        name = name.toUpperCase();
        for (AdapterType value : values()) {
            if (value.name().equals(name)) return value;
        }

        return null;
    }

    public static boolean canEnable() {
        return getActive() != null;
    }

    public Adapter getAdapter() {
        return adapter;
    }
}
