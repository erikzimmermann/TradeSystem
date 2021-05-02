package de.codingair.tradesystem.spigot.utils.money;

import org.bukkit.entity.Player;

public interface Adapter {
    double getMoney(Player player);

    void withdraw(Player player, double amount);

    void deposit(Player player, double amount);

    boolean valid();
}
