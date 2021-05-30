package de.codingair.tradesystem.spigot.utils;

import org.jetbrains.annotations.NotNull;

public enum InputGUI {
    SIGN,
    ANVIL;

    @NotNull
    public static InputGUI getByName(String s) {
        s = s.toLowerCase();
        for (InputGUI value : values()) {
            if (value.name().toLowerCase().equals(s)) return value;
        }

        return SIGN;
    }
}
