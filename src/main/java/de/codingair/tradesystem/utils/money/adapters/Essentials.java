package de.codingair.tradesystem.utils.money.adapters;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import de.codingair.tradesystem.utils.money.Adapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class Essentials implements Adapter {
    @Override
    public double getMoney(Player player) {
        if(check(player)) return 0;

        try {
            return Economy.getMoneyExact(player.getName()).doubleValue();
        } catch(UserDoesNotExistException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void withdraw(Player player, double amount) {
        if(check(player)) return;

        try {
            Economy.substract(player.getName(), new BigDecimal(amount));
        } catch(UserDoesNotExistException | NoLoanPermittedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deposit(Player player, double amount) {
        if(check(player)) return;

        try {
            Economy.add(player.getName(), new BigDecimal(amount));
        } catch(UserDoesNotExistException | NoLoanPermittedException e) {
            e.printStackTrace();
        }
    }

    private boolean check(Player player) {
        if(!Bukkit.getPluginManager().isPluginEnabled("Essentials")) return true;

        if(!Economy.playerExists(player.getName())) Economy.createNPC(player.getName());
        return false;
    }
}
