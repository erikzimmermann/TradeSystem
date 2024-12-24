package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.utils.SimpleShowIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public abstract class ShowEconomyIcon extends SimpleShowIcon<BigDecimal> implements Transition.Consumer<BigDecimal> {
    private final String namePlural;
    private BigDecimal value = BigDecimal.ZERO;

    public ShowEconomyIcon(@NotNull ItemStack itemStack, @NotNull String namePlural) {
        super(itemStack);
        this.namePlural = namePlural;
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        layout.setName("§e" + getName(viewer) + ": §7" + makeString(trade, perspective, viewer, value));
        if (value.signum() > 0) layout.addEnchantmentEffect();

        return layout;
    }

    /**
     * @param trade       The trade instance.
     * @param perspective The perspective of the trading player.
     * @param viewer      The player that is viewing the trade GUI. This is not necessarily the trading player.
     * @param value       The value to be converted.
     * @return A {@link String} which will be used to display the current value.
     */
    protected @NotNull String makeString(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable BigDecimal value) {
        return EconomyIcon.makeFancyString(value, true);
    }

    @NotNull
    private String getName(@NotNull Player player) {
        try {
            return Lang.get(namePlural, player);
        } catch (NullPointerException ex) {
            return namePlural;
        }
    }

    @Override
    public void applyTransition(@NotNull BigDecimal value) {
        this.value = value;
    }

    @NotNull
    public BigDecimal getValue() {
        return value;
    }
}
