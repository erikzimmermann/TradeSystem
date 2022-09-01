package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.money.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import net.ess3.api.MaxMoneyException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Optional;

public class EssentialsIcon extends EconomyIcon<ShowEssentialsIcon> {
    public EssentialsIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Coin", "Coins", TradeLogMessages.PAYED_MONEY, TradeLogMessages.RECEIVED_MONEY, true);
    }

    @Override
    public Class<ShowEssentialsIcon> getTargetClass() {
        return ShowEssentialsIcon.class;
    }

    @Override
    public double getPlayerValue(Player player) {
        check(player);

        try {
            return Economy.getMoneyExact(player.getUniqueId()).doubleValue();
        } catch (UserDoesNotExistException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void withdraw(Player player, double value) {
        check(player);

        try {
            Economy.subtract(player.getUniqueId(), new BigDecimal(value));
        } catch (UserDoesNotExistException | NoLoanPermittedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deposit(Player player, double value) {
        check(player);

        try {
            Economy.add(player.getUniqueId(), new BigDecimal(value));
        } catch (UserDoesNotExistException | NoLoanPermittedException e) {
            e.printStackTrace();
        }
    }

    private void check(Player player) {
        if (!Economy.playerExists(player.getUniqueId())) Economy.createNPC(player.getName());
    }

    @Override
    protected @NotNull Optional<Double> getLimitOf(@NotNull Player player) {
        Essentials ess = (Essentials) Essentials.getProvidingPlugin(Essentials.class);
        BigDecimal max = ess.getSettings().getMaxMoney();
        return max == null ? Optional.empty() : Optional.of(max.doubleValue() - 1);  // -1 because of the rounding error
    }

    @Override
    protected @NotNull Optional<Double> getBalanceOf(@NotNull Player player) {
        try {
            BigDecimal balance = Economy.getMoneyExact(player.getUniqueId());
            return balance == null ? Optional.empty() : Optional.of(balance.doubleValue());
        } catch (UserDoesNotExistException e) {
            return Optional.empty();
        }
    }
}
