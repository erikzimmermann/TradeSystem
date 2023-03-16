package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.extras.external.TypeCap;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class ExpPointIcon extends EconomyIcon<ShowExpPointIcon> {
    public ExpPointIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Exp_Point", "Exp_Points", false);
    }

    @Override
    public Class<ShowExpPointIcon> getTargetClass() {
        return ShowExpPointIcon.class;
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        int totalExp = (int) ExpLevelIcon.getTotalExp(player.getLevel() + player.getExp());
        return BigDecimal.valueOf(totalExp);
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
    protected @NotNull TypeCap getMaxSupportedValue() {
        return EconomySupportType.INTEGER;
    }
}
