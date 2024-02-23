package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyClosedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
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
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.EditorInfo;
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
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.trade.subscribe.PlayerSubscriber;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Stream;

public abstract class Trade {
    protected final String[] names = new String[2];
    protected final boolean initiationServer;
    protected final TradeLayout[] layout = new TradeLayout[2];
    protected final TradingGUI[] guis = new TradingGUI[2];
    protected final List<Integer> slots = new ArrayList<>();
    protected final List<Integer> otherSlots = new ArrayList<>();
    private final Set<Runnable> subscribers = new HashSet<>();

    protected final boolean[] ready = new boolean[]{false, false};
    protected final boolean[] pause = new boolean[]{false, false};

    protected Pattern pattern;
    protected Listener pickupListener;
    protected BukkitRunnable countdown = null;
    protected int countdownTicks = 0;
    protected boolean cancelling = false;

    protected Trade(String player0, String player1, boolean initiationServer) {
        this.initiationServer = initiationServer;
        this.names[0] = player0;
        this.names[1] = player1;
    }

    /**
     * Initialize trading GUIs for all participants.
     */
    protected abstract void initializeGUIs();

    /**
     * Create trading GUIs for all participants.
     */
    protected abstract void createGUIs();

    /**
     * Open trading GUIs for all participants.
     */
    protected abstract void startGUIs();

    /**
     * Note: Player with id=0 is never null.
     *
     * @param perspective The perspective that should be checked.
     * @return The player with the given id.
     */
    @Nullable
    public abstract Player getPlayer(@NotNull Perspective perspective);

    /**
     * @param perspective The perspective that should be checked.
     * @return The world name of the player with the given perspective.
     */
    @NotNull
    public abstract String getWorld(@NotNull Perspective perspective);

    /**
     * @param perspective The perspective that should be checked.
     * @return The server name of the player with the given perspective.
     */
    @Nullable
    public abstract String getServer(@NotNull Perspective perspective);

    /**
     * @param perspective The perspective that should be checked.
     * @return The {@link UUID} of the player with the given id.
     */
    @NotNull
    public abstract UUID getUniqueId(@NotNull Perspective perspective);

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
     * @param perspective The perspective that should be checked.
     * @return True if the perspective is the initiator of the trade.
     */
    protected abstract boolean isInitiator(@NotNull Perspective perspective);

    /**
     * @param perspective The perspective that should be checked.
     * @return The inventory of the player including the current cursor.
     */
    @NotNull
    protected abstract PlayerInventory getPlayerInventory(@NotNull Perspective perspective);

    /**
     * Synchronizes the inventory of the player to prepare for checking item overflow or other things.
     *
     * @param perspective The perspective that should be checked.
     */
    public abstract void synchronizePlayerInventory(@NotNull Perspective perspective);

    /**
     * @param perspective The perspective that received the item.
     * @param slotId      The slot of the item to receive (left side slots).
     * @return The item to receive.
     */
    @Nullable
    protected abstract ItemStack removeReceivedItem(@NotNull Perspective perspective, int slotId);

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
     * @param perspective The perspective that picked up an item.
     */
    protected abstract void onItemPickUp(@NotNull Perspective perspective);

    /**
     * Update the display item on the right side of the trade panel (i.e. the item that will be received).
     *
     * @param perspective The perspective that should be checked.
     * @param slotId      The item slot id.
     * @param item        The item to display.
     */
    public abstract void updateDisplayItem(@NotNull Perspective perspective, int slotId, @Nullable ItemStack item);

    /**
     * @param perspective The perspective that should be checked.
     * @param slotId      The item slot id.
     * @return The item that is currently offered on the left side of the trade panel (i.e. the item that will be sent).
     */
    public abstract @Nullable ItemStack getCurrentOfferedItem(@NotNull Perspective perspective, int slotId);

    /**
     * @param perspective The perspective that should be checked.
     * @param slotId      The item slot id.
     * @return The item that is currently displayed on the right side of the trade panel (i.e. the item that will be received).
     */
    protected abstract @Nullable ItemStack getCurrentDisplayedItem(@NotNull Perspective perspective, int slotId);

    /**
     * Marks this trade instance ready for all types of packets.
     *
     * @return The future that must be called to continue starting this trade.
     */
    protected abstract @NotNull CompletableFuture<Void> markAsInitialized();

    /**
     * @return The players that are participating in the trade.
     */
    @NotNull
    protected abstract Stream<Player> getParticipants();

    protected abstract void onReadyStateChange(@NotNull Perspective perspective, boolean ready);

    void start() {
        buildPattern();     // Build pattern first to
        initializeGUIs();   // use it here for the inventory size.
        startListeners();

        // create guis, synchronize inventories and open them
        createGUIs();

        // mark this trade as initialized and wait before sending any packets
        markAsInitialized().whenComplete((v, t) -> {
            if (t != null) {
                TradeSystem.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize trade", t);
                cancel();
                return;
            }

            // synchronize inventories
            synchronizePlayerInventory(Perspective.PRIMARY);
            synchronizePlayerInventory(Perspective.SECONDARY);

            // open guis
            startGUIs();

            // play start sound
            playStartSound();
        });
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
     * @return The perspectives that have changed their displayed items.
     */
    @NotNull
    private Set<Perspective> updateDisplayedItems() {
        Set<Perspective> change = new HashSet<>();

        if (isActive()) {
            for (Perspective perspective : Perspective.main()) {
                if (guis[perspective.id()] == null) continue;

                // update displayed items on other gui
                for (int slotId = 0; slotId < slots.size(); slotId++) {
                    ItemStack item = guis[perspective.id()].getItem(slots.get(slotId));
                    ItemStack other = getCurrentDisplayedItem(perspective.flip(), slotId);

                    if (!Objects.equals(item, other)) {
                        change.add(perspective);
                        updateDisplayItem(perspective.flip(), slotId, item);
                        onTradeOfferChange(false);
                    }
                }
            }

            // update status icons after all items were updated
            for (Perspective perspective : Perspective.main()) {
                if (guis[perspective.id()] == null) continue;
                updateStatusIcon(perspective);
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
        if (TradeSystem.handler().isRevokeReadyOnChange()) {
            setReadyState(Perspective.PRIMARY, false);
            setReadyState(Perspective.SECONDARY, false);
        }
        if (invokeTradeUpdate) update();
    }

    /**
     * Update the status icon of the given player.
     *
     * @param perspective The perspective of the player.
     */
    protected void updateStatusIcon(@NotNull Perspective perspective) {
        Player player = getPlayer(perspective);
        if (player == null) return;

        StatusIcon icon = layout[perspective.id()].getIcon(StatusIcon.class);
        icon.updateButton(this, player);

        ShowStatusIcon showIcon = layout[perspective.id()].getIcon(ShowStatusIcon.class);
        showIcon.updateButton(this, player);
    }

    /**
     * Registers runnables that will be executed when the trade is updated.
     *
     * @param runnable The runnable to register.
     */
    public void subscribe(@NotNull Runnable runnable) {
        subscribers.add(runnable);
    }

    /**
     * Unregisters runnables that will be executed when the trade is updated.
     *
     * @param runnable The runnable to unregister.
     */
    public void unsubscribe(@NotNull Runnable runnable) {
        subscribers.remove(runnable);
    }

    /**
     * Update displayed items and start countdown if both players are ready.
     */
    public void update() {
        Set<Perspective> someChange = updateDisplayedItems();
        if (!someChange.isEmpty()) closeShulkerPeekingGUIs(someChange);

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

        subscribers.forEach(Runnable::run);

        // update inventory a tick later to fix some visualization bugs
        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> this.getViewers().forEach(Player::updateInventory));
    }

    private boolean setReadyState(@NotNull Perspective perspective, boolean ready) {
        if (this.ready[perspective.id()] == ready) return false;
        this.ready[perspective.id()] = ready;
        onReadyStateChange(perspective, ready);
        return true;
    }

    /**
     * Update the ready state of the given player.
     *
     * @param perspective The perspective of the player.
     * @param ready       The new ready state.
     */
    private void updateReady(@NotNull Perspective perspective, boolean ready) {
        if (setReadyState(perspective, ready)) update();
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
     * Update the trade in the next tick.
     */
    public void updateLater() {
        updateLater(0);
    }

    /**
     * <b>Simulates</b> all trade icon exchanges.
     *
     * @param perspective The perspective of the player.
     * @return {@link Boolean#TRUE} if the simulation had no issues.
     */
    protected boolean tryFinish(@NotNull Perspective perspective) {
        Player player = getPlayer(perspective);
        if (player == null) return false;

        for (TradeIcon icon : layout[perspective.id()].getIcons()) {
            if (icon == null) continue;

            // check if all requirements for the traded goods are still available
            EditorInfo info = IconHandler.getInfo(icon.getClass());
            if (!info.matchRequirements()) {
                TradeSystem.getInstance().getLogger().warning("Could not finish a trade between players '" + getNames()[0] + "' and '" + getNames()[1] + "'. Reason: The requirements for icon '" + info.getName() + "' are not met. Maybe a plugin has been disabled?");
                return false;
            }

            FinishResult result = icon.tryFinish(this, perspective, player, this.initiationServer);
            switch (result) {
                case ERROR_ECONOMY:
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
            for (Perspective perspective : Perspective.main()) {
                Player player = getPlayer(perspective);
                if (player == null) continue;

                if (!tryFinish(perspective)) return false;
                prepareFinish(perspective);
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

            // exchange items
            for (Perspective perspective : Perspective.main()) {
                Player player = getPlayer(perspective);
                if (player == null) continue;

                boolean initiator = isInitiator(perspective);
                droppedItems[perspective.id()] = exchangeItems(perspective, initiator);

                if (initiator) logFinish = true;
            }

            // prepare and send report
            for (Perspective perspective : Perspective.main()) {
                sendReport(perspective, droppedItems[perspective.id()], results[perspective.id()]);
            }

            // exchange other goods AFTER sending the report for trade icon conformity
            for (Perspective perspective : Perspective.main()) {
                exchangeOtherGoods(perspective);
            }

            // finish trade
            for (Perspective perspective : Perspective.main()) {
                postFinish(perspective);
            }

            if (logFinish) TradeLogService.logLater(this.names[0], this.names[1], TradeLog.FINISHED.get(), 10);

            closeTrade(results);
            return true;
        });
    }

    @NotNull
    protected CompletableFuture<Void> runCountdown() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        int interval = TradeSystem.handler().getCountdownInterval();
        int repetitions = TradeSystem.handler().getCountdownRepetitions();
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
                    Trade.this.getViewers().forEach(p -> TradeSystem.handler().playCountdownStopSound(p));
                    countdownTicks = 0;
                    countdown = null;

                    subscribers.forEach(Runnable::run);
                    guis().forEach(TradingGUI::synchronizeTitle);
                    return;
                }

                subscribers.forEach(Runnable::run);
                if (countdownTicks == repetitions) {
                    future.complete(null);
                    this.cancel();
                    countdownTicks = 0;
                    countdown = null;
                    return;
                } else {
                    guis().forEach(TradingGUI::synchronizeTitle);
                    Trade.this.getViewers().forEach(p -> TradeSystem.handler().playCountdownTickSound(p));
                }

                countdownTicks++;
            }
        };

        this.countdown.runTaskTimer(TradeSystem.getInstance(), 0, interval);

        return future;
    }

    private void prepareFinish(@NotNull Perspective perspective) {
        Player player = getPlayer(perspective);
        if (player == null) return;

        pause[perspective.id()] = true;
        player.closeInventory();
    }


    @NotNull
    private TradeResult[] createResults() {
        return new TradeResult[]{createResult(Perspective.PRIMARY), createResult(Perspective.SECONDARY)};
    }

    @NotNull
    private TradeResult createResult(@NotNull Perspective perspective) {
        if (perspective.isTertiary()) throw new IllegalArgumentException("Perspective cannot be tertiary.");

        Player player = getPlayer(perspective);
        UUID id = getUniqueId(perspective);
        TradeResult result = player == null ? new TradeResult(id, getWorld(perspective), getServer(perspective), perspective) : new PlayerTradeResult(this, player, perspective);

        for (int i = 0; i < slots.size(); i++) {
            result.add(getCurrentOfferedItem(perspective, i), false);
            result.add(getCurrentDisplayedItem(perspective, i), true);
        }

        for (TradeIcon icon : layout[perspective.id()].getIcons()) {
            if (icon == null) continue;
            result.add(icon);
        }

        return result;
    }

    /**
     * @param perspective The perspective of the player.
     * @param initiator   Whether the player who receives the items initiated the trade.
     * @return True, if some items were dropped.
     */
    protected boolean exchangeItems(@NotNull Perspective perspective, boolean initiator) {
        Player player = getPlayer(perspective);
        if (player == null) throw new IllegalStateException("Player cannot be null!");
        Player other = getPlayer(perspective.flip());

        boolean droppedItems = false;
        for (int slotId = 0; slotId < slots.size(); slotId++) {
            //using original one to prevent dupe glitches
            ItemStack item = removeReceivedItem(perspective, slotId);

            //Log before calling the events. These events could remove this item, and we would still lose it.
            if (item != null && item.getType() != Material.AIR)
                TradeLog.logItemReceive(player, initiator, names[perspective.flip().id()], getUniqueId(perspective.flip()), item);

            //call events
            item = callTradeItemEvent(player, other, names[perspective.flip().id()], item);

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

    protected void exchangeOtherGoods(@NotNull Perspective perspective) {
        Player player = getPlayer(perspective);
        if (player == null) return;

        for (TradeIcon icon : layout[perspective.id()].getIcons()) {
            if (icon == null) continue;
            icon.onFinish(this, perspective, player, this.initiationServer);
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

        TradeSystem.handler().unregisterTrade(names[0]);
        TradeSystem.handler().unregisterTrade(names[1]);

        if (!alreadyCalled) cancelling(message);

        if (message != null) {
            if (initiationServer)
                TradeLogService.log(names[0], names[1], TradeLog.CANCELLED_WITH_REASON.get(message));
            sendMessage(message);
        } else {
            if (initiationServer) TradeLogService.log(names[0], names[1], TradeLog.CANCELLED.get());

            getViewers().forEach(player -> {
                Perspective perspective = getPerspective(player);

                if (perspective.isMain()) {
                    String m = Lang.getPrefix() + getPlaceholderMessage(perspective, "Trade_Was_Cancelled");
                    sendMessage(perspective, m);
                } else {
                    Lang.send(player, "Trade_Was_Cancelled");
                }
            });
        }


        for (Perspective perspective : Perspective.main()) {
            if (droppedItems[perspective.id()]) {
                sendMessage(perspective, Lang.getPrefix() + getPlaceholderMessage(perspective, "Items_Dropped"));
            }
        }

        closeTrade(results);
    }

    private void sendReport(@NotNull Perspective perspective, boolean droppedItems, @NotNull TradeResult result) {
        Player player = getPlayer(perspective);
        PlayerTradeResult playerResult = result instanceof PlayerTradeResult ? (PlayerTradeResult) result : null;
        if (player != null && playerResult != null) {
            TradeReportEvent e = getPlayerOpt(perspective.flip())
                    .map(other -> new TradeReportEvent(player, other, playerResult))
                    .orElseGet(() -> new TradeReportEvent(player, names[perspective.flip().id()], getUniqueId(perspective.flip()), playerResult));
            Bukkit.getPluginManager().callEvent(e);

            if (!e.isCancelled())
                player.sendMessage(buildFinishMessages(player, perspective, droppedItems, playerResult, e));
            if (e.isPlayFinishSound()) TradeSystem.handler().playFinishSound(player);
        }
    }

    private void postFinish(@NotNull Perspective perspective) {
        if (guis[perspective.id()] != null) guis[perspective.id()].clear();
        TradeSystem.handler().unregisterTrade(names[perspective.id()]);
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
        Player player = getPlayer(Perspective.PRIMARY);
        assert player != null;

        TradeFinishEvent e;
        if (isInitiator(Perspective.PRIMARY)) {
            e = getPlayerOpt(Perspective.SECONDARY)
                    .map(other -> new TradeFinishEvent(player, other, !cancelling, results))
                    .orElseGet(() -> new TradeFinishEvent(player, names[Perspective.SECONDARY.id()], getUniqueId(Perspective.SECONDARY), !cancelling, results));
        } else {
            // switch trade result order since we are NOT on the initiating server;
            // we are marked as PRIMARY perspective, but we are actually SECONDARY
            TradeResult[] swapped = new TradeResult[2];
            swapped[0] = results[1];
            swapped[1] = results[0];

            e = getPlayerOpt(Perspective.SECONDARY)
                    .map(other -> new TradeFinishEvent(other, player, !cancelling, swapped))
                    .orElseGet(() -> new TradeFinishEvent(names[Perspective.SECONDARY.id()], getUniqueId(Perspective.SECONDARY), player, !cancelling, swapped));
        }

        Bukkit.getPluginManager().callEvent(e);
    }

    private @NotNull String @NotNull [] buildFinishMessages(@NotNull Player viewer, @NotNull Perspective perspective, boolean droppedItems, @NotNull PlayerTradeResult result, @NotNull TradeReportEvent event) {
        List<String> messages = new ArrayList<>();
        messages.add(Lang.getPrefix() + getPlaceholderMessage(perspective, "Trade_Was_Finished"));

        // collect reports and sort them
        List<String> list = new ArrayList<>();

        if (TradeSystem.handler().isTradeReportEconomy()) {
            if (event.getEconomyReport() != null) list.addAll(event.getEconomyReport());
            else list.addAll(result.buildEconomyReport());
        }
        if (TradeSystem.handler().isTradeReportItems()) {
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
            messages.add(Lang.getPrefix() + Lang.get("Items_Dropped", viewer));
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
     *
     * @param invokedBy The perspectives that invoked the change which caused closing the shulker GUIs.
     */
    private void closeShulkerPeekingGUIs(@NotNull Set<Perspective> invokedBy) {
        getViewers().forEach(player -> {
            ShulkerPeekGUI shulkerPeek = API.getRemovable(player, ShulkerPeekGUI.class);
            if (shulkerPeek == null) return;

            if (invokedBy.contains(shulkerPeek.getOwner())) {
                try {
                    TradeSystem.handler().playChangeDuringShulkerPeekSound(player);
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

        boolean[] droppedItems = new boolean[]{false, false};

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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean canPickup(Player player, ItemStack item) {
        PlayerInventory inv = new PlayerInventory(player, false);
        Perspective perspective = getPerspective(player);

        for (Integer slot : this.slots) {
            ItemStack back = guis[perspective.id()].getItem(slot);
            if (back != null && back.getType() != Material.AIR) {
                inv.addItem(back);
            }
        }

        //placeholder
        ItemStack cursor = player.getOpenInventory().getCursor();
        if (cursor != null) {
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
                    if (guis[i] == null) continue;

                    if (e.getPlayer().getName().equals(names[i])) {
                        if (!canPickup(e.getPlayer(), e.getItem().getItemStack()))
                            e.setCancelled(true);
                        else {
                            //player picked up an item, check trading items -> balance items of other trader
                            Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> onItemPickUp(getPerspective(e.getPlayer())), 1);
                        }
                    }
                }
            }
        };
    }

    /**
     * Balances the items of the trader given in "player" to make them fit into the inventory of the trade partner.
     * Items will be removed from the trade panel if they do not fit into the inventory.
     *
     * @param perspective The perspective of the trader whose items will be balanced.
     */
    public void cancelItemOverflow(@NotNull Perspective perspective) {
        if (!isActive()) return;
        Player player = getPlayer(perspective);
        if (player == null) return;

        HashMap<Integer, ItemStack> items = new HashMap<>();
        for (Integer slot : this.slots) {
            ItemStack item = this.guis[perspective.id()].getItem(slot);

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

        PlayerInventory inv = getPlayerInventory(perspective.flip());
        HashMap<Integer, Integer> toRemove = new HashMap<>();

        // sort items so only the last items may be removed if they don't fit anymore
        List<Integer> slots = new ArrayList<>(items.keySet());
        slots.sort(Comparator.naturalOrder());

        for (Integer slot : slots) {
            ItemStack item = items.get(slot);
            int amount = inv.addUntilPossible(item, true);
            if (amount > 0) toRemove.put(slot, amount);
        }

        items.clear();
        slots.clear();

        // move items in natural order
        slots.addAll(toRemove.keySet());
        slots.sort(Comparator.reverseOrder());

        TradingGUI gui = guis[perspective.id()];
        for (Integer slot : slots) {
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
     * @param perspective The perspective of the trading player.
     * @param avoid       The slots to ignore.
     * @param items       The collection of items that should be checked if they fit into the inventory of the trade partner.
     * @return True if the given items fit into the inventory of the trade partner.
     */
    public boolean fitsTrade(@NotNull Perspective perspective, @NotNull List<Integer> avoid, @NotNull Collection<ItemStack> items) {
        List<ItemStack> currentlyAddingItems = new ArrayList<>(items);
        TradingGUI gui = this.guis[perspective.id()];

        for (Integer slot : this.slots) {
            if (avoid.contains(slot)) continue;

            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                currentlyAddingItems.add(item);
            }
        }

        PlayerInventory inv = getPlayerInventory(perspective.flip());
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

    @NotNull
    public Perspective getPerspective(@NotNull Player player) {
        return getPerspective(player.getName());
    }

    @NotNull
    public Perspective getPerspective(@NotNull String player) {
        if (player.equalsIgnoreCase(this.names[0])) return Perspective.PRIMARY;
        else if (player.equalsIgnoreCase(this.names[1])) return Perspective.SECONDARY;
        else return Perspective.TERTIARY;
    }

    @NotNull
    public UUID getUniqueId(@NotNull String player) {
        return getUniqueId(getPerspective(player));
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public List<Integer> getOtherSlots() {
        return otherSlots;
    }

    /**
     * Checks if the given collection of items can be picked up by the trade partner of the given player.
     *
     * @param from The perspective of the trading player.
     * @param item The item that should be checked if it does not fit into the inventory of the trade partner.
     * @return True if the given item does not fit into the inventory of the trade partner.
     */
    public boolean doesNotFit(@NotNull Perspective from, @NotNull ItemStack item) {
        return doesNotFit(from, new ArrayList<>(), item);
    }

    /**
     * Checks if the given collection of items can be picked up by the trade partner of the given player without the slots in 'avoid'.
     *
     * @param from  The perspective of the trading player.
     * @param avoid The slots to ignore.
     * @param item  The item that should be checked if it does not fit into the inventory of the trade partner.
     * @return True if the given item does not fit into the inventory of the trade partner.
     */
    public boolean doesNotFit(@NotNull Perspective from, @NotNull List<Integer> avoid, @NotNull ItemStack item) {
        return !fitsTrade(from, avoid, new ArrayList<ItemStack>() {{
            add(item);
        }});
    }

    /**
     * Checks if the given collection of items can be picked up by the trade partner of the given player without the slots in 'avoid'.
     *
     * @param from  The perspective of the trading player.
     * @param items The collection of items that should be checked if they fit into the inventory of the trade partner.
     * @return True if the given items fit into the inventory of the trade partner.
     */
    public boolean fitsTrade(@NotNull Perspective from, @NotNull Collection<ItemStack> items) {
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

    public void synchronizeTradeIcon(@NotNull Perspective from, @NotNull TradeIcon icon, boolean updateIcon) {
        if (icon instanceof Transition) {
            informTransition(icon, from.flip());
        }

        if (updateIcon) {
            icon.updateItem(this, from);

            if (icon instanceof Transition) {
                getLayout()[from.id()].getIcon(((Transition<?, ?>) icon).getTargetClass()).updateItem(this, from);
            }
        }
    }

    protected void informTransition(@NotNull TradeIcon from, @NotNull Perspective to) {
        try {
            Method method = IconHandler.findInform(from.getClass(), from.getClass());

            TradeIcon consumer = getLayout()[to.id()].getIcon(IconHandler.getTransitionTarget(from.getClass()));
            method.invoke(from, consumer);
            consumer.updateItem(this, to);
        } catch (ClassCastException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
            throw new IllegalStateException("Cannot execute method inform(TradeIcon) of " + from.getClass().getName(), ex);
        }
    }

    public void handleClickResult(@NotNull TradeIcon tradeIcon, @NotNull Perspective perspective, @NotNull Perspective viewer, @NotNull GUI gui, @NotNull IconResult result) {
        switch (result) {
            case PASS:
                return;

            case UPDATE:
                //calls an update
                onTradeOfferChange(true);

                synchronizeTradeIcon(perspective, tradeIcon, true);

                // make sure player get notified when something changed
                closeShulkerPeekingGUIs(Collections.singleton(viewer));

                // status icon might can be ready now
                updateStatusIcon(perspective);
                break;

            case GUI:
                try {
                    gui.open();
                } catch (AlreadyOpenedException ignored) {
                } catch (NoPageException | IsWaitingException e) {
                    throw new RuntimeException("Error while opening GUI.", e);
                }
                break;

            case READY:
                updateReady(perspective, true);
                break;

            case NOT_READY:
                updateReady(perspective, false);
                break;

            case CANCEL:
                cancel();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + result);
        }
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

    public BukkitRunnable getCountdown() {
        return countdown;
    }

    public int getCountdownTicks() {
        return countdownTicks;
    }

    public String getOther(String p) {
        if (this.names[0] == null || this.names[1] == null) return null;

        if (this.names[0].equals(p)) return this.names[1];
        else return this.names[0];
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
        Perspective perspective = getPerspective(player);
        if (perspective.isTertiary()) return false;

        TradingGUI gui = guis[perspective.id()];
        return gui.isOpen() && !gui.isWaiting();
    }

    public void acknowledgeGuiSwitch(@NotNull Player player) {
        // Fixes a dupe glitch which allowed the player to remain in the trade GUI during the countdown and then duplicate items.
        updateReady(getPerspective(player), false);
    }

    protected final void playCountDownStopSound() {
        this.getViewers().forEach(p -> TradeSystem.handler().playCountdownStopSound(p));
    }

    protected final void playStartSound() {
        this.getViewers().forEach(p -> TradeSystem.handler().playStartSound(p));
    }

    protected final void playCancelSound() {
        this.getViewers().forEach(p -> TradeSystem.handler().playCancelSound(p));
    }

    public boolean isInitiationServer() {
        return initiationServer;
    }

    public String[] getNames() {
        return names;
    }

    public boolean isCancelling() {
        return cancelling;
    }

    public boolean[] getReady() {
        return ready;
    }

    @NotNull
    private Predicate<Player> nonTrader() {
        return player -> getPerspective(player).isTertiary();
    }

    /**
     * @return The players that currently see this trade.
     */
    @NotNull
    public final Stream<Player> getViewers() {
        // Preparation for future features.
        return Stream.concat(getParticipants(),
                this.subscribers.stream()
                        .filter(s -> s instanceof PlayerSubscriber)
                        .map(s -> ((PlayerSubscriber) s).getPlayer())
        );
    }

    @NotNull
    protected Optional<Player> getPlayerOpt(@NotNull Perspective perspective) {
        return Optional.ofNullable(getPlayer(perspective));
    }

    protected void sendMessage(@NotNull Perspective perspective, @NotNull String message) {
        getPlayerOpt(perspective).ifPresent(p -> p.sendMessage(message));
    }

    @NotNull
    protected String getPlaceholderMessage(@NotNull Perspective perspective, @NotNull String message) {
        // Player with id 0 can be used as backup since we always have at least one player.
        return Lang.get(message, getPlayerOpt(perspective).orElse(getPlayer(Perspective.PRIMARY)));
    }
}
