package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.function.Function;

public class ExpLevelIcon extends EconomyIcon<ShowExpLevelIcon> {
    public ExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Level", "Levels", false);
    }

    @Override
    public Class<ShowExpLevelIcon> getTargetClass() {
        return ShowExpLevelIcon.class;
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        return BigDecimal.valueOf(player.getLevel());
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        player.setLevel(player.getLevel() - value.intValue());
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        player.setLevel(player.getLevel() + value.intValue());
    }

    @Override
    protected @NotNull Function<BigDecimal, BigDecimal> getMaxSupportedValue() {
        return EconomySupportType.INTEGER;
    }
}
