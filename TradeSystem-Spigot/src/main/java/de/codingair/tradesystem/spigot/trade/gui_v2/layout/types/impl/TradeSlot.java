package de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl;

import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.TradeLayout;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.feedback.FinishResult;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TradeSlot implements TradeIcon {
    @Override
    public @NotNull Button getButton(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        throw new IllegalStateException("The TradeSlot.class is just a placeholder as button.");
    }

    @Override
    public @NotNull ItemStack getEditorIcon(@NotNull TradeLayout layout, @NotNull Player player) {
        int amount = 25 - layout.getPattern().getAmountOf(TradeSlotOther.class);

        return new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE)
                .setAmount(amount)
                .addEnchantment(Enchantment.DAMAGE_ALL, 1)
                .setHideName(true)
                .setHideEnchantments(true)
                .getItem();
    }

    @Override
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        return FinishResult.PASS;
    }

    @Override
    public void onFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void serialize(@NotNull DataOutputStream out) throws IOException {
        //will be handled separately
    }

    @Override
    public void deserialize(@NotNull DataInputStream in) throws IOException {
        //will be handled separately
    }
}
