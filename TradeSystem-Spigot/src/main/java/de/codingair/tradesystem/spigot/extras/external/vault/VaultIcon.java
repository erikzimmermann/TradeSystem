package de.codingair.tradesystem.spigot.extras.external.vault;

import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.extras.external.TypeCap;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class VaultIcon extends EconomyIcon<ShowVaultIcon> {
    public VaultIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Coin", "Coins", true);
    }

    @Override
    public Class<ShowVaultIcon> getTargetClass() {
        return ShowVaultIcon.class;
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        return BigDecimal.valueOf(getEconomy().getBalance(player));
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        getEconomy().withdrawPlayer(player, value.doubleValue());
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        getEconomy().depositPlayer(player, value.doubleValue());
    }

    @Override
    protected @NotNull TypeCap getMaxSupportedValue() {
        return EconomySupportType.DOUBLE;
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) return economyProvider.getProvider();

        throw new IllegalStateException("Vault is not enabled properly.");
    }
}
