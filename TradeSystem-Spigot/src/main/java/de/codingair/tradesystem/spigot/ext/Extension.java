package de.codingair.tradesystem.spigot.ext;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Extension {
    private final String name;
    private final String description;

    public Extension(@NotNull String name, @Nullable String description) {
        this.name = name;
        this.description = description;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled(getName());
    }

    @Nullable
    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(name);
    }
}
