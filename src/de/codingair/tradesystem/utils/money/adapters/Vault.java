package de.codingair.tradesystem.utils.money.adapters;

import de.codingair.tradesystem.utils.money.Adapter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class Vault implements Adapter {
    private Economy economy = null;

    public Vault() {
        if(Bukkit.getPluginManager().isPluginEnabled("Vault")) this.setupEconomy();
    }

    @Override
    public double getMoney(Player player) {
        if(economy == null) return 0;
        return this.economy.bankBalance(player.getName()).balance;
    }

    @Override
    public void setMoney(Player player, double amount) {
        if(economy == null) return;

        double diff = amount - getMoney(player);
        if(diff > 0) this.economy.bankDeposit(player.getName(), amount);
        else if(diff < 0) this.economy.bankWithdraw(player.getName(), amount);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if(economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}
