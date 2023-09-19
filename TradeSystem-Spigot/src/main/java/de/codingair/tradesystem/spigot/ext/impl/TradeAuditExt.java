package de.codingair.tradesystem.spigot.ext.impl;

import de.codingair.tradesystem.spigot.ext.Extension;
import de.codingair.tradesystem.spigot.ext.Extensions;

public class TradeAuditExt extends Extension {
    public TradeAuditExt() {
        super("TradeAudit", "View " + Extensions.COLOR_TRANSLATED + "ongoing trades§r and " + Extensions.COLOR_TRANSLATED + "analyze traded items and economies§r with Grafana or via command.", "https://www.spigotmc.org/resources/trade-audit-view-ongoing-trades.111665/");
    }
}
