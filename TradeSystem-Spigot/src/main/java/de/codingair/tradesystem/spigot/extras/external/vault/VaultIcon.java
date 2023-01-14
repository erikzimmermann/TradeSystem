package de.codingair.tradesystem.spigot.extras.external.vault;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class VaultIcon extends EconomyIcon<ShowVaultIcon> {
    public VaultIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Coin", "Coins", TradeLogMessages.PAYED_MONEY, TradeLogMessages.RECEIVED_MONEY, true);
    }

    @Override
    public Class<ShowVaultIcon> getTargetClass() {
        return ShowVaultIcon.class;
    }

    @Override
    protected double getBalance(Player player) {
        return getEconomy().getBalance(player);
    }

    @Override
    protected void withdraw(Player player, double value) {
        getEconomy().withdrawPlayer(player, value);
    }

    @Override
    protected void deposit(Player player, double value) {
        getEconomy().depositPlayer(player, value);
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) return economyProvider.getProvider();

        throw new IllegalStateException("Vault is not enabled properly.");
    }
}
