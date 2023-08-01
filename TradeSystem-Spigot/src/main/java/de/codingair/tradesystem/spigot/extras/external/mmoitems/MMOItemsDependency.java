package de.codingair.tradesystem.spigot.extras.external.mmoitems;

import de.codingair.codingapi.utils.ChatColor;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeLogReceiveItemEvent;
import de.codingair.tradesystem.spigot.events.TradeReportEvent;
import de.codingair.tradesystem.spigot.extras.external.PluginDependency;
import de.codingair.tradesystem.spigot.trade.PlayerTradeResult;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MMOItemsDependency implements PluginDependency, Listener {

    @EventHandler
    public void onTradeLog(TradeLogReceiveItemEvent e) {
        String display = getMmoFormat(e.getItem());
        if (display != null) e.setMessage(display);
    }

    @EventHandler
    public void onReport(TradeReportEvent e) {
        e.setItemReport(e.getResult().buildItemReport(item -> {
            String type = getMmoId(item);
            if (type == null) type = item.getType().name();

            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                assert meta != null;

                if (meta.hasDisplayName())
                    return TradeSystem.handler().isOnlyDisplayNameInMessage() ? meta.getDisplayName() : PlayerTradeResult.formatName(type) + " (" + ChatColor.stripColor(meta.getDisplayName()) + ")";
            }

            return PlayerTradeResult.formatName(type);
        }));
    }

    @Nullable
    private String getMmoFormat(@NotNull ItemStack item) {
        String type = getMmoType(item);
        String id = getMmoId(item);
        if (type == null || id == null) return null;

        String displayName = null;
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            if (meta.hasDisplayName()) displayName = ChatColor.stripColor(meta.getDisplayName()) + " ";
        }

        if (displayName != null) return item.getAmount() + "x " + displayName + " (MMOItem." + type + "." + id + ")";
        return item.getAmount() + "x MMOItem (" + type + "." + id + ")";
    }

    @Nullable
    private String getMmoType(@NotNull ItemStack item) {
        return MMOItems.getTypeName(item);
    }

    @Nullable
    private String getMmoId(@NotNull ItemStack item) {
        return MMOItems.getID(item);
    }

    @Nullable
    public String getMmoNameSafely(@NotNull ItemStack item) {
        if (!isAvailable()) return null;
        return getMmoType(item);
    }

    @Override
    public @NotNull String getPluginName() {
        return "MMOItems";
    }
}
