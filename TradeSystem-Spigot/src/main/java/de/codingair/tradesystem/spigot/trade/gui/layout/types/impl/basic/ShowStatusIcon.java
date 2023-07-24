package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.cosmetics.PlayerHeadUtils;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ShowStatusIcon extends MultiTradeIcon {
    public ShowStatusIcon(ItemStack[] items) {
        super(new ShowNotReadyIcon(items[0]), new ShowReadyIcon(items[1]));
    }

    @Override
    public @NotNull TradeIcon currentTradeIcon(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        if (trade.getReady()[perspective.flip().id()]) return getIcon(ShowReadyIcon.class);
        else return getIcon(ShowNotReadyIcon.class);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void serialize(@NotNull DataOutputStream out) throws IOException {

    }

    @Override
    public void deserialize(@NotNull DataInputStream in) throws IOException {

    }

    public static class ShowReadyIcon extends DecorationIcon {
        public ShowReadyIcon(@NotNull ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
            PlayerHeadUtils.applyPlayerHead(layout, trade.getPlayer(perspective.flip()), trade.getNames()[perspective.flip().id()]);
            return layout.setName("§7" + Lang.get("Status", viewer) + ": §a" + Lang.get("Ready", viewer));
        }
    }

    public static class ShowNotReadyIcon extends DecorationIcon {
        public ShowNotReadyIcon(@NotNull ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
            PlayerHeadUtils.applyPlayerHead(layout, trade.getPlayer(perspective.flip()), trade.getNames()[perspective.flip().id()]);
            return layout.setName("§7" + Lang.get("Status", viewer) + ": §c" + Lang.get("Not_Ready", viewer));
        }
    }
}
