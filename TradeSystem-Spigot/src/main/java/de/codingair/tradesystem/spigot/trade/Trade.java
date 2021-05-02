package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.event.ProxyTradeItemEvent;
import de.codingair.tradesystem.spigot.event.TradeItemEvent;
import de.codingair.tradesystem.spigot.trade.layout.Function;
import de.codingair.tradesystem.spigot.trade.layout.Item;
import de.codingair.tradesystem.spigot.trade.layout.utils.Pattern;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static de.codingair.tradesystem.spigot.tradelog.TradeLogService.getTradeLog;

public abstract class Trade {
    protected final String[] players = new String[2];
    protected final TradingGUI[] guis = new TradingGUI[2];
    protected final int[] moneyBackup = new int[] {0, 0};
    protected final int[] money = new int[] {0, 0};
    protected final boolean[] ready = new boolean[] {false, false};
    protected final boolean[] cursor = new boolean[] {false, false};
    protected final boolean[] waitForPickup = new boolean[] {false, false}; //field to wait for a pickup event (e.g. when players holding items with their cursor)
    protected final List<Integer> slots = new ArrayList<>();
    protected final List<Integer> otherSlots = new ArrayList<>();
    protected BukkitRunnable countdown = null;
    protected int countdownTicks = 0;
    protected Listener listener;

    protected Trade(String p0, String p1) {
        this.players[0] = p0;
        this.players[1] = p1;

        Pattern layout = TradeSystem.getInstance().getLayoutManager().getActive();

        for (Item item : layout.getItems()) {
            if (item == null || item.getFunction() == null) continue;
            if (item.getFunction().equals(Function.EMPTY_FIRST_TRADER)) slots.add(item.getSlot());
            else if (item.getFunction().equals(Function.EMPTY_SECOND_TRADER)) otherSlots.add(item.getSlot());
        }
    }

    boolean[] getReady() {
        return ready;
    }

    int[] getMoney() {
        return money;
    }

    protected abstract void updateGUI(boolean forceUpdate);

    void update() {
        update(false);
    }

    void update(boolean forceUpdate) {
        updateGUI(forceUpdate);

        if (this.ready[0] && this.ready[1]) finish();
        else if (countdown != null) {
            playCountDownStopSound();
            countdown.cancel();
            countdownTicks = 0;
            countdown = null;
            guis[0].synchronizeTitle();
            guis[1].synchronizeTitle();
        }
    }

    void updateReady(int id, boolean ready) {
        callReadyUpdate(id, ready);
        this.ready[id] = ready;
        update(true);
    }

    protected abstract void callReadyUpdate(int id, boolean ready);

    protected abstract void playCountDownStopSound();

    protected abstract void startGUI();

    protected abstract void playStartSound();

    void start() {
        startListeners();
        startGUI();
        playStartSound();
    }

    public void cancel() {
        cancel(null);
    }

    protected abstract boolean[] closeGUI();

    protected abstract void cancelling(String message);

    public void cancel(String message) {
        cancel(message, false);
    }

    public void cancel(String message, boolean alreadyCalled) {
        boolean[] droppedItems = closeGUI();
        if (droppedItems == null) return;

        stopListeners();

        if (message != null) {
            getTradeLog().log(players[0], players[1], "Trade Cancelled: " + message);
            sendMessage(message);
        } else {
            getTradeLog().log(players[0], players[1], "Trade Cancelled");

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

        playCancelSound();
        closeInventories();

        TradeSystem.man().getTrades().remove(players[0].toLowerCase());
        TradeSystem.man().getTrades().remove(players[1].toLowerCase());
        if (!alreadyCalled) cancelling(message);
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

    protected boolean dropItem(Player player, ItemStack itemStack) {
        if (player == null || itemStack == null || itemStack.getType() == Material.AIR) return false;
        player.getWorld().dropItem(player.getLocation(), itemStack);
        return true;
    }

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

    /**
     * Returns the amount, which doesn't fit
     */
    protected int fit(Player player, ItemStack item) {
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

    protected abstract void finish();

    protected void handleMoney(String p, String other, Profile profile, double diff) {
        if (diff < 0) {
            profile.withdraw(-diff);
            getTradeLog().log(p, other, p + " payed money: " + diff);
        } else if (diff > 0) {
            profile.deposit(diff);
            getTradeLog().log(p, other, p + " received money: " + diff);
        }
    }

    protected void callTradeEvent(@NotNull Player receiver, @NotNull Player sender, ItemStack item) {
        if (item == null) return;
        TradeItemEvent event = new TradeItemEvent(receiver, sender, item);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.callEvent(event);
    }

    protected void callTradeEvent(@NotNull Player receiver, @NotNull String sender, ItemStack item) {
        if (item == null) return;
        ProxyTradeItemEvent event = new ProxyTradeItemEvent(receiver, sender, item);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.callEvent(event);
    }

    public boolean isFinished() {
        return !TradeSystem.man().getTradesList().contains(this);
    }

    int getOtherId(int id) {
        if (id == 1) return 0;
        else return 1;
    }

    int getOtherId(Player player) {
        return getOtherId(getId(player));
    }

    int getId(Player player) {
        if (player.getName().equals(this.players[0])) return 0;
        else if (player.getName().equals(this.players[1])) return 1;
        else return -999;
    }

    List<Integer> getSlots() {
        return slots;
    }

    List<Integer> getOtherSlots() {
        return otherSlots;
    }

    public boolean isParticipant(Player player) {
        return player.getName().equals(this.players[0]) || player.getName().equals(this.players[1]);
    }

    boolean noItemsAdded() {
        if (guis[0] != null && guis[1] != null) {
            for (int i = 0; i < slots.size(); i++) {
                if (guis[0].getItem(slots.get(i)) != null && guis[0].getItem(slots.get(i)).getType() != Material.AIR) return false;
                if (guis[1].getItem(slots.get(i)) != null && guis[1].getItem(slots.get(i)).getType() != Material.AIR) return false;
            }
        }

        return true;
    }

    boolean noMoneyAdded() {
        return this.money[0] == 0 && this.money[1] == 0;
    }

    boolean emptyTrades() {
        return noMoneyAdded() && noItemsAdded();
    }

    public boolean cancelBlockedItems(Player player) {
        List<Integer> blocked = new ArrayList<>();

        for (Integer slot : this.slots) {
            ItemStack item = this.guis[getId(player)].getItem(slot);

            if (item != null && item.getType() != Material.AIR) {
                if (TradeSystem.man().isBlocked(item)) blocked.add(slot);
            }
        }

        for (Integer slot : blocked) {
            ItemStack transport = this.guis[getId(player)].getItem(slot).clone();
            this.guis[getId(player)].setItem(slot, new ItemStack(Material.AIR));

            player.getInventory().addItem(transport);
        }

        player.updateInventory();

        boolean found = !blocked.isEmpty();
        blocked.clear();

        return found;
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
}
