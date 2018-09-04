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
        return this.economy.getBalance(player);
    }

    @Override
    public void setMoney(Player player, double amount) {
        if(economy == null) return;

        double diff = amount - getMoney(player);
        if(diff > 0) this.economy.depositPlayer(player, diff);
        else if(diff < 0) this.economy.withdrawPlayer(player, -diff);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if(economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}
