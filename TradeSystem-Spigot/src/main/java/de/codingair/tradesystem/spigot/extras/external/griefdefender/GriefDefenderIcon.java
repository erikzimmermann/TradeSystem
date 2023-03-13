package de.codingair.tradesystem.spigot.extras.external.griefdefender;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.permission.option.Options;
import com.griefdefender.lib.geantyref.TypeToken;
import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

public class GriefDefenderIcon extends EconomyIcon<ShowGriefDefenderIcon> {
    public GriefDefenderIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "ClaimBlock", "ClaimBlocks", false);
    }

    @Override
    public Class<ShowGriefDefenderIcon> getTargetClass() {
        return ShowGriefDefenderIcon.class;
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return BigDecimal.ZERO;

        return BigDecimal.valueOf(user.getPlayerData().getRemainingClaimBlocks());
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return;

        user.getPlayerData().setBonusClaimBlocks(user.getPlayerData().getBonusClaimBlocks() - value.intValue());
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return;

        user.getPlayerData().setBonusClaimBlocks(user.getPlayerData().getBonusClaimBlocks() + value.intValue());
    }

    @Override
    protected @NotNull Optional<BigDecimal> getBalanceLimit(@NotNull Player player) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return Optional.empty();

        // use the permission manager directly to check if a value is set
        Integer max = GriefDefender.getPermissionManager().getOptionValue(TypeToken.get(Integer.class), user, Options.MAX_BONUS_BLOCKS);
        if (max == null) return Optional.empty();
        return Optional.of(BigDecimal.valueOf(max));
    }

    @Override
    protected @NotNull Function<BigDecimal, BigDecimal> getMaxSupportedValue() {
        return EconomySupportType.INTEGER;
    }
}
