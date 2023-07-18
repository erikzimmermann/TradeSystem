package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.packetmanagement.packets.impl.BooleanPacket;
import de.codingair.tradesystem.proxy.packets.*;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.transfer.utils.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ProxyTrade extends Trade {
    private final Player player;
    private final String other;
    private final UUID otherId;
    private ItemStack[] sent;
    private ItemStack[] received;
    private final ItemStack[] otherInventory = new ItemStack[36];
    private final CompletableFuture<Boolean> finishCheck = new CompletableFuture<>();

    public ProxyTrade(@NotNull Player player, @NotNull String other, @NotNull UUID otherId, boolean initiationServer) {
        super(player.getName(), other, initiationServer);
        this.player = player;
        this.other = other;
        this.otherId = otherId;
    }

    @Override
    protected void buildPattern() {
        super.buildPattern();

        this.sent = new ItemStack[getSlots().size()];
        this.received = new ItemStack[getSlots().size()];
    }

    public void receiveItemData(int slotId, @Nullable ItemStack item) {
        this.received[slotId] = item;
        guis[0].setItem(otherSlots.get(slotId), item);
    }

    public boolean receiveFinishCheck() {
        boolean success = tryFinish(Perspective.PRIMARY, false);
        finishCheck.complete(success);
        return success;
    }

    public void receiveState(@NotNull TradeStateUpdatePacket.State state, @Nullable String extra) {
        switch (state) {
            case READY:
                ready[1] = true;
                update();
                break;

            case NOT_READY:
                ready[1] = false;
                update();
                break;

            case CANCELLED:
                cancel(extra, true);
                break;
        }
    }

    public void receiveTradeIconUpdate(@NotNull TradeIcon icon) {
        onTradeOfferChange(true);
        super.synchronizeTradeIcon(Perspective.SECONDARY, icon, false);
    }

    @Override
    public void synchronizeTradeIcon(@NotNull Perspective from, @NotNull TradeIcon icon, boolean updateIcon) {
        super.synchronizeTradeIcon(from, icon, updateIcon);

        //sync on proxy
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            icon.serialize(out);
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred while serializing " + icon.getClass().getName(), e);
        }

        int slot = layout[from.id()].getSlotOf(icon);

        TradeIconUpdatePacket packet = new TradeIconUpdatePacket(player.getName(), other, slot, baos.toByteArray());
        TradeSystem.proxyHandler().send(packet, this.player);
    }

    public void synchronizeState(@NotNull TradeStateUpdatePacket.State state, @Nullable String extra) {
        TradeStateUpdatePacket packet = new TradeStateUpdatePacket(player.getName(), other, state, extra);
        TradeSystem.proxyHandler().send(packet, this.player);
    }

    public void synchronizeInventory() {
        if (guis[0] == null) return;  // we might have already finished this trade

        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < 36; i++) {
            if (!ItemStackUtils.isCompatible(contents[i])) {

                continue;
            }
            Map<String, Object> data;

            if (contents[i] == null) data = null;
            else if (ItemStackUtils.isCompatible(contents[i])) data = ItemStackUtils.serializeItemStack(contents[i]);
            else data = ItemStackUtils.serializeItemStack(getItemPlaceholder(i));

            try {
                PlayerInventoryPacket packet = new PlayerInventoryPacket(player.getName(), other, data, i);
                TradeSystem.proxyHandler().send(packet, this.player);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates an item placeholder for the remote {@link PlayerInventory} since some items cannot be transferred.
     *
     * @param slot The current slot.
     * @return The created placeholder item.
     */
    @NotNull
    private ItemStack getItemPlaceholder(int slot) {
        ItemStack item = new ItemStack(Material.BARRIER);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName("TRADE PLACEHOLDER ITEM " + slot);
        item.setItemMeta(meta);

        return item;
    }

    private void synchronizeItem(int slotId, @Nullable ItemStack item) {
        try {
            TradeItemUpdatePacket packet = new TradeItemUpdatePacket(player.getName(), other, ItemStackUtils.serializeItemStack(item), (byte) slotId);
            TradeSystem.proxyHandler().send(packet, this.player);
            sent[slotId] = item == null ? null : item.clone();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private ItemStack getSent(int slotId) {
        return sent[slotId];
    }

    @Nullable
    private ItemStack getReceived(int slotId) {
        return received[slotId];
    }

    @Override
    protected @Nullable ItemStack removeReceivedItem(@NotNull Perspective perspective, int slotId) {
        ItemStack item = received[slotId];
        received[slotId] = null;
        return item;
    }

    @Override
    protected void updateDisplayItem(@NotNull Perspective perspective, int slotId, @Nullable ItemStack item) {
        if (!perspective.isSecondary()) return;
        synchronizeItem(slotId, item);
    }

    @Override
    protected @Nullable ItemStack getCurrentOfferedItem(@NotNull Perspective perspective, int slotId) {
        if (perspective.isSecondary()) return getReceived(slotId);
        else return guis[0].getItem(slots.get(slotId));
    }

    @Override
    protected @Nullable ItemStack getCurrentDisplayedItem(@NotNull Perspective perspective, int slotId) {
        if (perspective.isSecondary()) return getSent(slotId);
        else return guis[0].getItem(otherSlots.get(slotId));
    }

    @Override
    protected void initializeGUIs() {
        this.guis[0] = new TradingGUI(this.player, this, 0);
    }

    @Override
    protected void startGUI() {
        this.guis[0].prepareStart();
        synchronizeInventory();

        try {
            this.guis[0].open();
        } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable Player getPlayer(@NotNull Perspective perspective) {
        if (perspective == Perspective.PRIMARY) return player;
        return null;
    }

    @Override
    public @NotNull UUID getUniqueId(@NotNull Perspective perspective) {
        if (perspective.isPrimary()) return player.getUniqueId();
        return otherId;
    }

    @Override
    protected void onReadyStateChange(@NotNull Perspective perspective, boolean ready) {
        if (!perspective.isPrimary()) return;
        if (ready) synchronizeState(TradeStateUpdatePacket.State.READY, null);
        else synchronizeState(TradeStateUpdatePacket.State.NOT_READY, null);
    }

    @Override
    protected void onItemPickUp(@NotNull Perspective perspective) {
        synchronizeInventory();
    }

    @Override
    protected void cancelling(@Nullable String message) {
        synchronizeState(TradeStateUpdatePacket.State.CANCELLED, message);
    }

    @Override
    protected void clearOpenAnvils() {
        for (AnvilGUI gui : API.getRemovables(player, AnvilGUI.class)) {
            gui.clearInventory();
        }
    }

    @Override
    protected @NotNull CompletableFuture<Boolean> canFinish() {
        return TradeSystem.proxyHandler()
                .send(new TradeCheckFinishPacket(this.player.getName(), this.other), this.player)  // test if other player can finish
                .thenApply(BooleanPacket::getBoolean)
                .thenCompose(ready -> ready ? finishCheck : CompletableFuture.completedFuture(false));  // test this player once the other server checks in
    }

    @Override
    protected @NotNull Stream<Player> getParticipants() {
        return Stream.of(player);
    }

    @Override
    protected @NotNull PlayerInventory getPlayerInventory(@NotNull Perspective perspective) {
        if (perspective.isPrimary()) return new PlayerInventory(this.player, false);
        return new PlayerInventory(this.otherInventory);
    }

    public void setOtherInventory(int slot, @Nullable ItemStack item) {
        this.otherInventory[slot] = item;
    }

    @Override
    protected void informTransition(@NotNull TradeIcon from, @NotNull Perspective to) {
        if (to == Perspective.SECONDARY) return;
        super.informTransition(from, to);
    }

    @Override
    protected boolean isActive() {
        return this.guis[0] != null;
    }

    @Override
    protected boolean isPaused() {
        return pause[0];
    }

    @Override
    protected boolean isInitiator(@NotNull Perspective perspective) {
        return perspective.isPrimary() && initiationServer;
    }
}
