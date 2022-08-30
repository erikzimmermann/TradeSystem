package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.utils.SimpleShowIcon;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowEconomyIcon extends SimpleShowIcon<Double> implements Transition.Consumer<Double> {
    private final String namePlural;
    private Double value = 0D;

    public ShowEconomyIcon(@NotNull ItemStack itemStack, @NotNull String namePlural) {
        super(itemStack);
        this.namePlural = namePlural;
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        layout.setName("§e" + getName(player) + ": §7" + TradeSystem.man().getMoneyPattern().format(value));
        if (value > 0) layout.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

        return layout;
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
    public void applyTransition(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }
}
