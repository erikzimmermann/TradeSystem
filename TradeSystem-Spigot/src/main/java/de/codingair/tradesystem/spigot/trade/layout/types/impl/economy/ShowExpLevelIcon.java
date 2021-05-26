package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.layout.types.utils.ShowIcon;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowExpLevelIcon extends ShowIcon implements Transition.Consumer<Integer> {
    private int value = 0;

    public ShowExpLevelIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public boolean isClickable(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        return false;
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        layout.setName("§e" + Lang.get("Levels", player) + ": §7" + TradeSystem.getInstance().getTradeManager().makeAmountFancy(value));
        if (value > 0) layout.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

        return layout;
    }

    @Override
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        return FinishResult.PASS;
    }

    @Override
    public void onFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        //we do this in ExpIcon.class
    }

    @Override
    public void applyTransition(Integer value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
