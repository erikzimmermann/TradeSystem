package de.codingair.tradesystem.spigot.utils.money.adapters;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import de.codingair.tradesystem.spigot.utils.money.Adapter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class Essentials implements Adapter {

    @Override
    public double getMoney(Player player) {
        check(player);

        try {
            return Economy.getMoneyExact(player.getUniqueId()).doubleValue();
        } catch (UserDoesNotExistException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void withdraw(Player player, double amount) {
        check(player);

        try {
            Economy.subtract(player.getUniqueId(), new BigDecimal(amount));
        } catch (UserDoesNotExistException | NoLoanPermittedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deposit(Player player, double amount) {
        check(player);

        try {
            Economy.add(player.getUniqueId(), new BigDecimal(amount));
        } catch (UserDoesNotExistException | NoLoanPermittedException e) {
            e.printStackTrace();
        }
    }

    private void check(Player player) {
        if (!Economy.playerExists(player.getUniqueId())) Economy.createNPC(player.getName());
    }

    @Override
    public boolean valid() {
        return true;
    }
}
