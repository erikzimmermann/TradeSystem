package de.codingair.tradesystem.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.server.Environment;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.layout.Function;
import de.codingair.tradesystem.trade.layout.Item;
import de.codingair.tradesystem.trade.layout.utils.Pattern;
import de.codingair.tradesystem.utils.Lang;
import de.codingair.tradesystem.utils.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Trade {
    private Player[] players = new Player[2];
    private TradingGUI[] guis = new TradingGUI[2];
    private int[] moneyBackup = new int[] {0, 0};
    private int[] money = new int[] {0, 0};
    private boolean[] ready = new boolean[] {false, false};
    private boolean[] cursor = new boolean[] {false, false};
    private boolean[] waitForPickup = new boolean[] {false, false};

    private List<Integer> slots = new ArrayList<>();
    private List<Integer> otherSlots = new ArrayList<>();

    private Listener listener;

    Trade(Player p0, Player p1) {
        this.players[0] = p0;
        this.players[1] = p1;

        Pattern layout = TradeSystem.getInstance().getLayoutManager().getActive();

        for(Item item : layout.getItems()) {
            if(item == null || item.getFunction() == null) continue;
            if(item.getFunction().equals(Function.EMPTY_FIRST_TRADER)) slots.add(item.getSlot());
            else if(item.getFunction().equals(Function.EMPTY_SECOND_TRADER)) otherSlots.add(item.getSlot());
        }
    }

    boolean[] getReady() {
        return ready;
    }

    int[] getMoney() {
        return money;
    }

    void update() {
        if(this.ready[0] && this.ready[1]) {
            finish();
            return;
        }

        if(guis[0] != null && guis[1] != null) {
            for(int i = 0; i < slots.size(); i++) {
                if((guis[0].getItem(slots.get(i)) == null && guis[1].getItem(otherSlots.get(i)) != null) || (guis[0].getItem(slots.get(i)) != null && guis[1].getItem(otherSlots.get(i)) == null) ||
                        (guis[0].getItem(slots.get(i)) != null && guis[1].getItem(otherSlots.get(i)) != null && !guis[0].getItem(slots.get(i)).equals(guis[1].getItem(otherSlots.get(i))))) {
                    guis[1].setItem(otherSlots.get(i), guis[0].getItem(slots.get(i)));

                    ready[0] = false;
                    ready[1] = false;
                }

                if((guis[1].getItem(slots.get(i)) == null && guis[0].getItem(otherSlots.get(i)) != null) || (guis[1].getItem(slots.get(i)) != null && guis[0].getItem(otherSlots.get(i)) == null) ||
                        (guis[1].getItem(slots.get(i)) != null && guis[0].getItem(otherSlots.get(i)) != null && !guis[1].getItem(slots.get(i)).equals(guis[0].getItem(otherSlots.get(i))))) {
                    guis[0].setItem(otherSlots.get(i), guis[1].getItem(slots.get(i)));

                    ready[1] = false;
                    ready[0] = false;
                }
            }

            if(money[0] != moneyBackup[0] || money[1] != moneyBackup[1]) {
                moneyBackup[0] = money[0];
                moneyBackup[1] = money[1];

                ready[1] = false;
                ready[0] = false;
            }

            guis[0].initialize(this.players[0]);
            guis[1].initialize(this.players[1]);
        }
    }

    void start() {
        startListeners();
        this.guis[0] = new TradingGUI(this.players[0], 0, this);
        this.guis[1] = new TradingGUI(this.players[1], 1, this);

        this.guis[0].open();
        this.guis[1].open();

        TradeSystem.getInstance().getTradeManager().playStartSound(this.players[0]);
        TradeSystem.getInstance().getTradeManager().playStartSound(this.players[1]);
    }

    public void cancel() {
        cancel(null);
    }

    public void cancel(String message) {
        stopListeners();
        if(this.guis[0] == null || this.guis[1] == null) return;

        for(Integer slot : this.slots) {
            if(this.guis[0].getItem(slot) != null && !this.guis[0].getItem(slot).getType().equals(Material.AIR)) this.players[0].getInventory().addItem(this.guis[0].getItem(slot));
            if(this.guis[1].getItem(slot) != null && !this.guis[1].getItem(slot).getType().equals(Material.AIR)) this.players[1].getInventory().addItem(this.guis[1].getItem(slot));
        }

        this.guis[0] = null;
        this.guis[1] = null;

        if(message != null) {
            this.players[0].sendMessage(message);
            this.players[1].sendMessage(message);
        } else {
            this.players[0].sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Cancelled"));
            this.players[1].sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Cancelled"));
        }

        TradeSystem.getInstance().getTradeManager().playCancelSound(this.players[0]);
        TradeSystem.getInstance().getTradeManager().playCancelSound(this.players[1]);

        this.players[0].closeInventory();
        this.players[1].closeInventory();

        this.players[0].updateInventory();
        this.players[1].updateInventory();

        TradeSystem.getInstance().getTradeManager().getTradeList().remove(this);
    }

    private void startListeners() {
        Bukkit.getPluginManager().registerEvents(this.listener = new Listener() {
            @EventHandler
            public void onPickup(PlayerPickupItemEvent e) {
                if(e.getPlayer() == players[0]) {
                    if(!canPickup(e.getPlayer(), e.getItem().getItemStack()) || waitForPickup[0]) e.setCancelled(true);
                    else Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> cancelOverflow(players[1]), 1);
                } else if(e.getPlayer() == players[1]) {
                    if(!canPickup(e.getPlayer(), e.getItem().getItemStack()) || waitForPickup[1]) e.setCancelled(true);
                    else Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> cancelOverflow(players[0]), 1);
                }


            }
        }, TradeSystem.getInstance());
    }

    private boolean canPickup(Player player, ItemStack item) {
        PlayerInventory inv = new PlayerInventory(player);

        for(Integer slot : this.slots) {
            ItemStack back = guis[getId(player)].getItem(slot);
            if(back != null && back.getType() != Material.AIR) {
                inv.addItem(back);
            }
        }

        //placeholder
        if(this.cursor[getId(player)]) {
            ItemStack cursor = new ItemBuilder(XMaterial.BEDROCK).setName("PLACEHOLDER_CURSOR").getItem();
            if(!inv.addItem(cursor, false)) return false;
        }

        return inv.addItem(item);
    }

    private void stopListeners() {
        if(this.listener != null) HandlerList.unregisterAll(this.listener);
    }

    /**
     * Returns the amount, which doesn't fit
     */
    private int fit(Player player, ItemStack item) {
        int amount = item.getAmount();

        for(int i = 0; i < 36; i++) {
            ItemStack itemStack = player.getInventory().getContents()[i];

            if(itemStack == null || itemStack.getType().equals(Material.AIR)) return 0;
            if(itemStack.isSimilar(item) && itemStack.getAmount() < itemStack.getMaxStackSize()) {
                amount -= itemStack.getMaxStackSize() - itemStack.getAmount();
            }

            if(amount <= 0) return 0;
        }

        return amount;
    }

    private void finish() {
        if(this.guis[0] == null || this.guis[1] == null) return;
        if(this.guis[0].pause && this.guis[1].pause) return;
        TradeSystem.getInstance().getTradeManager().getTradeList().remove(this);

        for(Player player : this.players) {
            AnvilGUI gui = API.getRemovable(player, AnvilGUI.class);
            if(gui != null) {
                gui.clearInventory();
                player.closeInventory();
            }
        }

        this.guis[0].pause = true;
        this.guis[1].pause = true;

        for(Integer slot : this.slots) {
            //using original one to prevent dupe glitches!!!
            ItemStack i0 = this.guis[1].getItem(slot);
            ItemStack i1 = this.guis[0].getItem(slot);

            this.guis[1].setItem(slot, null);
            this.guis[0].setItem(slot, null);

            if(i0 != null && !i0.getType().equals(Material.AIR)) {
                int rest = fit(this.players[0], i0);

                if(rest <= 0) {
                    this.players[0].getInventory().addItem(i0);
                } else {
                    ItemStack toDrop = new ItemBuilder(i0).setAmount(rest).getItem();
                    i0.setAmount(i0.getAmount() - rest);

                    if(i0.getAmount() > 0) this.players[0].getInventory().addItem(i0);
                    Environment.dropItem(toDrop, this.players[0]);
                }
            }

            if(i1 != null && !i1.getType().equals(Material.AIR)) {
                int rest = fit(this.players[1], i1);

                if(rest <= 0) {
                    this.players[1].getInventory().addItem(i1);
                } else {
                    ItemStack toDrop = new ItemBuilder(i1).setAmount(rest).getItem();
                    i1.setAmount(i1.getAmount() - rest);

                    if(i1.getAmount() > 0) this.players[1].getInventory().addItem(i1);
                    Environment.dropItem(toDrop, this.players[1]);
                }
            }
        }

        this.guis[0].clear();
        this.guis[1].clear();
        this.guis[0].close();
        this.guis[1].close();

        Profile p0 = TradeSystem.getProfile(this.players[0]);
        Profile p1 = TradeSystem.getProfile(this.players[1]);

        double diff = -this.money[0] + this.money[1];
        if(diff < 0) p0.withdraw(-diff);
        else if(diff > 0) p0.deposit(diff);

        diff = -this.money[1] + this.money[0];
        if(diff < 0) p1.withdraw(-diff);
        else if(diff > 0) p1.deposit(diff);

        this.players[0].sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished"));
        this.players[1].sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished"));

        TradeSystem.getInstance().getTradeManager().playFinishSound(this.players[0]);
        TradeSystem.getInstance().getTradeManager().playFinishSound(this.players[1]);

//        this.players[0].updateInventory();
//        this.players[1].updateInventory();
    }

    public boolean isFinished() {
        return !TradeSystem.getInstance().getTradeManager().getTradeList().contains(this);
    }

    Player getOther(Player p) {
        if(this.players[0] == null || this.players[1] == null) return null;

        if(this.players[0].getName().equals(p.getName())) return this.players[1];
        else return this.players[0];
    }

    int getOtherId(int id) {
        if(id == 1) return 0;
        else return 1;
    }

    int getId(Player player) {
        if(this.players[0].equals(player)) return 0;
        else if(this.players[1].equals(player)) return 1;
        else return -999;
    }

    List<Integer> getSlots() {
        return slots;
    }

    List<Integer> getOtherSlots() {
        return otherSlots;
    }

    public boolean isParticipant(Player player) {
        return this.players != null && (this.players[0] == player || this.players[1] == player);
    }

    boolean noItemsAdded() {
        if(guis[0] != null && guis[1] != null) {
            for(int i = 0; i < slots.size(); i++) {
                if(guis[0].getItem(slots.get(i)) != null && guis[0].getItem(slots.get(i)).getType() != Material.AIR) return false;
                if(guis[1].getItem(slots.get(i)) != null && guis[1].getItem(slots.get(i)).getType() != Material.AIR) return false;
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

        for(Integer slot : this.slots) {
            ItemStack item = this.guis[getId(player)].getItem(slot);

            if(item != null && item.getType() != Material.AIR) {
                if(TradeSystem.getInstance().getTradeManager().isBlocked(item)) blocked.add(slot);
            }
        }

        for(Integer slot : blocked) {
            ItemStack transport = this.guis[getId(player)].getItem(slot).clone();
            this.guis[getId(player)].setItem(slot, new ItemStack(Material.AIR));

            player.getInventory().addItem(transport);
        }

        player.updateInventory();

        boolean found = !blocked.isEmpty();
        blocked.clear();

        return found;
    }

    public void cancelOverflow(Player player) {
        HashMap<Integer, ItemStack> items = new HashMap<>();
        for(Integer slot : this.slots) {
            ItemStack item = this.guis[getId(player)].getItem(slot);

            if(item != null && item.getType() != Material.AIR) {
                items.put(slot, item);
            }
        }

        if(items.isEmpty()) return;

        HashMap<Integer, ItemStack> sorted = new HashMap<>();
        int size = items.size();
        for(int i = 0; i < size; i++) {
            int slot = 0;
            ItemStack item = null;

            for(int nextSlot : items.keySet()) {
                ItemStack next = items.get(nextSlot);

                if(item == null || item.getAmount() > next.getAmount()) {
                    item = next;
                    slot = nextSlot;
                }
            }

            if(item != null) {
                sorted.put(slot, item);
                items.remove(slot);
            }
        }

        items.clear();
        items.putAll(sorted);
        sorted.clear();

        PlayerInventory inv = new PlayerInventory(getOther(player));
        HashMap<Integer, Integer> toRemove = new HashMap<>();

        items.forEach((slot, item) -> {
            int amount = inv.addUntilPossible(item, true);
            if(amount > 0) toRemove.put(slot, amount);
        });

        items.clear();

        TradingGUI gui = guis[getId(player)];
        for(Integer slot : toRemove.keySet()) {
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
        for(Integer slot : this.slots) {
            if(remove.contains(slot)) continue;

            ItemStack item = this.guis[getId(from)].getItem(slot);
            if(item != null && item.getType() != Material.AIR) items.add(item);
        }

        PlayerInventory inv = new PlayerInventory(getOther(from));
        boolean fits = true;

        for(ItemStack item : items) {
            if(!inv.addItem(item)) {
                fits = false;
                break;
            }
        }

        items.clear();
        return fits;
    }

    public boolean[] getCursor() {
        return cursor;
    }

    public boolean[] getWaitForPickup() {
        return waitForPickup;
    }
}
