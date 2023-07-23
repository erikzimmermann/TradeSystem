package de.codingair.tradesystem.spigot.utils.database.migrations;

import org.jetbrains.annotations.NotNull;

public interface Migration {

    @NotNull
    String getStatement();
}
