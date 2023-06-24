package de.codingair.tradesystem.spigot.extras.external.mmoitems;

import de.codingair.tradesystem.spigot.events.TradeLogReceiveItemEvent;
import de.codingair.tradesystem.spigot.events.TradeReportEvent;
import de.codingair.tradesystem.spigot.extras.external.PluginDependency;
import de.codingair.tradesystem.spigot.trade.PlayerTradeResult;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MMOItemsDependency implements PluginDependency, Listener {

    @EventHandler
    public void onTradeLog(TradeLogReceiveItemEvent e) {
        String name = getMmoName(e.getItem());
        if (name == null) name = e.getItem().getType().name();

        e.setMessage(e.getItem().getAmount() + "x " + name);
    }

    @EventHandler
    public void onReport(TradeReportEvent e) {
        e.setItemReport(e.getResult().buildItemReport(item -> {
            String name = getMmoName(item);
            if (name == null) name = PlayerTradeResult.itemToNameMapper().apply(item);
            return name;
        }));
    }

    @Nullable
    private String getMmoName(@NotNull ItemStack item) {
        return MMOItems.getTypeName(item);
    }

    @Nullable
    public String getMmoNameSafely(@NotNull ItemStack item) {
        if (!isAvailable()) return null;
        return getMmoName(item);
    }

    @Override
    public @NotNull String getPluginName() {
        return "MMOItems";
    }
}
