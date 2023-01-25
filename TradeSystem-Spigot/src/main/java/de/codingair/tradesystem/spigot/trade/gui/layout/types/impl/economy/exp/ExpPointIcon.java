package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.function.Function;

public class ExpPointIcon extends EconomyIcon<ShowExpPointIcon> {
    public ExpPointIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Exp_Point", "Exp_Points", TradeLogMessages.PAYED_EXP_POINTS, TradeLogMessages.RECEIVED_EXP_POINTS, false);
    }

    @Override
    public Class<ShowExpPointIcon> getTargetClass() {
        return ShowExpPointIcon.class;
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        //This causes issues when directly setting the level before.
        //Unfortunately, there is no other easy way to get the total experience points.
        return BigDecimal.valueOf(player.getTotalExperience());
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        player.giveExp(-value.intValue());
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        player.giveExp(value.intValue());
    }

    @Override
    protected @NotNull Function<BigDecimal, BigDecimal> getMaxSupportedValue() {
        return EconomySupportType.INTEGER;
    }
}
