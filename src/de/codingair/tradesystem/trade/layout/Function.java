package de.codingair.tradesystem.trade.layout;

public enum Function {
    DECORATION(false, false),
    PICK_MONEY(true, false),
    SHOW_MONEY(true, false),
    MONEY_REPLACEMENT(false, true),
    PICK_STATUS_NONE(true, false),
    PICK_STATUS_NOT_READY(true, true),
    PICK_STATUS_READY(true, true),
    SHOW_STATUS_NOT_READY(true, false),
    SHOW_STATUS_READY(true, true),
    CANCEL(true, false),
    EMPTY_FIRST_TRADER(true, false),
    EMPTY_SECOND_TRADER(true, false);

    private boolean function;
    private boolean ambiguous;

    Function(boolean function, boolean ambiguous) {
        this.function = function;
        this.ambiguous = ambiguous;
    }

    public boolean isFunction() {
        return function;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }
}
