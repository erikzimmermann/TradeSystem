package de.codingair.tradesystem.spigot.database.migrations;

import org.jetbrains.annotations.NotNull;

public interface Migration {

    @NotNull
    String getStatement();
}
