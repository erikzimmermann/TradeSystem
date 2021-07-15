package de.codingair.tradesystem.spigot.trade.gui;

import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlot;
import org.bukkit.entity.Player;

public class TradingPage extends Page {
    private final Trade trade;
    private final int id;

    public TradingPage(TradingGUI gui, Trade trade, int id) {
        super(gui);
        this.trade = trade;
        this.id = id;
    }

    @Override
    public void buildItems() {
        TradeIcon[] icons = trade.getLayout()[id].getIcons();

        Player player = gui.getPlayer();
        Player other = trade.getOther(player).orElse(null);
        String othersName = trade.getOther(player.getName());

        for (int slot = 0; slot < icons.length; slot++) {
            TradeIcon icon = icons[slot];
            if (icon == null || icon instanceof TradeSlot) continue;

            addButton(slot, icon.getButton(trade, player, other, othersName));
        }
    }
}
