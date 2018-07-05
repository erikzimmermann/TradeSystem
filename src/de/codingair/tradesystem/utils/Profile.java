package de.codingair.tradesystem.utils;

import de.codingair.tradesystem.utils.money.AdapterType;
import org.bukkit.entity.Player;

public class Profile {
    private Player player;

    public Profile(Player player) {
        this.player = player;
    }

    public int getMoney() {
        if(!AdapterType.canEnable()) {
            return 0;
        }

        return (int) AdapterType.getActive().getMoney(this.player);
    }

    public void setMoney(double money) {
        if(!AdapterType.canEnable()) {
            return;
        }

        AdapterType.getActive().setMoney(this.player, money);
    }
}
