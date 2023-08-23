package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class ShowExpLevelIcon extends ShowEconomyIcon {
    public ShowExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Levels");
    }

    @Override
    protected @NotNull String makeString(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable BigDecimal value) {
        ExpLevelIcon ownOffer = (ExpLevelIcon) trade.getLayout()[perspective.id()].getIcon(getOriginClass());
        Player player = trade.getPlayer(perspective);
        if (player == null) throw new NullPointerException("Player is null!");

        // convert exp to level
        if (value != null) {
            double current = ExpLevelIcon.getTotalExp(player.getLevel() + player.getExp());
            double offered = ownOffer.getValue().doubleValue();
            value = ExpLevelIcon.expToLevel(current - offered, value);
        }

        return super.makeString(trade, perspective, viewer, value);
    }

    @Override
    public @NotNull Class<? extends TradeIcon> getOriginClass() {
        return ExpLevelIcon.class;
    }
}
