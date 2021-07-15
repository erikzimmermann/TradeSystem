package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.api.TradeCompleteEvent;
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

import java.util.ArrayList;
import java.util.List;
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
    protected boolean[] cancelGUIs() {
        if (this.guis[0] == null && this.guis[1] == null) return null;

        boolean[] droppedItems = new boolean[] {false, false};
        for (Integer slot : this.slots) {
            if (this.guis[0] != null) {
                if (this.guis[0].getItem(slot) != null && this.guis[0].getItem(slot).getType() != Material.AIR) {
                    ItemStack item = this.guis[0].getItem(slot);
                    int i = fit(this.players[0], item);

                    if (item.getAmount() > i) {
                        item.setAmount(item.getAmount() - i);
                        this.players[0].getInventory().addItem(item);
                    }
                    if (i > 0) {
                        item.setAmount(i);
                        droppedItems[0] |= dropItem(players[0], item);
                    }
                }
            }

            if (this.guis[1] != null) {
                if (this.guis[1].getItem(slot) != null && this.guis[1].getItem(slot).getType() != Material.AIR) {
                    ItemStack item = this.guis[1].getItem(slot);
                    int i = fit(this.players[1], item);

                    if (item.getAmount() > i) {
                        item.setAmount(item.getAmount() - i);
                        this.players[1].getInventory().addItem(item);
                    }
                    if (i > 0) {
                        item.setAmount(i);
                        droppedItems[1] |= dropItem(players[1], item);
                    }
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            if (this.guis[i] == null) continue;

            ItemStack item = this.players[i].getOpenInventory().getCursor();
            if (item != null && item.getType() != Material.AIR) {
                int fit = fit(this.players[i], item.clone());

                if (item.getAmount() > fit) {
                    item.setAmount(item.getAmount() - fit);
                    this.players[i].getInventory().addItem(item);
                }
                if (fit > 0) {
                    item.setAmount(fit);
                    droppedItems[i] |= dropItem(players[i], item);
                }

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
        final Player player1 = players[0];
        final Player player2 = players[1];


        List<ItemStack> player1Items = new ArrayList<>();
        List<ItemStack> player2Items = new ArrayList<>();
        for (Integer slot : slots) {
            ItemStack i0 = guis[0].getItem(slot);
            if (i0 != null && i0.getType() != Material.AIR) {
                player1Items.add(i0);
            }

            ItemStack i1 = guis[1].getItem(slot);
            if (i1 != null && i1.getType() != Material.AIR) {
                player2Items.add(i1);
            }
        }
        TradeCompleteEvent tradeCompleteEvent = new TradeCompleteEvent(player1, player2, player1Items, player2Items);
        Bukkit.getPluginManager().callEvent(tradeCompleteEvent);
        if (tradeCompleteEvent.isCancelled()) {
            cancel();
            return;
        }
        List<ItemStack> player1TradeItems = tradeCompleteEvent.getPlayer1TradeItems();
        List<ItemStack> player2TradeItems = tradeCompleteEvent.getPlayer2TradeItems();
        
        Runnable runnable = () -> {
            if (!tryFinish(player1)) return;
            if (!tryFinish(player2)) return;

            pause[0] = true;
            pause[1] = true;

            for (Player player : players) {
                player.closeInventory();
            }

            TradeSystem.man().unregisterTrade(super.players[0]);
            TradeSystem.man().unregisterTrade(super.players[1]);

            for (Integer slot : slots) {
                guis[1].setItem(slot, null);
                guis[0].setItem(slot, null);
            }

            boolean[] droppedItems = new boolean[] {false, false};
            for (ItemStack i0 : player2TradeItems) {
                if (i0 != null && i0.getType() != Material.AIR) {
                    int rest = fit(player1, i0);

                    if (rest <= 0) {
                        callTradeEvent(player1, player2, i0);
                        player1.getInventory().addItem(i0);
                    } else {
                        ItemStack toDrop = i0.clone();
                        toDrop.setAmount(rest);

                        i0.setAmount(i0.getAmount() - rest);
                        if (i0.getAmount() > 0) player1.getInventory().addItem(i0);

                        droppedItems[0] |= dropItem(player1, toDrop);
                    }
                    getTradeLog().log(player1.getName(), player2.getName(), TradeLogMessages.RECEIVE_ITEM.get(player1.getName(), i0.getAmount() + "x " + i0.getType()));
                }
            }
            for (ItemStack i1 : player1TradeItems) {
                if (i1 != null && i1.getType() != Material.AIR) {
                    int rest = fit(player2, i1);

                    if (rest <= 0) {
                        callTradeEvent(player2, player1, i1);
                        player2.getInventory().addItem(i1);
                    } else {
                        ItemStack toDrop = i1.clone();
                        toDrop.setAmount(rest);

                        i1.setAmount(i1.getAmount() - rest);
                        if (i1.getAmount() > 0) player2.getInventory().addItem(i1);

                        droppedItems[1] |= dropItem(player2, toDrop);
                    }
                    getTradeLog().log(player1.getName(), player2.getName(), TradeLogMessages.RECEIVE_ITEM.get(player2.getName(), i1.getAmount() + "x " + i1.getType()));
                }
            }

            guis[0].clear();
            guis[1].clear();

            finishPlayer(player1);
            finishPlayer(player2);

            player1.sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished", player1));
            player2.sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished", player2));

            for (int i = 0; i < droppedItems.length; i++) {
                if (droppedItems[i]) {
                    this.players[i].sendMessage(Lang.getPrefix() + Lang.get("Items_Dropped", this.players[i]));
                }
            }
            getTradeLog().logLater(player1.getName(), player2.getName(), TradeLogMessages.FINISHED.get(), 10);

            TradeSystem.man().playFinishSound(player1);
            TradeSystem.man().playFinishSound(player2);
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
                        TradeSystem.man().playCountdownStopSound(player1);
                        TradeSystem.man().playCountdownStopSound(player2);
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
                        TradeSystem.man().playCountdownTickSound(player1);
                        TradeSystem.man().playCountdownTickSound(player2);
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
