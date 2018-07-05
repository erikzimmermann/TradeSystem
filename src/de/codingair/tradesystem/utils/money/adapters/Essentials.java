package de.codingair.tradesystem.utils.money.adapters;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import de.codingair.tradesystem.utils.money.Adapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Essentials implements Adapter {
    @Override
    public double getMoney(Player player) {
        if(!Bukkit.getPluginManager().isPluginEnabled("Essentials")) return 0;

        try {
            return Economy.getMoney(player.getName());
        } catch(UserDoesNotExistException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void setMoney(Player player, double amount) {
        if(!Bukkit.getPluginManager().isPluginEnabled("Essentials")) return;

        try {
            Economy.setMoney(player.getDisplayName(), amount);
        } catch(UserDoesNotExistException | NoLoanPermittedException e) {
            e.printStackTrace();
        }
    }
}
