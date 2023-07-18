package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.SimpleTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.cosmetics.PlayerHeadUtils;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatusIcon extends MultiTradeIcon {
    public StatusIcon(ItemStack[] items) {
        super(new CannotReadyIcon(items[0]), new NotReadyIcon(items[1]), new ReadyIcon(items[2]));
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
    public @NotNull TradeIcon currentTradeIcon(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        if (TradeSystem.getInstance().getTradeManager().isTradeBoth()) {
            boolean canFinish = false;
            for (Integer slot : trade.getSlots()) {
                ItemStack item = trade.getGUIs()[perspective.id()].getItem(slot);
                if (item != null && !item.getType().equals(Material.AIR)) canFinish = true;
            }

            if (!trade.getLayout()[perspective.id()].areTradeIconsEmpty()) canFinish = true;
            if (!canFinish) return getIcon(CannotReadyIcon.class);
        }

        if (trade.getReady()[perspective.id()]) return getIcon(ReadyIcon.class);
        else return getIcon(NotReadyIcon.class);
    }

    public static class ReadyIcon extends SimpleTradeIcon {
        public ReadyIcon(@NotNull ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
            PlayerHeadUtils.applyPlayerHead(layout, viewer);
            layout.setName("§7" + Lang.get("Status", viewer) + ": §a" + Lang.get("Ready", viewer));
            layout.addLore("", "§7" + Lang.get("Wait_For_Other_Player", viewer));
            return layout;
        }

        @Override
        public boolean isClickable(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
            return true;
        }

        @Override
        public @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @NotNull InventoryClickEvent event) {
            return IconResult.NOT_READY;
        }

        @Override
        public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
            return FinishResult.PASS;
        }

        @Override
        public void onFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
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

    public static class NotReadyIcon extends SimpleTradeIcon {
        public NotReadyIcon(@NotNull ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
            PlayerHeadUtils.applyPlayerHead(layout, viewer);
            layout.setName("§7" + Lang.get("Status", viewer) + ": §c" + Lang.get("Not_Ready", viewer));
            return layout;
        }

        @Override
        public boolean isClickable(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
            return true;
        }

        @Override
        public @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @NotNull InventoryClickEvent event) {
            return IconResult.READY;
        }

        @Override
        public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
            return FinishResult.PASS;
        }

        @Override
        public void onFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
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

    public static class CannotReadyIcon extends SimpleTradeIcon {
        public CannotReadyIcon(@NotNull ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
            PlayerHeadUtils.applyPlayerHead(layout, viewer);
            layout.setText("§c" + Lang.get("Trade_Only_With_Objects", viewer), TextAlignment.LEFT, 150);
            return layout;
        }

        @Override
        public boolean isClickable(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
            return false;
        }

        @Override
        public @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @NotNull InventoryClickEvent event) {
            return IconResult.PASS;
        }

        @Override
        public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
            return FinishResult.PASS;
        }

        @Override
        public void onFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
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
