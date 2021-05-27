package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExpPointIcon extends EconomyIcon<ShowExpPointIcon> {
    public ExpPointIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Exp_Point", "Exp_Points", TradeLogMessages.PAYED_EXP_POINTS, TradeLogMessages.RECEIVED_EXP_POINTS, false);
    }

    @Override
    public Class<ShowExpPointIcon> getTargetClass() {
        return ShowExpPointIcon.class;
    }

    @Override
    public double getPlayerValue(Player player) {
        //This causes issues when directly setting the level before.
        //Unfortunately, there is no other easy way to get the total experience points.
        return player.getTotalExperience();
    }

    @Override
    public void withdraw(Player player, double value) {
        player.giveExp((int) -value);
    }

    @Override
    public void deposit(Player player, double value) {
        player.giveExp((int) value);
    }
}
