package de.codingair.tradesystem.utils.database.migrations;

public interface Migration {

    String getStatement();

    int getVersion();
}
