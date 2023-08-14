package de.codingair.tradesystem.spigot.database.migrations.mysql;

import de.codingair.tradesystem.spigot.database.migrations.Migration;
import org.jetbrains.annotations.NotNull;

public class ConvertTimestampToDatetimeMigration implements Migration.MultiMigration {
    @Override
    public @NotNull String[] getStatements() {
        return new String[]{
                "ALTER TABLE tradelog ADD timestamp_temp DATETIME;",
                "UPDATE tradelog SET timestamp_temp = FROM_UNIXTIME(timestamp / 1000);",
                "ALTER TABLE tradelog DROP timestamp;",
                "ALTER TABLE tradelog CHANGE timestamp_temp timestamp DATETIME NOT NULL DEFAULT NOW();"
        };
    }
}
