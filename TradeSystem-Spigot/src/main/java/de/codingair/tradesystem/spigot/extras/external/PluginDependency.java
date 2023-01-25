package de.codingair.tradesystem.spigot.extras.external;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public interface PluginDependency {
    default void onEnable() {
    }

    default void onDisable() {
    }

    default boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }

    @NotNull String getPluginName();
}
