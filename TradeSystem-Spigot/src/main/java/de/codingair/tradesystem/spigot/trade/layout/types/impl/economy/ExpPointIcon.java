package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExpPointIcon extends EconomyIcon<ShowExpPointIcon> {
    public ExpPointIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Exp_Point", "Exp_Points", TradeLogMessages.PAYED_EXP_POINTS, TradeLogMessages.RECEIVED_EXP_POINTS);
    }

    @Override
    public Class<ShowExpPointIcon> getTargetClass() {
        return ShowExpPointIcon.class;
    }

    @Override
    public int getPlayerValue(Player player) {
        return player.getExpToLevel();
    }

    @Override
    public void setPlayerValue(Player player, int value) {
        player.setTotalExperience(value);
    }
}
