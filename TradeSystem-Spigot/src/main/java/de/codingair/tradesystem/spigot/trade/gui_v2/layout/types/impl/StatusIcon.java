package de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.TradeLayout;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.SimpleTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatusIcon extends MultiTradeIcon {
    public StatusIcon(ItemStack[] items) {
        super(new CannotReadyIcon(items[0]), new NotReadyIcon(items[1]), new ReadyIcon(items[2]));
    }

    @Override
    public @NotNull ItemStack getEditorIcon(@NotNull TradeLayout layout, @NotNull Player player) {
        //category icon?
        throw new IllegalStateException("Not implemented yet.");
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void serialize(@NotNull DataOutputStream out) throws IOException {
        //will be handled in the trade directly
    }

    @Override
    public void deserialize(@NotNull DataInputStream in) throws IOException {
        //will be handled in the trade directly
    }

    @Override
    public @NotNull TradeIcon currentTradeIcon(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        int id = trade.getId(player);

        if (TradeSystem.getInstance().getTradeManager().isTradeBoth()) {
            boolean canFinish = false;
            for (Integer slot : trade.getSlots()) {
                ItemStack item = trade.getGUIs()[id].getItem(slot);
                if (item != null && !item.getType().equals(Material.AIR)) canFinish = true;
            }

            if (!trade.getLayout()[id].areTradeIconsEmpty()) canFinish = true;
            if (!canFinish) return getIcon(CannotReadyIcon.class);
        }

        if (trade.getReady()[id]) return getIcon(ReadyIcon.class);
        else return getIcon(NotReadyIcon.class);
    }

    private static class ReadyIcon extends SimpleTradeIcon {
        public ReadyIcon(@NotNull ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
            layout.setName("§7" + Lang.get("Status", player) + ": §a" + Lang.get("Ready", player));
            layout.addLore("", "§7" + Lang.get("Wait_For_Other_Player", player));
            return layout;
        }

        @Override
        public @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Player player, @NotNull InventoryClickEvent event) {
            return IconResult.NOT_READY;
        }

        @Override
        public @NotNull ItemStack getEditorIcon(@NotNull TradeLayout layout, @NotNull Player player) {
            throw new IllegalStateException("Not implemented yet");
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

    private static class NotReadyIcon extends SimpleTradeIcon {
        public NotReadyIcon(@NotNull ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
            layout.setName("§7" + Lang.get("Status", player) + ": §c" + Lang.get("Not_Ready", player));
            return layout;
        }

        @Override
        public @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Player player, @NotNull InventoryClickEvent event) {
            return IconResult.READY;
        }

        @Override
        public @NotNull ItemStack getEditorIcon(@NotNull TradeLayout layout, @NotNull Player player) {
            throw new IllegalStateException("Not implemented yet");
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

    private static class CannotReadyIcon extends SimpleTradeIcon {
        public CannotReadyIcon(@NotNull ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
            layout.setText("§c" + Lang.get("Trade_Only_With_Objects", player), TextAlignment.LEFT, 150);
            return layout;
        }

        @Override
        public boolean isClickable(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
            return false;
        }

        @Override
        public @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Player player, @NotNull InventoryClickEvent event) {
            return IconResult.PASS;
        }

        @Override
        public @NotNull ItemStack getEditorIcon(@NotNull TradeLayout layout, @NotNull Player player) {
            throw new IllegalStateException("Not implemented yet");
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
}
