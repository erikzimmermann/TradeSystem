package de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.TradeLayout;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.SimpleTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CancelIcon extends SimpleTradeIcon {
    public CancelIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        layout.setName("§c" + Lang.get("Cancel_Trade", player));
        return layout;
    }

    @Override
    public @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Player player, @NotNull InventoryClickEvent event) {
        return IconResult.CANCEL;
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
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void serialize(@NotNull DataOutputStream out) throws IOException {
    }

    @Override
    public void deserialize(@NotNull DataInputStream in) throws IOException {
    }
}
