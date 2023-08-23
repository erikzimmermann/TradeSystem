package de.codingair.tradesystem.spigot.extras.external.playerpoints;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowPlayerPointsIcon extends ShowEconomyIcon {
    public ShowPlayerPointsIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "PlayerPoints");
    }

    @Override
    public @NotNull Class<? extends TradeIcon> getOriginClass() {
        return PlayerPointsIcon.class;
    }
}
