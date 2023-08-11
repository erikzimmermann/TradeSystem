package de.codingair.tradesystem.spigot.extras.external.vault;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowVaultIcon extends ShowEconomyIcon {
    public ShowVaultIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Coins");
    }

    @Override
    public @NotNull Class<? extends TradeIcon> getOriginClass() {
        return VaultIcon.class;
    }
}
