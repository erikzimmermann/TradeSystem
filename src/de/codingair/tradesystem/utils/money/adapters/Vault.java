package de.codingair.tradesystem.utils.money.adapters;

import de.codingair.warpsystem.spigot.base.utils.money.Adapter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault implements Adapter {
    private Economy economy = null;

    public Vault() {
        if(Bukkit.getPluginManager().isPluginEnabled("Vault")) this.setupEconomy();
    }

    @Override
    public double getMoney(Player player) {
        if(!isReady()) return 0;
        return this.economy.getBalance(player);
    }

    @Override
    public void withdraw(Player player, double amount) {
        if(!isReady()) return;
        this.economy.withdrawPlayer(player, amount);
    }

    @Override
    public void deposit(Player player, double amount) {
        if(!isReady()) return;
        this.economy.depositPlayer(player, amount);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    @Override
    public boolean isReady() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault") && (economy != null || setupEconomy());
    }
}
