package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeItemEvent;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.TradeLayout;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.IconHandler;
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService.getTradeLog;

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
    protected Listener listener;
    protected BukkitRunnable countdown = null;
    protected int countdownTicks = 0;
    protected boolean cancelling = false;

    protected Trade(String player0, String player1, boolean initiationServer) {
        this.initiationServer = initiationServer;
        this.players[0] = player0;
        this.players[1] = player1;
    }

    /**
     * @param player The player that should be analyzed.
     * @param item   The item that should be checked.
     * @return The amount which doesn't fit.
     */
    public static int fit(Player player, ItemStack item) {
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
    protected boolean addOrDrop(@NotNull Player player, @NotNull ItemStack item) {
        int fit = fit(player, item);

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

    public boolean[] getReady() {
        return ready;
    }

    protected abstract void updateGUI();

    public void update() {
        updateGUI();

        if (this.ready[0] && this.ready[1]) finish();
        else if (countdown != null) {
            playCountDownStopSound();
            countdown.cancel();
            countdownTicks = 0;
            countdown = null;
            synchronizeTitle();
        }
    }

    protected void updateStatusIcon(Player player, int id) {
        StatusIcon icon = layout[id].getIcon(StatusIcon.class);
        icon.updateButton(this, player);

        ShowStatusIcon showIcon = layout[id].getIcon(ShowStatusIcon.class);
        showIcon.updateButton(this, player);
    }

    /**
     * <b>Simulates</b> all trade icon exchanges.
     *
     * @param player The player which tries to finish.
     * @return {@link Boolean#TRUE} if the simulation had no issues.
     */
    protected boolean tryFinish(@NotNull Player player) {
        int id = getId(player);

        Player other = getOther(player).orElse(null);
        String othersName = getOther(player.getName());

        for (TradeIcon icon : layout[id].getIcons()) {
            if (icon == null) continue;
            FinishResult result = icon.tryFinish(this, player, other, othersName, this.initiationServer);

            switch (result) {
                case ERROR_ECONOMY:
                    cancel(Lang.getPrefix() + Lang.get("Economy_Error"));
                    return false;

                case PASS:
                    break;
            }
        }

        return true;
    }

    protected void finishPlayer(@NotNull Player player) {
        int id = getId(player);

        Player other = getOther(player).orElse(null);
        String othersName = getOther(player.getName());

        for (TradeIcon icon : layout[id].getIcons()) {
            if (icon == null) continue;
            icon.onFinish(this, player, other, othersName, this.initiationServer);
        }
    }

    public void updateLater(long delay) {
        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), this::update, delay);
    }

    protected abstract void synchronizeTitle();

    public void updateReady(int id, boolean ready) {
        callReadyUpdate(id, ready);
        this.ready[id] = ready;
        update();
    }

    protected abstract void callReadyUpdate(int id, boolean ready);

    protected abstract void playCountDownStopSound();

    protected abstract void initializeGUIs();

    protected abstract void startGUI();

    protected abstract void playStartSound();

    void start() {
        initializeGUIs();
        buildPattern();
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

    public void cancel() {
        cancel(null);
    }

    /**
     * This method moves all for trade placed items back to the item owner.
     * Useful when cancelling a trade.
     *
     * @return A tuple of booleans whether the players dropped items or not.
     */
    protected abstract boolean[] cancelGUIs();

    protected abstract void cancelling(String message);

    public void cancel(String message) {
        cancel(message, false);
    }

    public void cancelDueToGUIError() {
        cancel(Lang.getPrefix() + Lang.get("Open_GUI_Error"));
    }

    /**
     * Avoid moving the item which will be renamed into the players inventory.
     */
    protected abstract void clearOpenAnvils();

    public void cancel(String message, boolean alreadyCalled) {
        this.cancelling = true;
        boolean[] droppedItems = cancelGUIs();

        boolean alreadyClosed = droppedItems == null;
        if (alreadyClosed) return;

        stopListeners();
        clearOpenAnvils();

        playCancelSound();
        closeInventories();

        TradeSystem.man().unregisterTrade(players[0]);
        TradeSystem.man().unregisterTrade(players[1]);

        if (!alreadyCalled) cancelling(message);

        if (message != null) {
            if (initiationServer) getTradeLog().log(players[0], players[1], TradeLogMessages.CANCELLED_REASON.get(message));
            sendMessage(message);
        } else {
            if (initiationServer) getTradeLog().log(players[0], players[1], TradeLogMessages.CANCELLED.get());

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
    }

    protected abstract void closeInventories();

    protected abstract void playCancelSound();

    protected abstract void sendMessage(String message);

    protected abstract void sendMessage(int id, String message);

    protected abstract String getPlaceholderMessage(int playerId, String message);

    protected abstract Listener getPickUpListener();

    private void startListeners() {
        Bukkit.getPluginManager().registerEvents(this.listener = getPickUpListener(), TradeSystem.getInstance());
    }

    public boolean dropItem(Player player, ItemStack itemStack) {
        if (player == null || itemStack == null || itemStack.getType() == Material.AIR) return false;
        player.getWorld().dropItem(player.getLocation().add(0, 0.1, 0), itemStack);
        return true;
    }

    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    protected boolean canPickup(Player player, ItemStack item) {
        PlayerInventory inv = new PlayerInventory(player);

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

    private void stopListeners() {
        if (this.listener != null) HandlerList.unregisterAll(this.listener);
    }

    protected abstract void finish();

    protected @Nullable ItemStack callTradeEvent(@NotNull Player receiver, @NotNull Player sender, @Nullable ItemStack item) {
        if (item == null) return null;

        TradeItemEvent event = new TradeItemEvent(receiver, sender, item);
        Bukkit.getPluginManager().callEvent(event);
        return event.getItem();
    }

    protected @Nullable ItemStack callTradeEvent(@NotNull Player receiver, @NotNull String sender, @Nullable ItemStack item) {
        if (item == null) return null;

        TradeItemEvent event = new TradeItemEvent(receiver, sender, item);
        Bukkit.getPluginManager().callEvent(event);
        return event.getItem();
    }

    public int getOtherId(@Range (from = 0, to = 1) int id) {
        if (id == 1) return 0;
        else return 1;
    }

    public int getOtherId(Player player) {
        return getOtherId(getId(player));
    }

    public int getId(Player player) {
        if (player.getName().equals(this.players[0])) return 0;
        else if (player.getName().equals(this.players[1])) return 1;
        else return -999;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    /**
     * Balances the items of the trader given in "player" to make them fit into the inventory of the trade partner.
     * Items will be removed from the trade panel if they does not fit into the inventory.
     *
     * @param playerId The id of the trader whose items will be balanced.
     */
    public abstract void cancelOverflow(int playerId);

    /**
     * Balances the items of the trader given in "player" to make them fit into the inventory of the trade partner.
     * Items will be removed from the trade panel if they does not fit into the inventory.
     *
     * @param player The trader whose items will be balanced.
     */
    protected void cancelOverflow(Player player) {
        HashMap<Integer, ItemStack> items = new HashMap<>();
        for (Integer slot : this.slots) {
            ItemStack item = this.guis[getId(player)].getItem(slot);

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

        PlayerInventory inv = getPlayerInventory(getOtherId(getId(player)));
        HashMap<Integer, Integer> toRemove = new HashMap<>();

        items.forEach((slot, item) -> {
            int amount = inv.addUntilPossible(item, true);
            if (amount > 0) toRemove.put(slot, amount);
        });

        items.clear();

        TradingGUI gui = guis[getId(player)];
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

    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    public boolean fitsTrade(Player from, ItemStack... add) {
        return fitsTrade(from, new ArrayList<>(), add);
    }

    public boolean fitsTrade(Player from, List<Integer> remove, ItemStack... add) {
        List<ItemStack> items = new ArrayList<>(Arrays.asList(add));
        for (Integer slot : this.slots) {
            if (remove.contains(slot)) continue;

            ItemStack item = this.guis[getId(from)].getItem(slot);
            if (item != null && item.getType() != Material.AIR) items.add(item);
        }

        PlayerInventory inv = getPlayerInventory(getOtherId(from));
        boolean fits = true;

        for (ItemStack item : items) {
            if (!inv.addItem(item)) {
                fits = false;
                break;
            }
        }

        items.clear();
        remove.clear();
        return fits;
    }

    @NotNull
    protected abstract PlayerInventory getPlayerInventory(int playerId);

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

    public abstract Optional<Player> getOther(Player p);

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

    public TradingGUI[] getGUIs() {
        return guis;
    }

    public void handleClickResult(@NotNull TradeIcon tradeIcon, int playerId, @NotNull GUI gui, @NotNull IconResult result, long updateLaterDelay) {
        switch (result) {
            case PASS:
                return;

            case UPDATE:
                //calls an update
                updateReady(playerId, false);
                synchronizeTradeIcon(playerId, tradeIcon, true);
                break;

            case UPDATE_LATER:
                updateReady(playerId, false);

                //calls update() another time - not important
                updateLater(updateLaterDelay);
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

    public boolean isInitiationServer() {
        return initiationServer;
    }

    public String[] getPlayers() {
        return players;
    }

    public boolean isCancelling() {
        return cancelling;
    }
}
