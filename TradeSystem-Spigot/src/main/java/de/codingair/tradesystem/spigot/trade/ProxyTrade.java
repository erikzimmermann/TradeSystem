package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.tradesystem.proxy.packets.*;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Profile;
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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static de.codingair.tradesystem.spigot.tradelog.TradeLogService.getTradeLog;

public class ProxyTrade extends Trade {
    private final Player player;
    private final String other;
    private final ItemStack[] sent;
    private final ItemStack[] received;
    private ItemStack[] otherInventory = null;
    private boolean finishing = false;

    public ProxyTrade(Player player, String other) {
        super(player.getName(), other);
        this.player = player;
        this.other = other;

        this.sent = new ItemStack[getSlots().size()];
        this.received = new ItemStack[getSlots().size()];
    }

    public void receiveItemData(int slotId, @Nullable ItemStack item) {
        this.received[slotId] = item;
        update();
    }

    public void receiveMoneyData(double money) {
        super.money[1] = (int) money;
        update();
    }

    public boolean receiveEconomyCheck(double money) {
        if (super.money[1] == (int) money) {
            confirmFinish();
            return true;
        } else {
            callEconomyError();
            return false;
        }
    }

    public void receiveState(TradeStateUpdatePacket.State state, String extra) {
        switch (state) {
            case READY:
                ready[1] = true;
                update(true);
                break;

            case NOT_READY:
                ready[1] = false;
                update(true);
                break;

            case CANCELLED:
                cancel(extra, true);
                break;
        }
    }

    public void synchronizeState(@NotNull TradeStateUpdatePacket.State state, String extra) {
        TradeStateUpdatePacket packet = new TradeStateUpdatePacket(player.getName(), other, state, extra);
        TradeSystem.proxyHandler().send(packet, this.player);
    }

    public void synchronizeInventory() {
        Map<String, Object>[] items = new Map[36];
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < 36; i++) {
            items[i] = contents[i] == null ? null : contents[i].serialize();
        }

        PlayerInventoryPacket packet = new PlayerInventoryPacket(player.getName(), other, items);
        TradeSystem.proxyHandler().send(packet, this.player);
    }

    private void synchronizeItem(int slotId, @Nullable ItemStack item) {
        TradeItemUpdatePacket packet = new TradeItemUpdatePacket(player.getName(), other, item == null ? null : item.serialize(), (byte) slotId);
        TradeSystem.proxyHandler().send(packet, this.player);
        sent[slotId] = item == null ? null : item.clone();
    }

    private void synchronizeMoney(double money) {
        TradeMoneyUpdatePacket packet = new TradeMoneyUpdatePacket(player.getName(), other, money);
        TradeSystem.proxyHandler().send(packet, this.player);
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
    protected void updateGUI(boolean forceUpdate) {
        if (guis[0] != null) {
            boolean update = forceUpdate;
            for (int i = 0; i < slots.size(); i++) {
                int slot = slots.get(i);

                ItemStack item = guis[0].getItem(slot);
                if (!Objects.equals(item, getSent(i))) {
                    synchronizeItem(i, item);

                    ready[0] = ready[1] = false;
                    update = true;
                }

                int otherSlot = otherSlots.get(i);
                item = guis[0].getItem(otherSlot);
                if (!Objects.equals(item, getReceived(i))) {
                    guis[0].setItem(otherSlot, getReceived(i));

                    ready[0] = ready[1] = false;
                    update = true;
                }
            }

            if (money[0] != moneyBackup[0]) {
                synchronizeMoney(money[0]);

                moneyBackup[0] = money[0];
                ready[0] = ready[1] = false;
                update = true;
            }

            if (money[1] != moneyBackup[1]) {
                moneyBackup[1] = money[1];
                ready[0] = ready[1] = false;
                update = true;
            }

            if (update) guis[0].initialize(this.player);
        }
    }

    @Override
    protected void playCountDownStopSound() {
        TradeSystem.man().playCountdownStopSound(player);
    }

    @Override
    protected void startGUI() {
        synchronizeInventory();

        this.guis[0] = new TradingGUI(this.player, 0, this);
        this.guis[0].open();
    }

    @Override
    protected void playStartSound() {
        TradeSystem.man().playStartSound(this.player);
    }

    @Override
    protected boolean[] closeGUI() {
        if (this.guis[0] == null) return null;

        boolean[] droppedItems = new boolean[] {false, false};
        for (Integer slot : this.slots) {
            if (this.guis[0].getItem(slot) != null && this.guis[0].getItem(slot).getType() != Material.AIR) {
                ItemStack item = this.guis[0].getItem(slot);
                int i = fit(this.player, item);

                if (item.getAmount() > i) {
                    item.setAmount(item.getAmount() - i);
                    this.player.getInventory().addItem(item);
                }
                if (i > 0) {
                    item.setAmount(i);
                    droppedItems[0] |= dropItem(this.player, item);
                }
            }
        }

        ItemStack item = this.player.getOpenInventory().getCursor();
        if (item != null && item.getType() != Material.AIR) {
            int fit = fit(this.player, item.clone());

            if (item.getAmount() > fit) {
                item.setAmount(item.getAmount() - fit);
                this.player.getInventory().addItem(item);
            }
            if (fit > 0) {
                item.setAmount(fit);
                droppedItems[0] |= dropItem(player, item);
            }

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

    public synchronized void confirmFinish() {
        if (!finishing) finishing = true;
        else {
            //finish
            Profile profile = TradeSystem.getProfile(this.player);

            guis[0].pause = true;

            this.player.closeInventory();

            TradeSystem.man().getTrades().remove(this.player.getName());
            TradeSystem.man().getTrades().remove(this.other);

            boolean[] droppedItems = new boolean[] {false, false};
            for (int i = 0; i < slots.size(); i++) {
                int slot = slots.get(i);

                //using original one to prevent dupe glitches!!!
                ItemStack i0 = getReceived(i);
                guis[0].setItem(slot, null);

                if (i0 != null && i0.getType() != Material.AIR) {
                    int rest = fit(this.player, i0);

                    if (rest <= 0) {
                        callTradeEvent(this.player, this.other, i0);
                        this.player.getInventory().addItem(i0);
                    } else {
                        ItemStack toDrop = i0.clone();
                        toDrop.setAmount(rest);

                        i0.setAmount(i0.getAmount() - rest);
                        if (i0.getAmount() > 0) this.player.getInventory().addItem(i0);

                        droppedItems[0] |= dropItem(this.player, toDrop);
                    }
                    getTradeLog().log(this.player.getName(), this.other, this.player.getName() + " received " + i0.getAmount() + "x " + i0.getType());
                }
            }

            guis[0].clear();
            guis[0].close(this.player, true);

            double diff = -money[0] + money[1];
            handleMoney(this.player.getName(), this.other, profile, diff);

            this.player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished", this.player));
            if (droppedItems[0]) this.player.sendMessage(Lang.getPrefix() + Lang.get("Items_Dropped", this.player));
            getTradeLog().log(this.player.getName(), this.other, "Trade Finished");

            TradeSystem.man().playFinishSound(this.player);
        }
    }

    private void checkFinish() {
        Profile profile = TradeSystem.getProfile(this.player);

        if (profile.getMoney() < money[0]) {
            callEconomyError();
            return;
        }

        TradeSystem.proxyHandler().send(new TradeCheckEconomyPacket(this.player.getName(), this.other, money[0]), this.player).whenComplete((suc, t) -> {
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
        if (this.guis[0].pause) return;

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
}
