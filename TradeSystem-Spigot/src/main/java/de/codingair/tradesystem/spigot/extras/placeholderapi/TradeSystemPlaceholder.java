package de.codingair.tradesystem.spigot.extras.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TradeSystemPlaceholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return PAPI.IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return "CodingAir";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String id) {
        return PAPI.apply(p, id);
    }
}
