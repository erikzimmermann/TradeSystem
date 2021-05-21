package de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.TradeLayout;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.utils.ShowIcon;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowExpIcon extends ShowIcon implements Transition.Consumer<Integer> {
    private int value = 0;

    public ShowExpIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        String ending = (value == 1 ? Lang.get("Coin", player) : Lang.get("Coins", player));
        layout.setName("§e" + Lang.get("Money_Amount", player) + ": §7" + TradeSystem.getInstance().getTradeManager().makeAmountFancy(value) + " " + ending);
        if (value > 0) layout.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

        return layout;
    }

    @Override
    public @NotNull ItemStack getEditorIcon(@NotNull TradeLayout layout, @NotNull Player player) {
        return null;
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
