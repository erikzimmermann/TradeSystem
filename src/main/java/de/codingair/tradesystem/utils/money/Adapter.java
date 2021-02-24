package de.codingair.tradesystem.utils.money;

import org.bukkit.entity.Player;

public interface Adapter {
    double getMoney(Player player);

    void withdraw(Player player, double amount);

    void deposit(Player player, double amount);

    boolean valid();
}
