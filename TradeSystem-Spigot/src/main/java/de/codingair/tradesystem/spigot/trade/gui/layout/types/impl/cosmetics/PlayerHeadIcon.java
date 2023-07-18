package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.cosmetics;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.DecorationIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerHeadIcon extends DecorationIcon {
    public PlayerHeadIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        Player player = trade.getPlayer(perspective);
        if (player == null) throw new IllegalStateException("Player is null!");

        return super.prepareItemStack(layout, trade, perspective, viewer)
                .setType(XMaterial.PLAYER_HEAD)
                .setSkullId(player);
    }
}
