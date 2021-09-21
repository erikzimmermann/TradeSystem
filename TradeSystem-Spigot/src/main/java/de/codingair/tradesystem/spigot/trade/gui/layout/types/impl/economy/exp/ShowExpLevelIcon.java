package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowExpLevelIcon extends ShowEconomyIcon {
    public ShowExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Levels");
    }
}
