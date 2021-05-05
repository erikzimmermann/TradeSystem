package de.codingair.tradesystem.spigot.utils.database.migrations;

public interface Migration {

    String getStatement();

    int getVersion();
}
