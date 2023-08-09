package de.codingair.tradesystem.spigot.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DatabaseType {
    MYSQL("MySQL"),
    SQLITE("SQLite"),
    ;

    private final String name;

    DatabaseType(@NotNull String name) {
        this.name = name;
    }

    @Nullable
    public static DatabaseType byName(@Nullable String databaseType) {
        for (DatabaseType type : values()) {
            if (type.name().equalsIgnoreCase(databaseType))
                return type;
        }

        return null;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
