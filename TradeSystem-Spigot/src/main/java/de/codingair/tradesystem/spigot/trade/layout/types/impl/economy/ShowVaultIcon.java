package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowVaultIcon extends ShowEconomyIcon {
    public ShowVaultIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Coins");
    }
}
