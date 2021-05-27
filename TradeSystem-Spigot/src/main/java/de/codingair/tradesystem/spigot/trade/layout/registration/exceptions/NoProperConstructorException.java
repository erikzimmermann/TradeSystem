package de.codingair.tradesystem.spigot.trade.layout.registration.exceptions;

import de.codingair.tradesystem.spigot.trade.layout.types.TradeIcon;

public class NoProperConstructorException extends TradeIconException {
    public NoProperConstructorException(Class<? extends TradeIcon> icon) {
        super("The TradeIcon " + icon.getName() + " cannot be initiated since it has no constructor with an ItemStack as parameter.");
    }

    public NoProperConstructorException(Class<? extends TradeIcon> icon, Throwable cause) {
        super("The TradeIcon " + icon.getName() + " cannot be initiated since it has no constructor with an ItemStack as parameter.", cause);
    }
}
