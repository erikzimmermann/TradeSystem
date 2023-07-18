package de.codingair.tradesystem.spigot.trade.gui.layout.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the perspective of a player in a trade.
 */
public enum Perspective {
    /**
     * The perspective of the player that owns the trade on this server. Technically speaking, the perspective of player with id 0.
     */
    PRIMARY(0),

    /**
     * The perspective of the player that is part of this trade. Technically speaking, the perspective of player with id 1.
     */
    SECONDARY(1),

    /**
     * The perspective of a player that is not part of this trade.
     */
    TERTIARY(-1);

    private final int id;

    Perspective(int id) {
        this.id = id;
    }

    /**
     * @return The id of the player that is represented by this perspective. Used for array-based structures.
     */
    public int id() {
        return id;
    }

    /**
     * @return The other perspective. If this is {@link #PRIMARY}, {@link #SECONDARY} is returned and vice versa.
     */
    @NotNull
    public Perspective flip() {
        return this == PRIMARY ? SECONDARY : PRIMARY;
    }

    /**
     * @return Whether this perspective is {@link #PRIMARY}.
     */
    public boolean isPrimary() {
        return this == PRIMARY;
    }

    /**
     * @return Whether this perspective is {@link #SECONDARY}.
     */
    public boolean isSecondary() {
        return this == SECONDARY;
    }

    /**
     * @return Whether this perspective is {@link #TERTIARY}.
     */
    public boolean isTertiary() {
        return this == TERTIARY;
    }

    /**
     * @return An array containing all main perspectives. The main perspectives are {@link #PRIMARY} and {@link #SECONDARY}.
     */
    @NotNull
    public static Perspective[] main() {
        return new Perspective[]{PRIMARY, SECONDARY};
    }
}
