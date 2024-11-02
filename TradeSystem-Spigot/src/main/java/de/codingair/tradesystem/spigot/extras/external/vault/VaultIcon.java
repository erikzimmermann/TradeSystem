package de.codingair.tradesystem.spigot.extras.external.vault;

import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.extras.external.TypeCap;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class VaultIcon extends EconomyIcon<ShowVaultIcon> {
    public VaultIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Coin", "Coins", true);
    }

    @Override
    public Class<ShowVaultIcon> getTargetClass() {
        return ShowVaultIcon.class;
    }

    @Override
    protected @NotNull BigDecimal getBalance(@NotNull Player player) {
        return BigDecimal.valueOf(getEconomy().getBalance(player));
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        getEconomy().withdrawPlayer(player, value.doubleValue());
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        getEconomy().depositPlayer(player, value.doubleValue());
    }

    @Override
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
        FinishResult result = super.tryFinish(trade, perspective, viewer, initiationServer);

        Player player = trade.getPlayer(perspective);
        if (player == null) {
            // might be a proxy trade
            return result;
        }

        // fix exceeding balance limits by testing deposit first
        double value = getOverallDifference(trade, perspective).doubleValue();
        if (value < 0) {
            // withdrawal -> check if possible
            EconomyResponse response = getEconomy().withdrawPlayer(player, -value);
            if (!response.transactionSuccess()) {
                // got an error -> cancel trade finish
                return FinishResult.ERROR_ECONOMY;
            }

            // deposit money again
            getEconomy().depositPlayer(player, -value);
        } else if (value > 0) {
            // deposit -> check if possible
            EconomyResponse response = getEconomy().depositPlayer(player, value);
            if (!response.transactionSuccess()) {
                // got an error -> cancel trade finish
                return FinishResult.ERROR_ECONOMY;
            }

            // withdraw money again
            getEconomy().withdrawPlayer(player, value);
        }

        return result;
    }

    @Override
    protected @NotNull TypeCap getMaxSupportedValue() {
        return EconomySupportType.DOUBLE;
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) return economyProvider.getProvider();

        throw new IllegalStateException("Vault is not enabled properly.");
    }
}
