package de.codingair.tradesystem.spigot.extras.tradelog;

import org.jetbrains.annotations.NotNull;

public enum TradeLogMessages {
    NONE(null, null, "§8"),
    STARTED("started", "Trade started", "§e"),
    CANCELLED_REASON("cancelled", "Trade cancelled: %s", "§c"),
    CANCELLED("tag", "Trade cancelled", "§c"),
    FINISHED("finished", "Trade finished", "§a"),

    PAYED_MONEY("payed money", "%s payed money: %s", "§8"),
    RECEIVED_MONEY("received money", "%s received money: %s", "§8"),

    PAYED_EXP_LEVELS("traded exp levels", "%s traded exp levels: %s", "§8"),
    RECEIVED_EXP_LEVELS("received exp levels", "%s received exp levels: %s", "§8"),
    PAYED_EXP_POINTS("traded exp points", "%s traded exp points: %s", "§8"),
    RECEIVED_EXP_POINTS("received exp points", "%s received exp points: %s", "§8"),

    RECEIVE_ITEM("received ", "%s received %s", "§8"),
    ;

    private final String tag;
    private final String message;
    private final String color;

    TradeLogMessages(String tag, String message, String colorIndicator) {
        this.tag = tag;
        this.message = message;
        this.color = colorIndicator;
    }

    public static @NotNull TradeLogMessages getByString(@NotNull String s) {
        s = s.toLowerCase();

        for (TradeLogMessages value : values()) {
            if (value == NONE) continue;
            if (s.contains(value.tag)) return value;
        }

        return NONE;
    }

    public String get(Object... o) {
        return String.format(this.message, o);
    }

    public String getColor() {
        return color;
    }
}
