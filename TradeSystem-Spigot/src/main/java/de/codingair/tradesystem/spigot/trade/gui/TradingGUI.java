package de.codingair.tradesystem.spigot.trade.gui;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsNotWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;

public class TradingGUI extends GUI {
    private final Trade trade;
    private final int id;

    public TradingGUI(Player player, Trade trade, int id) {
        super(player, TradeSystem.getInstance(), trade.getLayout()[id].getPattern().getSize(), Lang.get("GUI_Title", player, new Lang.P("player", trade.getOther(player.getName()))),
                false  // check for plugins that already block items
        );

        this.trade = trade;
        this.id = id;
    }

    public void synchronizeTitle() {
        updateTitle(Lang.get("GUI_Title", getPlayer(), new Lang.P("player", trade.getOther(getPlayer().getName()))));
    }

    public void prepareStart() {
        registerPage(new TradingPage(this, trade, id), true);
    }

    @Override
    protected void continueGUI() throws IsNotWaitingException {
        if (trade.isCancelling()) return;
        super.continueGUI();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (!trade.getPause()[id]) trade.cancel();
    }

    public boolean isWaiting() {
        return waiting;
    }

    @Override
    public void openNestedGUI(GUI gui, boolean listenOnClose, boolean clickSound) throws AlreadyOpenedException, NoPageException, IsWaitingException {
        trade.acknowledgeGuiSwitch(player);
        super.openNestedGUI(gui, listenOnClose, clickSound);
    }
}
