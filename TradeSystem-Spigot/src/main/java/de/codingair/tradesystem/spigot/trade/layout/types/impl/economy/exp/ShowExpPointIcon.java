package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.ShowEconomyIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowExpPointIcon extends ShowEconomyIcon {
    public ShowExpPointIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Exp_Points");
    }
}
