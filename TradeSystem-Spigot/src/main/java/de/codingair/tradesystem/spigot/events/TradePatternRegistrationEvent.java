package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Called during onEnable when loading trade layouts. This allows you to register your own trade layout.
 */
public class TradePatternRegistrationEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final Set<Pattern> patterns = new HashSet<>();

    /**
     * Adds a custom pattern to the default list. This pattern can be further configured in the in-game layout editor later on.
     *
     * @param pattern Your pattern.
     */
    public void addPattern(@NotNull Pattern pattern) {
        this.patterns.add(pattern);
    }

    /**
     * @return An unmodifiable set of all patterns that were added during this event.
     */
    @NotNull
    public @Unmodifiable Set<Pattern> getPatterns() {
        return Collections.unmodifiableSet(patterns);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}