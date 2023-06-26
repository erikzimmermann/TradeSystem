package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.cosmetics;

import de.codingair.codingapi.player.data.GameProfileUtils;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.DecorationIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OtherPlayerHeadIcon extends DecorationIcon {
    public OtherPlayerHeadIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        String skinId;
        if (other != null) skinId = GameProfileUtils.extractSkinId(other);
        else skinId = TradeSystem.proxy().getSkin(othersName);

        return super.prepareItemStack(layout, trade, player, other, othersName)
                .setType(XMaterial.PLAYER_HEAD)
                .setSkullId(skinId);
    }
}
