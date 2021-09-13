package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.tradesystem.proxy.packets.*;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.transfer.utils.ItemStackUtils;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService.getTradeLog;

public class ProxyTrade extends Trade {
    private final Player player;
    private final String other;
    private ItemStack[] sent;
    private ItemStack[] received;
    private ItemStack[] otherInventory = null;
    private boolean finishing = false;

    public ProxyTrade(Player player, String other, boolean initiationServer) {
        super(player.getName(), other, initiationServer);
        this.player = player;
        this.other = other;
    }

    @Override
    protected void buildPattern() {
        super.buildPattern();

        this.sent = new ItemStack[getSlots().size()];
        this.received = new ItemStack[getSlots().size()];
    }

    public void receiveItemData(int slotId, @Nullable ItemStack item) {
        this.received[slotId] = item;
        update();
    }

    public boolean receiveEconomyCheck() {
        confirmFinish();
        return true;
    }

    public void receiveState(TradeStateUpdatePacket.State state, String extra) {
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

    public void receiveTradeIconUpdate(TradeIcon icon) {
        super.synchronizeTradeIcon(1, icon, false);
    }

    @Override
    public void synchronizeTradeIcon(int playerId, TradeIcon icon, boolean updateIcon) {
        super.synchronizeTradeIcon(playerId, icon, updateIcon);

        //sync on proxy
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            icon.serialize(out);
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred while serializing " + icon.getClass().getName(), e);
        }

        int slot = layout[playerId].getSlotOf(icon);

        TradeIconUpdatePacket packet = new TradeIconUpdatePacket(player.getName(), other, slot, baos.toByteArray());
        TradeSystem.proxyHandler().send(packet, this.player);
    }

    public void synchronizeState(@NotNull TradeStateUpdatePacket.State state, String extra) {
        TradeStateUpdatePacket packet = new TradeStateUpdatePacket(player.getName(), other, state, extra);
        TradeSystem.proxyHandler().send(packet, this.player);
    }

    public void synchronizeInventory() {
        @SuppressWarnings ("unchecked")
        Map<String, Object>[] items = new Map[36];
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < 36; i++) {
            if (contents[i] != null) items[i] = ItemStackUtils.serializeItemStack(contents[i]);
        }

        PlayerInventoryPacket packet = new PlayerInventoryPacket(player.getName(), other, items);
        TradeSystem.proxyHandler().send(packet, this.player);
    }

    private void synchronizeItem(int slotId, @Nullable ItemStack item) {
        TradeItemUpdatePacket packet = new TradeItemUpdatePacket(player.getName(), other, ItemStackUtils.serializeItemStack(item), (byte) slotId);
        TradeSystem.proxyHandler().send(packet, this.player);
        sent[slotId] = item == null ? null : item.clone();
    }

    private ItemStack getSent(int slot) {
        if (slot < 0 || slot >= 54) return null;
        return sent[slot];
    }

    private ItemStack getReceived(int slot) {
        if (slot < 0 || slot >= 54) return null;
        return received[slot];
    }

    @Override
    protected void updateGUI() {
        if (guis[0] != null) {
            for (int i = 0; i < slots.size(); i++) {
                int slot = slots.get(i);

                ItemStack item = guis[0].getItem(slot);
                if (!Objects.equals(item, getSent(i))) {
                    synchronizeItem(i, item);

                    ready[0] = ready[1] = false;
                }

                int otherSlot = otherSlots.get(i);
                item = guis[0].getItem(otherSlot);
                if (!Objects.equals(item, getReceived(i))) {
                    guis[0].setItem(otherSlot, getReceived(i));

                    ready[0] = ready[1] = false;
                }
            }

            updateStatusIcon(player, 0);
        }
    }

    @Override
    protected void synchronizeTitle() {
        guis[0].synchronizeTitle();
    }

    @Override
    protected void playCountDownStopSound() {
        TradeSystem.man().playCountdownStopSound(player);
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
            e.printStackTrace();
        }
    }

    @Override
    protected void playStartSound() {
        TradeSystem.man().playStartSound(this.player);
    }

    @Override
    protected synchronized boolean[] cancelGUIs() {
        if (this.guis[0] == null) return null;

        boolean[] droppedItems = new boolean[] {false, false};
        for (Integer slot : this.slots) {
            if (this.guis[0].getItem(slot) != null && this.guis[0].getItem(slot).getType() != Material.AIR) {
                ItemStack item = this.guis[0].getItem(slot);
                droppedItems[0] |= addOrDrop(this.player, item);
            }
        }

        ItemStack item = this.player.getOpenInventory().getCursor();
        if (item != null && item.getType() != Material.AIR) {
            droppedItems[0] |= addOrDrop(this.player, item);
            this.player.getOpenInventory().setCursor(null);
        }

        this.guis[0] = null;
        return droppedItems;
    }

    @Override
    protected void callReadyUpdate(int id, boolean ready) {
        if (ready) synchronizeState(TradeStateUpdatePacket.State.READY, null);
        else synchronizeState(TradeStateUpdatePacket.State.NOT_READY, null);
    }

    @Override
    protected void closeInventories() {
        this.player.closeInventory();
        if (this.guis[0] != null) this.guis[0].destroy();
        this.player.updateInventory();
    }

    @Override
    protected void playCancelSound() {
        TradeSystem.man().playCancelSound(this.player);
    }

    @Override
    protected void sendMessage(String message) {
        sendMessage(0, message);
    }

    @Override
    protected void sendMessage(int id, String message) {
        if (id == 0) this.player.sendMessage(message);
    }

    @Override
    protected String getPlaceholderMessage(int playerId, String message) {
        return Lang.get(message, player);
    }

    @Override
    protected Listener getPickUpListener() {
        //noinspection deprecation
        return new Listener() {
            @EventHandler
            public void onPickup(PlayerPickupItemEvent e) {
                if (e.getPlayer() == player) {
                    if (!canPickup(e.getPlayer(), e.getItem().getItemStack()) || waitForPickup[0]) e.setCancelled(true);
                    else {
                        //player picked up an item, check trading items -> balance items of other trader
                        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> synchronizeInventory(), 1);
                    }
                }
            }
        };
    }

    @Override
    protected void cancelling(String message) {
        synchronizeState(TradeStateUpdatePacket.State.CANCELLED, message);
    }

    @Override
    protected void clearOpenAnvils() {
        for (AnvilGUI gui : API.getRemovables(player, AnvilGUI.class)) {
            gui.clearInventory();
        }
    }

    public synchronized void confirmFinish() {
        if (!finishing) finishing = true;
        else {
            //finish
            pause[0] = true;

            this.player.closeInventory();

            TradeSystem.man().unregisterTrade(this.player.getName());
            TradeSystem.man().unregisterTrade(this.other);

            boolean[] droppedItems = new boolean[] {false, false};
            for (int i = 0; i < slots.size(); i++) {
                int slot = slots.get(i);

                guis[0].setItem(slot, null);

                //using original one to prevent dupe glitches!!!
                ItemStack i0 = getReceived(i);

                //Log before calling the event. The event could remove this item and we would still lose it.
                if (i0 != null && i0.getType() != Material.AIR) {
                    if (initiationServer) getTradeLog().log(this.player.getName(), this.other, TradeLogMessages.RECEIVE_ITEM.get(this.player.getName(), i0.getAmount() + "x " + i0.getType()));
                    else {
                        //exception -> proxy trade -> handle item on one server -> switch players
                        getTradeLog().log(this.other, this.player.getName(), TradeLogMessages.RECEIVE_ITEM.get(this.player.getName(), i0.getAmount() + "x " + i0.getType()));
                    }
                }

                //call event
                i0 = callTradeEvent(this.player, this.other, i0);

                if (i0 != null && i0.getType() != Material.AIR) {
                    int rest = fit(this.player, i0);

                    if (rest <= 0) this.player.getInventory().addItem(i0);
                    else {
                        ItemStack toDrop = i0.clone();
                        toDrop.setAmount(rest);

                        i0.setAmount(i0.getAmount() - rest);
                        if (i0.getAmount() > 0) this.player.getInventory().addItem(i0);

                        droppedItems[0] |= dropItem(this.player, toDrop);
                    }
                }
            }

            guis[0].clear();

            finishPlayer(this.player);

            this.player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished", this.player));
            if (droppedItems[0]) this.player.sendMessage(Lang.getPrefix() + Lang.get("Items_Dropped", this.player));

            if (initiationServer) getTradeLog().logLater(this.player.getName(), this.other, TradeLogMessages.FINISHED.get(), 10);

            TradeSystem.man().playFinishSound(this.player);
        }
    }

    private void checkFinish() {
        tryFinish(this.player);

        TradeSystem.proxyHandler().send(new TradeCheckFinishPacket(this.player.getName(), this.other), this.player).whenComplete((suc, t) -> {
            if (t != null) t.printStackTrace();
            else if (suc.getBoolean()) confirmFinish();
            else callEconomyError();
        });
    }

    private void callEconomyError() {
        cancel(Lang.getPrefix() + Lang.get("Economy_Error"));
    }

    @Override
    protected void finish() {
        if (this.countdown != null) return;

        if (this.guis[0] == null) return;
        if (pause[0]) return;

        int interval = TradeSystem.man().getCountdownInterval();
        int repetitions = TradeSystem.man().getCountdownRepetitions();

        if (interval == 0 || repetitions == 0) checkFinish();
        else {
            this.countdown = new BukkitRunnable() {
                @Override
                public void run() {
                    if (guis[0] == null) {
                        this.cancel();
                        countdownTicks = 0;
                        countdown = null;
                        return;
                    }

                    if (!ready[0] || !ready[1]) {
                        this.cancel();
                        TradeSystem.man().playCountdownStopSound(player);
                        countdownTicks = 0;
                        countdown = null;
                        guis[0].synchronizeTitle();
                        return;
                    }

                    if (countdownTicks == repetitions) {
                        checkFinish();
                        this.cancel();
                        countdownTicks = 0;
                        countdown = null;
                        return;
                    } else {
                        guis[0].synchronizeTitle();
                        TradeSystem.man().playCountdownTickSound(player);
                    }

                    countdownTicks++;
                }
            };

            this.countdown.runTaskTimer(TradeSystem.getInstance(), 0, interval);
        }
    }

    @Override
    public void cancelOverflow(int playerId) {
        if (guis[0] == null || playerId != 0) return;
        super.cancelOverflow(this.player);
    }

    @Override
    protected @NotNull PlayerInventory getPlayerInventory(int playerId) {
        if (playerId == 0) return new PlayerInventory(this.player);
        return new PlayerInventory(this.otherInventory);
    }

    @Override
    public Optional<Player> getOther(Player p) {
        return Optional.empty();
    }

    public void setOtherInventory(ItemStack[] otherInventory) {
        this.otherInventory = otherInventory;
    }

    @Override
    protected void informTransition(TradeIcon icon, int otherId) {
        if (otherId == 1) return;
        super.informTransition(icon, otherId);
    }
}
