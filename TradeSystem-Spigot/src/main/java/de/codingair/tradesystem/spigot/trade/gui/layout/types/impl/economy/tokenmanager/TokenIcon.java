package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.tokenmanager;

import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import me.realized.tokenmanager.api.TokenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TokenIcon extends EconomyIcon<ShowTokenIcon> {
    public TokenIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Token", "Tokens", TradeLogMessages.PAYED_TOKENS, TradeLogMessages.RECEIVED_TOKENS, false);
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
    public double getPlayerValue(Player player) {
        return getTokenManager().getTokens(player).orElse(0);
    }

    @Override
    public void withdraw(Player player, double value) {
        getTokenManager().removeTokens(player, (long) value);
    }

    @Override
    public void deposit(Player player, double value) {
        getTokenManager().addTokens(player, (long) value);
    }
}
