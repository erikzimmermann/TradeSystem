package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
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

import java.util.Objects;
import java.util.Optional;

import static de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService.getTradeLog;

public class BukkitTrade extends Trade {
    private final Player[] players = new Player[2];

    BukkitTrade(Player p0, Player p1, boolean initiationServer) {
        super(p0.getName(), p1.getName(), initiationServer);
        this.players[0] = p0;
        this.players[1] = p1;
    }

    @Override
    protected void updateGUI() {
        if (guis[0] != null && guis[1] != null) {
            for (int i = 0; i < slots.size(); i++) {
                ItemStack item = guis[0].getItem(slots.get(i));
                ItemStack other = guis[1].getItem(otherSlots.get(i));

                if (!Objects.equals(item, other)) {
                    guis[1].setItem(otherSlots.get(i), guis[0].getItem(slots.get(i)));

                    ready[0] = false;
                    ready[1] = false;
                }

                item = guis[1].getItem(slots.get(i));
                other = guis[0].getItem(otherSlots.get(i));

                if (!Objects.equals(item, other)) {
                    guis[0].setItem(otherSlots.get(i), guis[1].getItem(slots.get(i)));

                    ready[1] = false;
                    ready[0] = false;
                }
            }

            updateStatusIcon(players[0], 0);
            updateStatusIcon(players[1], 1);
        }
    }

    @Override
    protected void synchronizeTitle() {
        guis[0].synchronizeTitle();
        guis[1].synchronizeTitle();
    }

    @Override
    protected void playCountDownStopSound() {
        TradeSystem.man().playCountdownStopSound(players[0]);
        TradeSystem.man().playCountdownStopSound(players[1]);
    }

    @Override
    protected void initializeGUIs() {
        this.guis[0] = new TradingGUI(this.players[0], this, 0);
        this.guis[1] = new TradingGUI(this.players[1], this, 1);
    }

    @Override
    protected void startGUI() {
        this.guis[0].prepareStart();
        this.guis[1].prepareStart();

        try {
            this.guis[0].open();
            this.guis[1].open();
        } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void playStartSound() {
        TradeSystem.man().playStartSound(this.players[0]);
        TradeSystem.man().playStartSound(this.players[1]);
    }

    @Override
    protected synchronized boolean[] cancelGUIs() {
        if (this.guis[0] == null && this.guis[1] == null) return null;

        boolean[] droppedItems = new boolean[] {false, false};
        for (Integer slot : this.slots) {
            for (int i = 0; i < 2; i++) {
                if (this.guis[i] != null) {
                    if (this.guis[i].getItem(slot) != null && this.guis[i].getItem(slot).getType() != Material.AIR) {
                        ItemStack item = this.guis[i].getItem(slot);
                        droppedItems[i] |= addOrDrop(this.players[i], item);
                    }
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            if (this.guis[i] == null) continue;

            ItemStack item = this.players[i].getOpenInventory().getCursor();
            if (item != null && item.getType() != Material.AIR) {
                droppedItems[i] |= addOrDrop(this.players[i], item);
                this.players[i].getOpenInventory().setCursor(null);
            }
        }

        this.guis[0] = null;
        this.guis[1] = null;
        return droppedItems;
    }

    @Override
    protected void closeInventories() {
        this.players[0].closeInventory();
        this.players[1].closeInventory();

        if (this.guis[0] != null) this.guis[0].destroy();
        if (this.guis[1] != null) this.guis[1].destroy();

        this.players[0].updateInventory();
        this.players[1].updateInventory();
    }

    @Override
    protected void playCancelSound() {
        TradeSystem.man().playCancelSound(this.players[0]);
        TradeSystem.man().playCancelSound(this.players[1]);
    }

    @Override
    protected void sendMessage(String message) {
        sendMessage(0, message);
        sendMessage(1, message);
    }

    @Override
    protected void sendMessage(int id, String message) {
        this.players[id].sendMessage(message);
    }

    @Override
    protected String getPlaceholderMessage(int playerId, String message) {
        return Lang.get(message, players[playerId]);
    }

    @Override
    protected Listener getPickUpListener() {
        //noinspection deprecation
        return new Listener() {
            @EventHandler
            public void onPickup(PlayerPickupItemEvent e) {
                if (e.getPlayer() == players[0]) {
                    if (!canPickup(e.getPlayer(), e.getItem().getItemStack()) || waitForPickup[0]) e.setCancelled(true);
                    else {
                        //player picked up an item, check trading items -> balance items of other trader
                        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> cancelOverflow(players[1]), 1);
                    }
                } else if (e.getPlayer() == players[1]) {
                    if (!canPickup(e.getPlayer(), e.getItem().getItemStack()) || waitForPickup[1]) e.setCancelled(true);
                    else {
                        //player picked up an item, check trading items -> balance items of other trader
                        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> cancelOverflow(players[0]), 1);
                    }
                }
            }
        };
    }

    @Override
    protected void cancelling(String message) {
        //ignore
    }

    @Override
    protected void clearOpenAnvils() {
        for (Player player : this.players) {
            for (AnvilGUI gui : API.getRemovables(player, AnvilGUI.class)) {
                gui.clearInventory();
            }
        }
    }

    @Override
    protected void callReadyUpdate(int id, boolean ready) {
        //ignore
    }

    @Override
    protected void finish() {
        if (this.countdown != null) return;

        if (this.guis[0] == null || this.guis[1] == null) return;
        if (pause[0] && pause[1]) return;

        // code to avoid some weird money dupe
        final Player player0 = players[0];
        final Player player1 = players[1];

        Runnable runnable = () -> {
            if (!tryFinish(player0)) return;
            if (!tryFinish(player1)) return;

            pause[0] = true;
            pause[1] = true;

            for (Player player : players) {
                player.closeInventory();
            }

            TradeSystem.man().unregisterTrade(super.players[0]);
            TradeSystem.man().unregisterTrade(super.players[1]);

            boolean[] droppedItems = new boolean[] {false, false};
            for (Integer slot : slots) {
                //using original one to prevent dupe glitches!!!
                ItemStack i0 = guis[1].getItem(slot);
                ItemStack i1 = guis[0].getItem(slot);

                guis[1].setItem(slot, null);
                guis[0].setItem(slot, null);

                //Log before calling the events. The events could remove this item and we would still lose it.
                if (i0 != null && i0.getType() != Material.AIR)
                    getTradeLog().log(player0.getName(), player1.getName(), TradeLogMessages.RECEIVE_ITEM.get(player0.getName(), i0.getAmount() + "x " + i0.getType()));
                if (i1 != null && i1.getType() != Material.AIR)
                    getTradeLog().log(player0.getName(), player1.getName(), TradeLogMessages.RECEIVE_ITEM.get(player1.getName(), i1.getAmount() + "x " + i1.getType()));

                //call events
                i0 = callTradeEvent(player0, player1, i0);
                i1 = callTradeEvent(player1, player0, i1);

                if (i0 != null && i0.getType() != Material.AIR) {
                    int rest = fit(player0, i0);

                    if (rest <= 0) player0.getInventory().addItem(i0);
                    else {
                        ItemStack toDrop = i0.clone();
                        toDrop.setAmount(rest);

                        i0.setAmount(i0.getAmount() - rest);
                        if (i0.getAmount() > 0) player0.getInventory().addItem(i0);

                        droppedItems[0] |= dropItem(player0, toDrop);
                    }
                }

                if (i1 != null && i1.getType() != Material.AIR) {
                    int rest = fit(player1, i1);

                    if (rest <= 0) player1.getInventory().addItem(i1);
                    else {
                        ItemStack toDrop = i1.clone();
                        toDrop.setAmount(rest);

                        i1.setAmount(i1.getAmount() - rest);
                        if (i1.getAmount() > 0) player1.getInventory().addItem(i1);

                        droppedItems[1] |= dropItem(player1, toDrop);
                    }
                }
            }

            guis[0].clear();
            guis[1].clear();

            finishPlayer(player0);
            finishPlayer(player1);

            player0.sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished", player0));
            player1.sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished", player1));

            for (int i = 0; i < droppedItems.length; i++) {
                if (droppedItems[i]) {
                    this.players[i].sendMessage(Lang.getPrefix() + Lang.get("Items_Dropped", this.players[i]));
                }
            }
            getTradeLog().logLater(player0.getName(), player1.getName(), TradeLogMessages.FINISHED.get(), 10);

            TradeSystem.man().playFinishSound(player0);
            TradeSystem.man().playFinishSound(player1);
        };

        int interval = TradeSystem.man().getCountdownInterval();
        int repetitions = TradeSystem.man().getCountdownRepetitions();

        if (interval == 0 || repetitions == 0) runnable.run();
        else {
            this.countdown = new BukkitRunnable() {
                @Override
                public void run() {
                    if (guis[0] == null || guis[1] == null) {
                        this.cancel();
                        countdownTicks = 0;
                        countdown = null;
                        return;
                    }

                    if (!ready[0] || !ready[1]) {
                        this.cancel();
                        TradeSystem.man().playCountdownStopSound(player0);
                        TradeSystem.man().playCountdownStopSound(player1);
                        countdownTicks = 0;
                        countdown = null;
                        guis[0].synchronizeTitle();
                        guis[1].synchronizeTitle();
                        return;
                    }

                    if (countdownTicks == repetitions) {
                        runnable.run();
                        this.cancel();
                        countdownTicks = 0;
                        countdown = null;
                        return;
                    } else {
                        guis[0].synchronizeTitle();
                        guis[1].synchronizeTitle();
                        TradeSystem.man().playCountdownTickSound(player0);
                        TradeSystem.man().playCountdownTickSound(player1);
                    }

                    countdownTicks++;
                }
            };

            this.countdown.runTaskTimer(TradeSystem.getInstance(), 0, interval);
        }
    }

    @Override
    public void cancelOverflow(int playerId) {
        cancelOverflow(players[playerId]);
    }

    public Optional<Player> getOther(Player p) {
        if (this.players[0] == null || this.players[1] == null) return Optional.empty();

        if (this.players[0].equals(p)) return Optional.of(this.players[1]);
        else return Optional.of(this.players[0]);
    }

    @Override
    protected @NotNull PlayerInventory getPlayerInventory(int playerId) {
        return new PlayerInventory(players[playerId]);
    }
}
