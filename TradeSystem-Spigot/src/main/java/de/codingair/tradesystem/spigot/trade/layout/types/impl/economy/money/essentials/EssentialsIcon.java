package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.money.essentials;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

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
        if (!com.earth2me.essentials.api.Economy.playerExists(player.getUniqueId())) com.earth2me.essentials.api.Economy.createNPC(player.getName());
    }
}
