package de.codingair.tradesystem.spigot.utils;

import de.codingair.tradesystem.spigot.utils.money.AdapterType;
import org.bukkit.entity.Player;

public class Profile {
    private final Player player;

    public Profile(Player player) {
        this.player = player;
    }

    public int getMoney() {
        if (!AdapterType.canEnable()) {
            return 0;
        }

        return (int) AdapterType.getActive().getMoney(this.player);
    }

    public void withdraw(double money) {
        if (!AdapterType.canEnable()) {
            return;
        }

        AdapterType.getActive().withdraw(this.player, money);
    }

    public void deposit(double money) {
        if (!AdapterType.canEnable()) {
            return;
        }

        AdapterType.getActive().deposit(this.player, money);
    }
}
