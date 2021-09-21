package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.tokenmanager;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowTokenIcon extends ShowEconomyIcon {
    public ShowTokenIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Tokens");
    }
}
