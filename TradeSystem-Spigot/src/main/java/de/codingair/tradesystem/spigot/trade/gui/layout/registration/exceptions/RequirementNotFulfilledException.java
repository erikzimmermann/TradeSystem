package de.codingair.tradesystem.spigot.trade.gui.layout.registration.exceptions;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;

public class RequirementNotFulfilledException extends TradeIconException {
    public RequirementNotFulfilledException(Class<? extends TradeIcon> icon) {
        super("The requirements of the icon " + icon.getName() + " are not fulfilled!");
    }
}
