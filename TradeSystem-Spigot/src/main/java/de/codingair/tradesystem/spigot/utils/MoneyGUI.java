package de.codingair.tradesystem.spigot.utils;

import org.jetbrains.annotations.NotNull;

public enum MoneyGUI {
    SIGN,
    ANVIL;

    @NotNull
    public static MoneyGUI getByName(String s) {
        s = s.toLowerCase();
        for (MoneyGUI value : values()) {
            if (value.name().toLowerCase().equals(s)) return value;
        }

        return SIGN;
    }
}
