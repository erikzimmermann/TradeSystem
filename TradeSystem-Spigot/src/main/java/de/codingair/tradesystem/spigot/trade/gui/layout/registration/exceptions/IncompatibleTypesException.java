package de.codingair.tradesystem.spigot.trade.gui.layout.registration.exceptions;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;

import java.util.Arrays;

public class IncompatibleTypesException extends TradeIconException {
    public IncompatibleTypesException(Class<? extends TradeIcon> icon, Class<?>... classes) {
        super("The TradeIcon " + icon.getName() + " cannot implement following classes together: " + Arrays.toString(classes));
    }
}
