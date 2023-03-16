package de.codingair.tradesystem.spigot.extras.external.mmoitems;

import de.codingair.tradesystem.spigot.events.TradeLogReceiveItemEvent;
import de.codingair.tradesystem.spigot.events.TradeReportEvent;
import de.codingair.tradesystem.spigot.extras.external.PluginDependency;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class MMOItemsDependency implements PluginDependency, Listener {

    @EventHandler
    public void onTradeLog(TradeLogReceiveItemEvent e) {
        String name = MMOItems.getTypeName(e.getItem());
        if (name == null) name = e.getItem().getType().name();

        e.setMessage(e.getItem().getAmount() + "x " + name);
    }

    @EventHandler
    public void onReport(TradeReportEvent e) {
        e.setItemReport(e.getResult().buildItemReport(item -> {
            String name = MMOItems.getTypeName(item);
            if (name == null) name = item.getType().name();
            return name;
        }));
    }

    @Override
    public @NotNull String getPluginName() {
        return "MMOItems";
    }
}
