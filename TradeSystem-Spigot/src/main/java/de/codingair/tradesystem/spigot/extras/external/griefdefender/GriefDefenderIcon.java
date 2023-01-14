package de.codingair.tradesystem.spigot.extras.external.griefdefender;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import me.realized.tokenmanager.api.TokenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.html.Option;
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
    public double getPlayerValue(Player player) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return 0;

        return user.getPlayerData().getBonusClaimBlocks();
    }

    @Override
    public void withdraw(Player player, double value) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return;

        user.getPlayerData().setBonusClaimBlocks(user.getPlayerData().getBonusClaimBlocks() - (int) value);
    }

    @Override
    public void deposit(Player player, double value) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return;

        user.getPlayerData().setBonusClaimBlocks(user.getPlayerData().getBonusClaimBlocks() + (int) value);
    }

    @Override
    protected @NotNull Optional<Double> getLimitOf(@NotNull Player player) {
        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user == null) return Optional.empty();

        Number n = user.getPlayerData().getMaxBonusClaimBlocks();
        return Optional.of(n.doubleValue());
    }
}
