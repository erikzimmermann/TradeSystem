package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyClosedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeFinishEvent;
import de.codingair.tradesystem.spigot.events.TradeItemEvent;
import de.codingair.tradesystem.spigot.events.TradeReportEvent;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.TradeLayout;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.IconHandler;
import de.codingair.tradesystem.spigot.trade.gui.layout.shulker.ShulkerPeekGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.ShowStatusIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.StatusIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlot;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlotOther;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class Trade {
    protected final String[] players = new String[2];
    protected final boolean initiationServer;
    protected final TradeLayout[] layout = new TradeLayout[2];
    protected final TradingGUI[] guis = new TradingGUI[2];
    protected final List<Integer> slots = new ArrayList<>();
    protected final List<Integer> otherSlots = new ArrayList<>();

    protected final boolean[] ready = new boolean[] {false, false};
    protected final boolean[] pause = new boolean[] {false, false};
    protected final boolean[] cursor = new boolean[] {false, false};
    protected final boolean[] waitForPickup = new boolean[] {false, false}; //field to wait for a pickup event (e.g. when players holding items with their cursor)

    protected Pattern pattern;
    protected Listener pickupListener;
    protected BukkitRunnable countdown = null;
    protected int countdownTicks = 0;
    protected boolean cancelling = false;

    protected Trade(String player0, String player1, boolean initiationServer) {
        this.initiationServer = initiationServer;
        this.players[0] = player0;
        this.players[1] = player1;
    }

    /**
     * Initialize trading GUIs for all participants.
     */
    protected abstract void initializeGUIs();

    /**
     * Open trading GUIs for all participants.
     */
    protected abstract void startGUI();

    /**
     * Note: Player with id=0 is never null.
     *
     * @param id The id of the player who should be returned.
     * @return The player with the given id.
     */
    @Nullable
    public abstract Player getPlayer(int id);

    /**
     * @param id The id of the player who should be returned.
     * @return The {@link UUID} of the player with the given id.
     */
    @NotNull
    public abstract UUID getUniqueId(int id);

    /**
     * Avoid moving the item which will be renamed into the players inventory.
     */
    protected abstract void clearOpenAnvils();

    /**
     * @return True if the trade is still active.
     */
    protected abstract boolean isActive();

    /**
     * @return True if the trade is paused (to allow closing the inventory without cancelling the trade).
     */
    protected abstract boolean isPaused();

    /**
     * @param player The player that should be checked.
     * @param id     The id of the player.
     * @return True if the player is the initiator of the trade.
     */
    protected abstract boolean isInitiator(@NotNull Player player, int id);

    /**
     * @param playerId The id of the player whose inventory will be returned.
     * @return The inventory of the player.
     */
    @NotNull
    protected abstract PlayerInventory getPlayerInventory(int playerId);

    /**
     * @param id     The id of the player who receives the items.
     * @param slotId The slot of the item to receive (left side slots).
     * @return The item to receive.
     */
    @Nullable
    protected abstract ItemStack removeReceivedItem(int id, int slotId);

    /**
     * Check if the trade can be finished on both sides.
     *
     * @return A future that will be completed with true if the trade can be finished.
     */
    @NotNull
    protected abstract CompletableFuture<Boolean> canFinish();

    /**
     * The item pickup event for checking item overflow.
     *
     * @param player The player who is picking up the item.
     * @param id     The id of the player.
     */
    protected abstract void onItemPickUp(@NotNull Player player, int id);

    /**
     * Update the display item on the right side of the trade panel (i.e. the item that will be received).
     *
     * @param id     The id of the player.
     * @param slotId The item slot id.
     * @param item   The item to display.
     */
    protected abstract void updateDisplayItem(int id, int slotId, @Nullable ItemStack item);

    /**
     * @param id     The id of the player.
     * @param slotId The item slot id.
     * @return The item that is currently offered on the left side of the trade panel (i.e. the item that will be sent).
     */
    protected abstract @Nullable ItemStack getCurrentOfferedItem(int id, int slotId);

    /**
     * @param id     The id of the player.
     * @param slotId The item slot id.
     * @return The item that is currently displayed on the right side of the trade panel (i.e. the item that will be received).
     */
    protected abstract @Nullable ItemStack getCurrentDisplayedItem(int id, int slotId);

    /**
     * @return The players that are participating in the trade.
     */
    @NotNull
    protected abstract Stream<Player> getParticipants();

    protected abstract void onReadyStateChange(int id, boolean ready);

    void start() {
        buildPattern();     // Build pattern first to
        initializeGUIs();   // use it here for the inventory size.
        startListeners();
        startGUI();
        playStartSound();
    }

    protected void buildPattern() {
        this.pattern = TradeSystem.getInstance().getLayoutManager().getActive();
        buildSlots();

        this.layout[0] = pattern.build();
        this.layout[1] = pattern.build();
    }

    private void buildSlots() {
        slots.addAll(pattern.getSlotsOf(TradeSlot.class));
        Collections.sort(slots);

        //Sort other slots in a way that the trade layout is symmetrically.
        otherSlots.addAll(pattern.getSlotsOf(TradeSlotOther.class));
        otherSlots.sort((o1, o2) -> {
            int row1 = o1 / 9;
            int row2 = o2 / 9;

            if (row1 != row2) return Integer.compare(row1, row2);

            //reverse
            return Integer.compare(o2, o1);
        });
    }

    private void startListeners() {
        Bukkit.getPluginManager().registerEvents(this.pickupListener = getPickUpListener(), TradeSystem.getInstance());
    }

    /**
     * Update all displayed items on both sides (i.e. the items that one receive).
     *
     * @return True if something has changed.
     */
    private boolean updateDisplayedItems() {
        boolean change = false;

        if (isActive()) {
            for (int id = 0; id < 2; id++) {
                if (guis[id] == null) continue;

                int otherId = getOtherId(id);

                // update displayed items on other gui
                for (int slotId = 0; slotId < slots.size(); slotId++) {
                    ItemStack item = guis[id].getItem(slots.get(slotId));
                    ItemStack other = getCurrentDisplayedItem(otherId, slotId);

                    if (!Objects.equals(item, other)) {
                        change = true;
                        updateDisplayItem(otherId, slotId, item);
                        onTradeOfferChange(false);
                    }
                }
            }

            // update status icons after all items were updated
            for (int id = 0; id < 2; id++) {
                if (guis[id] == null) continue;
                updateStatusIcon(guis[id].getPlayer(), id);
            }
        }

        return change;
    }

    /**
     * Called when an offer has changed.
     *
     * @param invokeTradeUpdate True, if the current state should be updated. Active countdowns will be stopped then.
     */
    public void onTradeOfferChange(boolean invokeTradeUpdate) {
        if (TradeSystem.man().isRevokeReadyOnChange()) {
            setReadyState(0, false);
            setReadyState(1, false);
        }
        if (invokeTradeUpdate) update();
    }

    /**
     * Update the status icon of the given player.
     *
     * @param player The player whose status icon should be updated.
     * @param id     The id of the player.
     */
    protected void updateStatusIcon(@NotNull Player player, int id) {
        StatusIcon icon = layout[id].getIcon(StatusIcon.class);
        icon.updateButton(this, player);

        ShowStatusIcon showIcon = layout[id].getIcon(ShowStatusIcon.class);
        showIcon.updateButton(this, player);
    }

    /**
     * Update displayed items and start countdown if both players are ready.
     */
    public void update() {
        boolean someChange = updateDisplayedItems();
        if (someChange) closeShulkerPeekingGUIs();

        if (this.ready[0] && this.ready[1]) finish().whenComplete((suc, err) -> {
            if (err != null) err.printStackTrace();
            else if (suc) cleanUp();
        });
        else if (countdown != null) {
            playCountDownStopSound();
            countdown.cancel();
            countdownTicks = 0;
            countdown = null;
            synchronizeTitle();
        }

        // update inventory a tick later to fix some visualization bugs
        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> this.getViewers().forEach(Player::updateInventory), 1);
    }

    private boolean setReadyState(int id, boolean ready) {
        if (this.ready[id] == ready) return false;
        this.ready[id] = ready;
        onReadyStateChange(id, ready);
        return true;
    }

    /**
     * Update the ready state of the given player.
     *
     * @param id    The id of the player.
     * @param ready The new ready state.
     */
    private void updateReady(int id, boolean ready) {
        if (setReadyState(id, ready)) update();
    }

    /**
     * Update the trade in a few ticks.
     *
     * @param delay The delay in ticks.
     */
    public void updateLater(long delay) {
        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), this::update, delay);
    }

    /**
     * <b>Simulates</b> all trade icon exchanges.
     *
     * @param player       The player which tries to finish.
     * @param failDirectly True, if an error should call a trade cancellation.
     * @return {@link Boolean#TRUE} if the simulation had no issues.
     */
    protected boolean tryFinish(@NotNull Player player, boolean failDirectly) {
        int id = getId(player);

        Player other = getOther(player).orElse(null);
        String othersName = getOther(player.getName());

        for (TradeIcon icon : layout[id].getIcons()) {
            if (icon == null) continue;
            FinishResult result = icon.tryFinish(this, player, other, othersName, this.initiationServer);

            switch (result) {
                case ERROR_ECONOMY:
                    if (failDirectly) callEconomyError();
                    return false;

                case PASS:
                    break;
            }
        }

        return true;
    }

    @NotNull
    private CompletableFuture<Boolean> finish() {
        if (this.countdown != null) return CompletableFuture.completedFuture(false);
        if (!isActive()) return CompletableFuture.completedFuture(false);
        if (isPaused()) return CompletableFuture.completedFuture(false);

        return runCountdown().thenApply($ -> {
            // prepare finish before sending the finish-check packet (prevents the GUI from bugging out)
            for (int id = 0; id < 2; id++) {
                Player player = getPlayer(id);
                if (player == null) continue;

                if (!tryFinish(player, false)) return false;

                prepareFinish(player, id);
            }

            return true;
        }).thenCompose(ready -> ready ? canFinish() : CompletableFuture.completedFuture(false)).thenApply(ready -> {
            if (!ready) {
                callEconomyError();
                return false;
            }

            boolean logFinish = false;
            boolean[] droppedItems = new boolean[2];
            TradeResult[] results = createResults();

            // exchange goods
            for (int id = 0; id < 2; id++) {
                Player player = getPlayer(id);
                if (player == null) continue;

                boolean initiator = isInitiator(player, id);
                droppedItems[id] = exchangeItems(player, id, initiator);
                exchangeOtherGoods(player);

                if (initiator) logFinish = true;
            }

            // finish trade
            for (int id = 0; id < 2; id++) {
                Player player = getPlayer(id);
                postFinish(player, id, droppedItems[id], results[id]);
            }

            if (logFinish) TradeLogService.logLater(this.players[0], this.players[1], TradeLog.FINISHED.get(), 10);

            closeTrade(results);
            return true;
        });
    }

    @NotNull
    protected CompletableFuture<Void> runCountdown() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        int interval = TradeSystem.man().getCountdownInterval();
        int repetitions = TradeSystem.man().getCountdownRepetitions();
        this.countdown = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    this.cancel();
                    countdownTicks = 0;
                    countdown = null;
                    return;
                }

                if (!ready[0] || !ready[1]) {
                    this.cancel();
                    Trade.this.getViewers().forEach(p -> TradeSystem.man().playCountdownStopSound(p));
                    countdownTicks = 0;
                    countdown = null;
                    guis().forEach(TradingGUI::synchronizeTitle);
                    return;
                }

                if (countdownTicks == repetitions) {
                    future.complete(null);
                    this.cancel();
                    countdownTicks = 0;
                    countdown = null;
                    return;
                } else {
                    guis().forEach(TradingGUI::synchronizeTitle);
                    Trade.this.getViewers().forEach(p -> TradeSystem.man().playCountdownTickSound(p));
                }

                countdownTicks++;
            }
        };

        this.countdown.runTaskTimer(TradeSystem.getInstance(), 0, interval);

        return future;
    }

    private void prepareFinish(@NotNull Player player, int id) {
        pause[id] = true;
        player.closeInventory();
    }


    @NotNull
    private TradeResult[] createResults() {
        return new TradeResult[] {createResult(getPlayer(0), 0), createResult(getPlayer(1), 1)};
    }

    @NotNull
    private TradeResult createResult(@Nullable Player player, int id) {
        TradeResult result = player == null ? new TradeResult(id) : new PlayerTradeResult(this, player, id);

        for (int i = 0; i < slots.size(); i++) {
            result.add(getCurrentOfferedItem(id, i), false);
            result.add(getCurrentDisplayedItem(id, i), true);
        }

        for (TradeIcon icon : layout[id].getIcons()) {
            if (icon == null) continue;
            result.add(icon);
        }

        return result;
    }

    /**
     * @param player    The player who receives the items.
     * @param id        The id of the player who receives the items.
     * @param initiator Whether the player who receives the items initiated the trade.
     * @return True, if some items were dropped.
     */
    protected boolean exchangeItems(@NotNull Player player, int id, boolean initiator) {
        int otherId = getOtherId(id);
        Player other = getOther(player).orElse(null);

        boolean droppedItems = false;
        for (int slotId = 0; slotId < slots.size(); slotId++) {
            //using original one to prevent dupe glitches
            ItemStack item = removeReceivedItem(id, slotId);

            //Log before calling the events. These events could remove this item, and we would still lose it.
            if (item != null && item.getType() != Material.AIR)
                TradeLog.logItemReceive(player, initiator, players[otherId], getUniqueId(otherId), item);

            //call events
            item = callTradeItemEvent(player, other, players[otherId], item);

            //try fit into inventory
            if (item != null && item.getType() != Material.AIR) {
                int rest = checkItemFit(player, item);

                if (rest <= 0) player.getInventory().addItem(item);
                else {
                    ItemStack toDrop = item.clone();
                    toDrop.setAmount(rest);

                    item.setAmount(item.getAmount() - rest);
                    if (item.getAmount() > 0) player.getInventory().addItem(item);

                    droppedItems |= dropItem(player, toDrop);
                }
            }
        }

        return droppedItems;
    }

    protected void exchangeOtherGoods(@NotNull Player player) {
        int id = getId(player);

        Player other = getOther(player).orElse(null);
        String othersName = getOther(player.getName());

        for (TradeIcon icon : layout[id].getIcons()) {
            if (icon == null) continue;
            icon.onFinish(this, player, other, othersName, this.initiationServer);
        }
    }

    public void cancel() {
        cancel(null);
    }

    private void callEconomyError() {
        cancel(Lang.getPrefix() + Lang.get("Economy_Error"));
    }

    protected void cancelling(@Nullable String message) {
    }

    public void cancel(@Nullable String message) {
        cancel(message, false);
    }

    public synchronized void cancel(@Nullable String message, boolean alreadyCalled) {
        if (cancelling) return;  // already cancelling

        TradeResult[] results = createResults();

        this.cancelling = true;
        boolean[] droppedItems = returnItemsToOwner();

        boolean alreadyClosed = droppedItems == null;
        if (alreadyClosed) return;

        cleanUp();
        clearOpenAnvils();

        playCancelSound();
        closeInventories();

        // indicate inactive trade
        // DUPE fix: guis must be null AFTER closing all inventories
        this.guis[0] = null;
        this.guis[1] = null;

        TradeSystem.man().unregisterTrade(players[0]);
        TradeSystem.man().unregisterTrade(players[1]);

        if (!alreadyCalled) cancelling(message);

        if (message != null) {
            if (initiationServer) TradeLogService.log(players[0], players[1], TradeLog.CANCELLED_WITH_REASON.get(message));
            sendMessage(message);
        } else {
            if (initiationServer) TradeLogService.log(players[0], players[1], TradeLog.CANCELLED.get());

            for (int i = 0; i < 2; i++) {
                String m = Lang.getPrefix() + getPlaceholderMessage(i, "Trade_Was_Cancelled");
                sendMessage(i, m);
            }
        }

        for (int i = 0; i < droppedItems.length; i++) {
            if (droppedItems[i]) {
                sendMessage(i, Lang.getPrefix() + getPlaceholderMessage(i, "Items_Dropped"));
            }
        }

        closeTrade(results);
    }

    private void postFinish(@Nullable Player player, int id, boolean droppedItems, @NotNull TradeResult result) {
        if (guis[id] != null) guis[id].clear();
        TradeSystem.man().unregisterTrade(players[id]);

        PlayerTradeResult playerResult = result instanceof PlayerTradeResult ? (PlayerTradeResult) result : null;
        if (player != null && playerResult != null) {
            int oId = getOtherId(id);
            TradeReportEvent e = getPlayerOpt(oId)
                    .map(other -> new TradeReportEvent(player, other, playerResult))
                    .orElseGet(() -> new TradeReportEvent(player, players[oId], getUniqueId(oId), playerResult));
            Bukkit.getPluginManager().callEvent(e);

            if (!e.isCancelled()) player.sendMessage(buildFinishMessages(player, id, droppedItems, playerResult, e));
            if (e.isPlayFinishSound()) TradeSystem.man().playFinishSound(player);
        }
    }

    /**
     * At this state, the trade is already unregistered. This is just a last opportunity to do something with this trade instance.
     *
     * @param results The results of the trade.
     */
    private void closeTrade(@NotNull TradeResult @NotNull [] results) {
        callFinishEvent(results);
    }

    private void callFinishEvent(@NotNull TradeResult @NotNull [] results) {
        Player player = getPlayer(0);
        assert player != null;

        TradeFinishEvent e;
        if (isInitiator(player, 0)) {
            e = getPlayerOpt(1)
                    .map(other -> new TradeFinishEvent(player, other, !cancelling, results))
                    .orElseGet(() -> new TradeFinishEvent(player, players[1], getUniqueId(1), !cancelling, results));
        } else {
            e = getPlayerOpt(1)
                    .map(other -> new TradeFinishEvent(other, player, !cancelling, results))
                    .orElseGet(() -> new TradeFinishEvent(players[1], getUniqueId(1), player, !cancelling, results));
        }

        Bukkit.getPluginManager().callEvent(e);
    }

    private @NotNull String @NotNull [] buildFinishMessages(@NotNull Player player, int id, boolean droppedItems, @NotNull PlayerTradeResult result, @NotNull TradeReportEvent event) {
        List<String> messages = new ArrayList<>();
        messages.add(Lang.getPrefix() + getPlaceholderMessage(id, "Trade_Was_Finished"));

        // collect reports and sort them
        List<String> list = new ArrayList<>();

        if (TradeSystem.man().isTradeReportEconomy()) {
            if (event.getEconomyReport() != null) list.addAll(event.getEconomyReport());
            else list.addAll(result.buildEconomyReport());
        }
        if (TradeSystem.man().isTradeReportItems()) {
            if (event.getItemReport() != null) list.addAll(event.getItemReport());
            else list.addAll(result.buildItemReport());
        }

        list.sort((o1, o2) -> {
            o1 = ChatColor.stripColor(o1).replaceFirst("\\d+(x)?", "");
            o2 = ChatColor.stripColor(o2).replaceFirst("\\d+(x)?", "");
            return o1.compareTo(o2);
        });
        messages.addAll(list);

        if (droppedItems) {
            messages.add("");
            messages.add(Lang.getPrefix() + Lang.get("Items_Dropped", player));
        }

        return messages.toArray(new String[0]);
    }

    private void cleanUp() {
        stopListeners();
    }

    protected @Nullable ItemStack callTradeItemEvent(@NotNull Player receiver, @Nullable Player sender, @NotNull String senderName, @Nullable ItemStack item) {
        if (item == null) return null;

        TradeItemEvent event = sender == null ? new TradeItemEvent(receiver, senderName, getUniqueId(senderName), item) : new TradeItemEvent(receiver, sender, item);
        Bukkit.getPluginManager().callEvent(event);
        return event.getItem();
    }

    /**
     * Close shulker peeking GUIs in case of trade offer changes.
     */
    private void closeShulkerPeekingGUIs() {
        getViewers().forEach(p -> {
            ShulkerPeekGUI shulkerPeek = API.getRemovable(p, ShulkerPeekGUI.class);
            if (shulkerPeek == null) return;

            boolean ownShulkerBox = getSlots().contains(shulkerPeek.getOriginalSlot());
            if (!ownShulkerBox) {
                try {
                    TradeSystem.man().playChangeDuringShulkerPeekSound(p);
                    shulkerPeek.close();
                } catch (AlreadyClosedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * This method moves all for trade placed items back to the item owner.
     * Useful when cancelling a trade.
     *
     * @return A tuple of booleans whether the players dropped items or not.
     */
    protected final boolean[] returnItemsToOwner() {
        if (!isActive()) return null;

        boolean[] droppedItems = new boolean[] {false, false};

        // move items to their owners
        for (Integer slot : this.slots) {
            for (int id = 0; id < 2; id++) {
                if (this.guis[id] != null) {
                    Player player = this.guis[id].getPlayer();

                    if (this.guis[id].getItem(slot) != null && this.guis[id].getItem(slot).getType() != Material.AIR) {
                        ItemStack item = this.guis[id].getItem(slot);
                        droppedItems[id] |= addOrDropItem(player, item);
                    }
                }
            }
        }

        // check the cursor of every viewer
        for (int id = 0; id < 2; id++) {
            if (this.guis[id] == null) continue;

            Player player = this.guis[id].getPlayer();
            droppedItems[id] |= moveCursorItemToInventory(player);
        }

        // check cursor of viewers which are not the main participants
        this.getViewers().filter(nonTrader()).forEach(this::moveCursorItemToInventory);

        return droppedItems;
    }

    /**
     * Check if the player has an item in their cursor and move it to their inventory.
     *
     * @param player The player whose cursor should be moved to the inventory.
     * @return True, if the player dropped the item.
     */
    private boolean moveCursorItemToInventory(@NotNull Player player) {
        ItemStack item = player.getOpenInventory().getCursor();
        if (item != null && item.getType() != Material.AIR) {
            boolean dropped = addOrDropItem(player, item);
            player.getOpenInventory().setCursor(null);
            return dropped;
        } else return false;
    }

    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    protected boolean canPickup(Player player, ItemStack item) {
        PlayerInventory inv = new PlayerInventory(player, false);

        for (Integer slot : this.slots) {
            ItemStack back = guis[getId(player)].getItem(slot);
            if (back != null && back.getType() != Material.AIR) {
                inv.addItem(back);
            }
        }

        //placeholder
        if (this.cursor[getId(player)]) {
            ItemStack cursor = new ItemBuilder(XMaterial.BEDROCK).setName("PLACEHOLDER_CURSOR").getItem();
            if (!inv.addItem(cursor, false)) return false;
        }

        return inv.addItem(item);
    }

    @NotNull
    private Listener getPickUpListener() {
        //noinspection deprecation
        return new Listener() {
            @EventHandler
            public void onPickup(PlayerPickupItemEvent e) {
                for (int i = 0; i < 2; i++) {
                    final int id = i;
                    if (guis[id] == null) continue;

                    if (e.getPlayer().getName().equals(players[id])) {
                        if (!canPickup(e.getPlayer(), e.getItem().getItemStack()) || waitForPickup[id]) e.setCancelled(true);
                        else {
                            //player picked up an item, check trading items -> balance items of other trader
                            Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> onItemPickUp(e.getPlayer(), id), 1);
                        }
                    }
                }
            }
        };
    }

    /**
     * Balances the items of the trader given in "player" to make them fit into the inventory of the trade partner.
     * Items will be removed from the trade panel if they does not fit into the inventory.
     *
     * @param playerId The id of the trader whose items will be balanced.
     */
    public final void cancelItemOverflow(int playerId) {
        Player player = getPlayer(playerId);
        if (player == null) return;
        cancelItemOverflow(player, playerId);
    }

    /**
     * Balances the items of the trader given in "player" to make them fit into the inventory of the trade partner.
     * Items will be removed from the trade panel if they does not fit into the inventory.
     *
     * @param player The trader whose items will be balanced.
     */
    protected void cancelItemOverflow(@NotNull Player player, int id) {
        if (!isActive()) return;

        HashMap<Integer, ItemStack> items = new HashMap<>();
        for (Integer slot : this.slots) {
            ItemStack item = this.guis[id].getItem(slot);

            if (item != null && item.getType() != Material.AIR) {
                items.put(slot, item);
            }
        }

        if (items.isEmpty()) return;

        HashMap<Integer, ItemStack> sorted = new HashMap<>();
        int size = items.size();
        for (int i = 0; i < size; i++) {
            int slot = 0;
            ItemStack item = null;

            for (int nextSlot : items.keySet()) {
                ItemStack next = items.get(nextSlot);

                if (item == null || item.getAmount() > next.getAmount()) {
                    item = next;
                    slot = nextSlot;
                }
            }

            if (item != null) {
                sorted.put(slot, item);
                items.remove(slot);
            }
        }

        items.clear();
        items.putAll(sorted);
        sorted.clear();

        PlayerInventory inv = getPlayerInventory(getOtherId(id));
        HashMap<Integer, Integer> toRemove = new HashMap<>();

        items.forEach((slot, item) -> {
            int amount = inv.addUntilPossible(item, true);
            if (amount > 0) toRemove.put(slot, amount);
        });

        items.clear();

        TradingGUI gui = guis[id];
        for (Integer slot : toRemove.keySet()) {
            ItemStack item = gui.getItem(slot).clone();
            item.setAmount(item.getAmount() - toRemove.get(slot));

            ItemStack transport = gui.getItem(slot).clone();
            transport.setAmount(toRemove.get(slot));

            player.getInventory().addItem(transport);
            gui.setItem(slot, item.getAmount() <= 0 ? new ItemStack(Material.AIR) : item);
        }

        update();
    }

    /**
     * Checks if the given collection of items can be picked up by the trade partner of the given player without the slots in 'avoid'.
     *
     * @param from  The trading player.
     * @param avoid The slots to ignore.
     * @param items The collection of items that should be checked if they fit into the inventory of the trade partner.
     * @return True if the given items fit into the inventory of the trade partner.
     */
    public boolean fitsTrade(@NotNull Player from, @NotNull List<Integer> avoid, @NotNull Collection<ItemStack> items) {
        List<ItemStack> currentlyAddingItems = new ArrayList<>(items);
        TradingGUI gui = this.guis[getId(from)];

        for (Integer slot : this.slots) {
            if (avoid.contains(slot)) continue;

            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                currentlyAddingItems.add(item);
            }
        }

        PlayerInventory inv = getPlayerInventory(getOtherId(from));
        boolean fits = true;

        for (ItemStack item : currentlyAddingItems) {
            if (!inv.addItem(item)) {
                fits = false;
                break;
            }
        }

        currentlyAddingItems.clear();
        avoid.clear();
        return fits;
    }

    protected final void sendMessage(@NotNull String message) {
        getViewers().forEach(player -> player.sendMessage(message));
    }

    public boolean dropItem(Player player, ItemStack itemStack) {
        if (player == null || itemStack == null || itemStack.getType() == Material.AIR) return false;
        player.getWorld().dropItem(player.getLocation().add(0, 0.1, 0), itemStack);
        return true;
    }

    private void stopListeners() {
        if (this.pickupListener != null) {
            HandlerList.unregisterAll(this.pickupListener);
            this.pickupListener = null;
        }
    }

    public int getOtherId(@Range (from = 0, to = 1) int id) {
        if (id == 1) return 0;
        else return 1;
    }

    public int getOtherId(@NotNull Player player) {
        return getOtherId(getId(player));
    }

    public int getId(@NotNull Player player) {
        return getId(player.getName());
    }

    public int getId(@NotNull String player) {
        if (player.equalsIgnoreCase(this.players[0])) return 0;
        else if (player.equalsIgnoreCase(this.players[1])) return 1;
        else return -1;
    }

    @NotNull
    public UUID getUniqueId(@NotNull String player) {
        return getUniqueId(getId(player));
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public List<Integer> getOtherSlots() {
        return otherSlots;
    }

    /**
     * Checks if the given collection of items can be picked up by the trade partner of the given player without the slots in 'avoid'.
     *
     * @param from The trading player.
     * @param item The item that should be checked if it does not fit into the inventory of the trade partner.
     * @return True if the given item does not fit into the inventory of the trade partner.
     */
    public boolean doesNotFit(@NotNull Player from, @NotNull ItemStack item) {
        return doesNotFit(from, new ArrayList<>(), item);
    }

    /**
     * Checks if the given collection of items can be picked up by the trade partner of the given player without the slots in 'avoid'.
     *
     * @param from  The trading player.
     * @param avoid The slots to ignore.
     * @param item  The item that should be checked if it does not fit into the inventory of the trade partner.
     * @return True if the given item does not fit into the inventory of the trade partner.
     */
    public boolean doesNotFit(@NotNull Player from, @NotNull List<Integer> avoid, @NotNull ItemStack item) {
        return !fitsTrade(from, avoid, new ArrayList<ItemStack>() {{
            add(item);
        }});
    }

    /**
     * Checks if the given collection of items can be picked up by the trade partner of the given player without the slots in 'avoid'.
     *
     * @param from  The trading player.
     * @param items The collection of items that should be checked if they fit into the inventory of the trade partner.
     * @return True if the given items fit into the inventory of the trade partner.
     */
    public boolean fitsTrade(@NotNull Player from, @NotNull Collection<ItemStack> items) {
        return fitsTrade(from, new ArrayList<>(), items);
    }

    /**
     * Check if the item fits into the inventory of the given player.
     *
     * @param player The player that should be analyzed.
     * @param item   The item that should be checked.
     * @return The amount which doesn't fit.
     */
    public static int checkItemFit(@NotNull Player player, @NotNull ItemStack item) {
        int amount = item.getAmount();

        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = player.getInventory().getContents()[i];

            if (itemStack == null || itemStack.getType().equals(Material.AIR)) return 0;
            if (itemStack.isSimilar(item) && itemStack.getAmount() < itemStack.getMaxStackSize()) {
                amount -= itemStack.getMaxStackSize() - itemStack.getAmount();
            }

            if (amount <= 0) return 0;
        }

        return amount;
    }

    /**
     * @param player The player that should pick up the provided item
     * @param item   The item that should be added.
     * @return True if this item was dropped. False if this item was added to the inventory.
     */
    private boolean addOrDropItem(@NotNull Player player, @NotNull ItemStack item) {
        int fit = checkItemFit(player, item);

        if (item.getAmount() > fit) {
            //move remaining items into inventory
            item.setAmount(item.getAmount() - fit);
            player.getInventory().addItem(item);
        }

        if (fit > 0) {
            //drop not fitting items
            item.setAmount(fit);
            return dropItem(player, item);
        } else return false;
    }

    public void synchronizeTradeIcon(int playerId, TradeIcon icon, boolean updateIcon) {
        if (icon instanceof Transition) {
            int otherId = getOtherId(playerId);
            informTransition(icon, otherId);
        }

        if (updateIcon) icon.updateItem(this, playerId);
    }

    protected void informTransition(TradeIcon icon, int otherId) {
        try {
            Method method = findInform(icon.getClass(), icon.getClass());

            TradeIcon consumer = getLayout()[otherId].getIcon(IconHandler.getTransitionTarget(icon.getClass()));
            method.invoke(icon, consumer);
            consumer.updateItem(this, otherId);
        } catch (ClassCastException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
            throw new IllegalStateException("Cannot execute method inform(TradeIcon) of " + icon.getClass().getName(), ex);
        }
    }

    public void handleClickResult(@NotNull TradeIcon tradeIcon, @NotNull Player player, int playerId, @NotNull GUI gui, @NotNull IconResult result) {
        switch (result) {
            case PASS:
                return;

            case UPDATE:
                //calls an update
                onTradeOfferChange(true);

                synchronizeTradeIcon(playerId, tradeIcon, true);

                // make sure player get notified when something changed
                closeShulkerPeekingGUIs();

                // status icon might can be ready now
                updateStatusIcon(player, playerId);
                break;

            case GUI:
                try {
                    gui.open();
                } catch (AlreadyOpenedException ignored) {
                } catch (NoPageException | IsWaitingException e) {
                    e.printStackTrace();
                }
                break;

            case READY:
                updateReady(playerId, true);
                break;

            case NOT_READY:
                updateReady(playerId, false);
                break;

            case CANCEL:
                cancel();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + result);
        }
    }

    private @NotNull Method findInform(Class<? extends TradeIcon> origin, Class<? extends TradeIcon> icon) throws NoSuchMethodException {
        try {
            return icon.getMethod("inform", IconHandler.getTransitionTarget(origin));
        } catch (NoSuchMethodException ignored) {
        }

        //might be a generic
        try {
            return icon.getMethod("inform", TradeIcon.class);
        } catch (NoSuchMethodException ignored) {
        }

        //transition without transition method?
        throw new NoSuchMethodException();
    }

    protected final void synchronizeTitle() {
        guis().forEach(TradingGUI::synchronizeTitle);
    }

    protected final void closeInventories() {
        this.getViewers().forEach(p -> {
            p.closeInventory();
            p.updateInventory();
        });

        guis().forEach(TradingGUI::destroy);

        // fix buggy inventories of other plugins that were opened while trading
        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> this.getViewers().forEach(p -> {
            p.closeInventory();
            p.updateInventory();
        }));
    }

    public boolean[] getCursor() {
        return cursor;
    }

    public boolean[] getWaitForPickup() {
        return waitForPickup;
    }

    public BukkitRunnable getCountdown() {
        return countdown;
    }

    public int getCountdownTicks() {
        return countdownTicks;
    }

    public String getOther(String p) {
        if (this.players[0] == null || this.players[1] == null) return null;

        if (this.players[0].equals(p)) return this.players[1];
        else return this.players[0];
    }

    public TradeLayout[] getLayout() {
        return this.layout;
    }

    public boolean[] getPause() {
        return pause;
    }

    @NotNull
    private Stream<TradingGUI> guis() {
        return Arrays.stream(guis).filter(Objects::nonNull);
    }

    public TradingGUI[] getGUIs() {
        return guis;
    }

    public boolean inMainGUI(Player player) {
        int id = getId(player);
        if (id == -1) return false;

        TradingGUI gui = guis[id];
        return gui.isOpen() && !gui.isWaiting();
    }

    public void acknowledgeGuiSwitch(@NotNull Player player) {
        // Fixes a dupe glitch which allowed the player to remain in the trade GUI during the countdown and then duplicate items.
        updateReady(getId(player), false);
    }

    protected final void playCountDownStopSound() {
        this.getViewers().forEach(p -> TradeSystem.man().playCountdownStopSound(p));
    }

    protected final void playStartSound() {
        this.getViewers().forEach(p -> TradeSystem.man().playStartSound(p));
    }

    protected final void playCancelSound() {
        this.getViewers().forEach(p -> TradeSystem.man().playCancelSound(p));
    }

    public boolean isInitiationServer() {
        return initiationServer;
    }

    public String[] getPlayers() {
        return players;
    }

    public boolean isCancelling() {
        return cancelling;
    }

    public boolean[] getReady() {
        return ready;
    }

    @NotNull
    private Predicate<Player> nonTrader() {
        return player -> getId(player) == -1;
    }

    /**
     * @return The players that currently see this trade.
     */
    @NotNull
    public final Stream<Player> getViewers() {
        // Preparation for future features.
        return getParticipants();
    }

    /**
     * @param player The current player.
     * @return The other player.
     */
    @NotNull
    public Optional<Player> getOther(@NotNull Player player) {
        int id = getId(player);
        if (id == -1) return Optional.empty();
        return getPlayerOpt(getOtherId(id));
    }

    @NotNull
    protected Optional<Player> getPlayerOpt(int id) {
        return Optional.ofNullable(getPlayer(id));
    }

    protected void sendMessage(int id, @NotNull String message) {
        getPlayerOpt(id).ifPresent(p -> p.sendMessage(message));
    }

    @NotNull
    protected String getPlaceholderMessage(int playerId, @NotNull String message) {
        // Player with id 0 can be used as backup since we always have at least one player.
        return Lang.get(message, getPlayerOpt(playerId).orElse(getPlayer(0)));
    }
}
