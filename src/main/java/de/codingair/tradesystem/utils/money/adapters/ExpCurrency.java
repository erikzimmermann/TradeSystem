package de.codingair.tradesystem.utils.money.adapters;

import de.codingair.tradesystem.utils.money.Adapter;
import org.bukkit.entity.Player;

public class ExpCurrency implements Adapter {
    @Override
    public synchronized double getMoney(Player player) {
        return player.getLevel() + player.getExp();
    }

    @Override
    public synchronized void withdraw(Player player, double amount) {
        double remainingExp = Math.max(player.getLevel() + player.getExp() - amount, 0);

        int level = (int) remainingExp;
        float exp = (float) (remainingExp - level);

        player.setLevel(level);
        player.setExp(exp);
    }

    @Override
    public synchronized void deposit(Player player, double amount) {
        double remainingExp = player.getLevel() + player.getExp() + amount;

        int level = (int) remainingExp;
        float exp = (float) (remainingExp - level);

        player.setLevel(level);
        player.setExp(exp);
    }
}
