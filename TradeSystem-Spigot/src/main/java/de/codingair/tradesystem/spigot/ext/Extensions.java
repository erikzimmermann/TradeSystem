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
    private static final Map<String, Extension> SUPPORTED = new HashMap<>();

    static {
        SUPPORTED.put("tradeaudit", new TradeAuditExt());
        SUPPORTED.put("tradereputation", new TradeReputationExt());
    }

    @NotNull
    public static @Unmodifiable Map<String, Extension> get() {
        return Collections.unmodifiableMap(SUPPORTED);
    }

    public static boolean isSupported(@NotNull JavaPlugin extension) {
        return SUPPORTED.containsKey(extension.getName().toLowerCase());
    }
}
