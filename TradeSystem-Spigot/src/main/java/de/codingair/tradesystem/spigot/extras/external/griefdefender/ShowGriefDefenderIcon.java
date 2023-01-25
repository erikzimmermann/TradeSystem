package de.codingair.tradesystem.spigot.extras.external.griefdefender;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowGriefDefenderIcon extends ShowEconomyIcon {
    public ShowGriefDefenderIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "ClaimBlocks");
    }
}
