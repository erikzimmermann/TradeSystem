package de.codingair.tradesystem.spigot.extras.external.essentials;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowEssentialsIcon extends ShowEconomyIcon {
    public ShowEssentialsIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Coins");
    }
}
