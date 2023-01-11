package de.codingair.tradesystem.spigot.extras.external.playerpoints;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    public double getPlayerValue(Player player) {
        return api().look(player.getUniqueId());
    }

    @Override
    public void withdraw(Player player, double value) {
        api().take(player.getUniqueId(), (int) value);
    }

    @Override
    public void deposit(Player player, double value) {
        api().give(player.getUniqueId(), (int) value);
    }
}
