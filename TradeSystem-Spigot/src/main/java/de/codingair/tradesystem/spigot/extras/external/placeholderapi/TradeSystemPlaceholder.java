package de.codingair.tradesystem.spigot.extras.external.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TradeSystemPlaceholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return PlaceholderDependency.IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return "CodingAir";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2";
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String id) {
        return PlaceholderDependency.apply(p, id);
    }

    @Override
    public boolean persist() {
        return true;
    }
}
