package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.cosmetics;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerHeadUtils {

    public static void applyPlayerHead(@NotNull ItemBuilder builder, @NotNull Player player) {
        applyPlayerHead(builder, player, player.getName());
    }

    public static void applyPlayerHead(@NotNull ItemBuilder builder, @Nullable Player player, @NotNull String name) {
        if (builder.getType() == Material.PLAYER_HEAD) {
            if (player != null) builder.setSkullId(player);
            else builder.setSkullId(TradeSystem.proxy().getSkin(name));
        }
    }

}
