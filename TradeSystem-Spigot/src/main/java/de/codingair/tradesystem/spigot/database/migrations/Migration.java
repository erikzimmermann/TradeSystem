package de.codingair.tradesystem.spigot.database.migrations;

import org.jetbrains.annotations.NotNull;

public interface Migration {

    @NotNull
    String getStatement();

    interface MultiMigration extends Migration {

        @NotNull
        String[] getStatements();

        @Override
        default @NotNull String getStatement() {
            throw new IllegalStateException("Use getStatements() instead!");
        }
    }
}
