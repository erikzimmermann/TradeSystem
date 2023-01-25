package de.codingair.tradesystem.spigot.extras.external.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import net.ess3.api.MaxMoneyException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

public class EssentialsIcon extends EconomyIcon<ShowEssentialsIcon> {
    public EssentialsIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Coin", "Coins", true);
    }

    @Override
    public Class<ShowEssentialsIcon> getTargetClass() {
        return ShowEssentialsIcon.class;
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        check(player);

        try {
            return Economy.getMoneyExact(player.getUniqueId());
        } catch (UserDoesNotExistException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        check(player);

        try {
            Economy.subtract(player.getUniqueId(), value);
        } catch (UserDoesNotExistException | NoLoanPermittedException | MaxMoneyException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        check(player);

        try {
            Economy.add(player.getUniqueId(), value);
        } catch (UserDoesNotExistException | NoLoanPermittedException | MaxMoneyException e) {
            e.printStackTrace();
        }
    }

    private void check(Player player) {
        if (!Economy.playerExists(player.getUniqueId())) Economy.createNPC(player.getName());
    }

    @Override
    protected @NotNull Optional<BigDecimal> getBalanceLimit(@NotNull Player player) {
        Essentials ess = (Essentials) Essentials.getProvidingPlugin(Essentials.class);
        BigDecimal max = ess.getSettings().getMaxMoney();
        return max == null ? Optional.empty() : Optional.of(max.subtract(BigDecimal.ONE));  // -1 because of the rounding error
    }

    @Override
    protected @NotNull Function<BigDecimal, BigDecimal> getMaxSupportedValue() {
        return EconomySupportType.BIG_DECIMAL;
    }
}
