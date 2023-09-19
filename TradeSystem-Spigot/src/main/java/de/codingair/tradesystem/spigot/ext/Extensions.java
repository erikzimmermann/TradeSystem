package de.codingair.tradesystem.spigot.ext;

import de.codingair.tradesystem.spigot.ext.impl.TradeAuditExt;
import de.codingair.tradesystem.spigot.ext.impl.TradeReputationExt;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Extensions {
    public static final String COLOR = "#69e070";
    public static final String COLOR_TRANSLATED = "§x§6§9§e§0§7§0";
    private static final Map<String, Extension> SUPPORTED = new HashMap<>();
    public static final Extension TradeAudit;
    public static final Extension TradeReputation;

    static {
        SUPPORTED.put("tradeaudit", TradeAudit = new TradeAuditExt());
        SUPPORTED.put("tradereputation", TradeReputation = new TradeReputationExt());
    }

    @NotNull
    public static @Unmodifiable Map<String, Extension> get() {
        return Collections.unmodifiableMap(SUPPORTED);
    }

    public static boolean isSupported(@NotNull JavaPlugin extension) {
        return SUPPORTED.containsKey(extension.getName().toLowerCase());
    }
}
