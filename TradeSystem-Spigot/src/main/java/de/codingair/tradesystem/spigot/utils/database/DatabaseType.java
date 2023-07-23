package de.codingair.tradesystem.spigot.utils.database;

import org.jetbrains.annotations.Nullable;

public enum DatabaseType {
    MYSQL,
    SQLITE,
    ;

    @Nullable
    public static DatabaseType byName(String databaseType) {
        for (DatabaseType type : values()) {
            if (type.name().equalsIgnoreCase(databaseType)) {
                return type;
            }
        }
        return null;
    }
}
