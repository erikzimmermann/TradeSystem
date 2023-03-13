package de.codingair.tradesystem.spigot.extras.external.tokenmanager;

import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import me.realized.tokenmanager.api.TokenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.function.Function;

public class TokenIcon extends EconomyIcon<ShowTokenIcon> {
    public TokenIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Token", "Tokens", false);
    }

    @Override
    public Class<ShowTokenIcon> getTargetClass() {
        return ShowTokenIcon.class;
    }

    private @NotNull TokenManager getTokenManager() {
        TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
        assert tokenManager != null;
        return tokenManager;
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        return BigDecimal.valueOf(getTokenManager().getTokens(player).orElse(0));
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        getTokenManager().removeTokens(player, value.longValue());
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        getTokenManager().addTokens(player, value.longValue());
    }

    @Override
    protected @NotNull Function<BigDecimal, BigDecimal> getMaxSupportedValue() {
        return EconomySupportType.LONG;
    }
}
