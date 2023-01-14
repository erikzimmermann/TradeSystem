package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExpLevelIcon extends EconomyIcon<ShowExpLevelIcon> {
    public ExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Level", "Levels", TradeLogMessages.PAYED_EXP_LEVELS, TradeLogMessages.RECEIVED_EXP_LEVELS, false);
    }

    @Override
    public Class<ShowExpLevelIcon> getTargetClass() {
        return ShowExpLevelIcon.class;
    }

    @Override
    protected double getBalance(Player player) {
        return player.getLevel();
    }

    @Override
    protected void withdraw(Player player, double value) {
        player.setLevel((int) (player.getLevel() - value));
    }

    @Override
    protected void deposit(Player player, double value) {
        player.setLevel((int) (player.getLevel() + value));
    }
}
