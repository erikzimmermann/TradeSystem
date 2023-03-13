package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class ShowExpLevelIcon extends ShowEconomyIcon {
    public ShowExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Levels");
    }

    @Override
    protected @NotNull String makeString(@NotNull Player player, @NotNull BigDecimal value) {
        // convert exp to level
        value = ExpLevelIcon.expToLevel(ExpLevelIcon.getTotalExp(player.getLevel() + player.getExp()), value);
        return super.makeString(player, value);
    }
}
