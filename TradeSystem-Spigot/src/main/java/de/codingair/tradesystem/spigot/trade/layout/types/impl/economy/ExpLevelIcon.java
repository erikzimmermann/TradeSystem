package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExpLevelIcon extends EconomyIcon<ShowExpLevelIcon> {
    public ExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Level", "Levels", TradeLogMessages.PAYED_EXP_LEVELS, TradeLogMessages.RECEIVED_EXP_LEVELS);
    }

    @Override
    public Class<ShowExpLevelIcon> getTargetClass() {
        return ShowExpLevelIcon.class;
    }

    @Override
    public int getPlayerValue(Player player) {
        return player.getLevel();
    }

    @Override
    public void setPlayerValue(Player player, int value) {
        player.setLevel(value);
    }
}
