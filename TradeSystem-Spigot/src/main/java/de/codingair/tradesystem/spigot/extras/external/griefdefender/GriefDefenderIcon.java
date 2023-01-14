package de.codingair.tradesystem.spigot.extras.external.griefdefender;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.permission.option.Options;
import com.griefdefender.lib.geantyref.TypeToken;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.Type;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GriefDefenderIcon extends EconomyIcon<ShowGriefDefenderIcon> {
    public GriefDefenderIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "ClaimBlock", "ClaimBlocks", TradeLogMessages.PAYED_CLAIM_BLOCKS, TradeLogMessages.RECEIVED_CLAIM_BLOCKS, false);
    }

    @Override
    public Class<ShowGriefDefenderIcon> getTargetClass() {
        return ShowGriefDefenderIcon.class;
    }

    @Override
    protected double getBalance(Player player) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return 0;

        return user.getPlayerData().getRemainingClaimBlocks();
    }

    @Override
    protected void withdraw(Player player, double value) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return;

        user.getPlayerData().setBonusClaimBlocks(user.getPlayerData().getBonusClaimBlocks() - (int) value);
    }

    @Override
    protected void deposit(Player player, double value) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return;

        user.getPlayerData().setBonusClaimBlocks(user.getPlayerData().getBonusClaimBlocks() + (int) value);
    }

    @Override
    protected @NotNull Optional<Double> getBalanceLimit(@NotNull Player player) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return Optional.empty();

        // use the permission manager directly to check if a value is set
        Integer max = GriefDefender.getPermissionManager().getOptionValue(TypeToken.get(Integer.class), user, Options.MAX_BONUS_BLOCKS);
        if (max == null) return Optional.empty();
        return Optional.of(max.doubleValue());
    }
}
