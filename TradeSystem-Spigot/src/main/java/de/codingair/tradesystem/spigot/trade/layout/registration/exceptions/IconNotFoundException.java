package de.codingair.tradesystem.spigot.trade.layout.registration.exceptions;

public class IconNotFoundException extends TradeIconException {
    public IconNotFoundException(String icon) {
        super("The icon '" + icon + "' could not be found. Either it is no longer available or a plugin requirement has not been fulfilled.");
    }
}
