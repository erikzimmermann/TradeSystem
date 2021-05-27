package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.layout.types.utils.ShowIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.utils.SimpleShowIcon;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowExpPointIcon extends SimpleShowIcon implements Transition.Consumer<Integer> {
    private int value = 0;

    public ShowExpPointIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        layout.setName("§e" + Lang.get("Levels", player) + ": §7" + TradeSystem.getInstance().getTradeManager().makeAmountFancy(value));
        if (value > 0) layout.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

        return layout;
    }

    @Override
    public void applyTransition(Integer value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
