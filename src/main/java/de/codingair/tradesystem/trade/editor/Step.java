package de.codingair.tradesystem.trade.editor;

public enum Step {
    MENU,
    CHOOSE_ITEMS,
    SET_FUNCTIONS,
    SET_MULTIPLE_ITEMS,
    CHOOSE_NAME,
    FINISH;

    public Step getNext() {
        int current = 0;

        for(Step step : values()) {
            if(step == this) break;
            current++;
        }

        return values().length == current ? null : values()[current + 1];
    }

    public Step getPrevious() {
        int current = 0;

        for(Step step : values()) {
            if(step == this) break;
            current++;
        }

        return current == 0 ? null : values()[current - 1];
    }
}
