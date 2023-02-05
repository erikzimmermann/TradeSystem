package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.utils.SimpleShowIcon;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class ShowEconomyIcon extends SimpleShowIcon<BigDecimal> implements Transition.Consumer<BigDecimal> {
    private final String namePlural;
    private BigDecimal value = BigDecimal.ZERO;

    public ShowEconomyIcon(@NotNull ItemStack itemStack, @NotNull String namePlural) {
        super(itemStack);
        this.namePlural = namePlural;
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        layout.setName("§e" + getName(player) + ": §7" + makeString(player, value));
        if (value.signum() > 0) layout.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

        return layout;
    }

    @NotNull
    protected String makeString(@NotNull Player player, @NotNull BigDecimal value) {
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
