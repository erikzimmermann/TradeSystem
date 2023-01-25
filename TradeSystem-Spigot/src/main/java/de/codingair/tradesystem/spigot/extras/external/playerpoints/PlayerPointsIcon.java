package de.codingair.tradesystem.spigot.extras.external.playerpoints;

import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.function.Function;

public class PlayerPointsIcon extends EconomyIcon<ShowPlayerPointsIcon> {
    public PlayerPointsIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "PlayerPoint", "PlayerPoints", TradeLogMessages.PAYED_PLAYER_POINTS, TradeLogMessages.RECEIVED_PLAYER_POINTS, false);
    }

    @Override
    public Class<ShowPlayerPointsIcon> getTargetClass() {
        return ShowPlayerPointsIcon.class;
    }

    private PlayerPointsAPI api() {
        return PlayerPoints.getInstance().getAPI();
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        return BigDecimal.valueOf(api().look(player.getUniqueId()));
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        api().take(player.getUniqueId(), value.intValue());
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        api().give(player.getUniqueId(), value.intValue());
    }

    @Override
    protected @NotNull Function<BigDecimal, BigDecimal> getMaxSupportedValue() {
        return EconomySupportType.INTEGER;
    }
}
