package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowExpLevelIcon extends ShowEconomyIcon {
    public ShowExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Levels");
    }
}
