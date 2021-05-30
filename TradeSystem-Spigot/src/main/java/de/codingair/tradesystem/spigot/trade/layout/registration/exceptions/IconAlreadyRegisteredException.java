package de.codingair.tradesystem.spigot.trade.layout.registration.exceptions;

import de.codingair.tradesystem.spigot.trade.layout.types.TradeIcon;

public class IconAlreadyRegisteredException extends TradeIconException {
    public IconAlreadyRegisteredException(Class<? extends TradeIcon> icon) {
        super("The TradeIcon " + icon.getName() + " is already registered.");
    }
}
