package de.codingair.tradesystem.spigot.trade.layout.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Can be used to ignore case sensitive hash codes but store the case sensitive value.
 */
public class Name {
    private final String name;

    public Name(@NotNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name name1 = (Name) o;
        return name.equalsIgnoreCase(name1.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
}
