package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.exp;

import de.codingair.tradesystem.spigot.extras.external.EconomySupportType;
import de.codingair.tradesystem.spigot.extras.external.TypeCap;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * A trade icon to trade exp levels. Due to the exponential experience formula, some calculations must be done before exchanging exp levels.
 * <p>
 * For simplified value usage, user inputs are calculated into experience and vice versa for displaying the current value. Therefore, the trading player might see a different value than the other.
 */
public class ExpLevelIcon extends EconomyIcon<ShowExpLevelIcon> {
    public ExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack, "Level", "Levels", true);
    }

    @Override
    public Class<ShowExpLevelIcon> getTargetClass() {
        return ShowExpLevelIcon.class;
    }

    @Override
    protected @NotNull BigDecimal getBalance(Player player) {
        double totalExp = ExpLevelIcon.getTotalExp(player.getLevel() + player.getExp());
        return BigDecimal.valueOf(totalExp);
    }

    @Override
    protected void withdraw(Player player, @NotNull BigDecimal value) {
        player.giveExp(-value.intValue());
    }

    @Override
    protected void deposit(Player player, @NotNull BigDecimal value) {
        player.giveExp(value.intValue());
    }

    @Override
    protected @NotNull TypeCap getMaxSupportedValue() {
        return EconomySupportType.DOUBLE;
    }

    @Override
    public IconResult processInput(@NotNull Trade trade, @NotNull Player player, @Nullable BigDecimal input, @NotNull String origin) {
        if (input != null) {
            // convert level to exp
            input = levelToExp(player, input);
        }

        return super.processInput(trade, player, input, origin);
    }

    @Override
    public @NotNull String makeString(@NotNull Player player, @Nullable BigDecimal current) {
        if (current != null) {
            // convert exp to level

            // decrease own balance first to compute the correct level
            double totalExp = getTotalExp(player.getLevel() + player.getExp());
            current = expToLevel(totalExp - current.doubleValue(), current);
        }

        return super.makeString(player, current);
    }

    static double getTotalExp(double level) {
        if (level < 17) return Math.pow(level, 2) + 6 * level;
        else if (level < 32) return 2.5 * Math.pow(level, 2) - 40.5 * level + 360;
        else return 4.5 * Math.pow(level, 2) - 162.5 * level + 2220;
    }

    private static double getTotalLevel(double exp) {
        if (exp < 353) return Math.sqrt(exp + 9) - 3;
        else if (exp < 1508) return 8.1 + Math.sqrt(0.4 * (exp - 195.975));
        else return 325 / 18D + Math.sqrt(2 / 9D * (exp - 54215 / 72D));
    }

    @NotNull
    private static BigDecimal levelToExp(@NotNull Player player, @NotNull BigDecimal input) {
        double currentExp = getTotalExp(player.getLevel() + player.getExp());
        double targetExp = getTotalExp(player.getLevel() + player.getExp() - input.doubleValue());
        double diff = currentExp - targetExp;

        return BigDecimal.valueOf(diff);
    }

    @NotNull
    static BigDecimal expToLevel(double currentExp, @NotNull BigDecimal current) {
        double targetExp = currentExp + current.doubleValue();
        double targetLevel = getTotalLevel(targetExp) - getTotalLevel(currentExp);

        return BigDecimal.valueOf(targetLevel);
    }
}
