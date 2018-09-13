package de.codingair.tradesystem.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.server.Environment;
import de.codingair.codingapi.server.Sound;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.layout.Function;
import de.codingair.tradesystem.trade.layout.Item;
import de.codingair.tradesystem.trade.layout.utils.Pattern;
import de.codingair.tradesystem.utils.Lang;
import de.codingair.tradesystem.utils.Profile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Trade {
    private Player[] players = new Player[2];
    private TradingGUI[] guis = new TradingGUI[2];
    private int[] moneyBackup = new int[] {0, 0};
    private int[] money = new int[] {0, 0};
    private boolean[] ready = new boolean[] {false, false};

    private List<Integer> slots = new ArrayList<>();
    private List<Integer> otherSlots = new ArrayList<>();

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
        this.guis[0] = new TradingGUI(this.players[0], 0, this);
        this.guis[1] = new TradingGUI(this.players[1], 1, this);

        this.guis[0].open();
        this.guis[1].open();

        Sound.LEVEL_UP.playSound(this.players[0], 0.6F, 1F);
        Sound.LEVEL_UP.playSound(this.players[1], 0.6F, 1F);
    }

    public void cancel() {
        cancel(null);
    }

    public void cancel(String message) {
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

        this.players[0].playSound(this.players[0].getLocation(), Sound.ITEM_BREAK.bukkitSound(), 0.6F, 1);
        this.players[1].playSound(this.players[1].getLocation(), Sound.ITEM_BREAK.bukkitSound(), 0.6F, 1);

        this.players[0].closeInventory();
        this.players[1].closeInventory();

        TradeSystem.getInstance().getTradeManager().getTradeList().remove(this);
    }

    private boolean fit(Player player, ItemStack item) {
        int amount = item.getAmount();

        for(ItemStack itemStack : player.getInventory().getContents()) {
            if(itemStack == null || itemStack.getType().equals(Material.AIR)) return true;
            if(itemStack.isSimilar(item) && itemStack.getAmount() < itemStack.getMaxStackSize()) {
                amount -= itemStack.getMaxStackSize() - itemStack.getAmount();
            }

            if(amount <= 0) return true;
        }

        return amount <= 0;
    }

    private void finish() {
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

        for(Integer slot : this.otherSlots) {
            ItemStack i0 = this.guis[0].getItem(slot);
            ItemStack i1 = this.guis[1].getItem(slot);

            if(i0 != null && !i0.getType().equals(Material.AIR)) {
                if(fit(this.players[0], i0)) this.players[0].getInventory().addItem(i0);
                else Environment.dropItem(i0, this.players[0]);
            }

            if(i1 != null && !i1.getType().equals(Material.AIR)) {
                if(fit(this.players[1], i1)) this.players[1].getInventory().addItem(i1);
                else Environment.dropItem(i1, this.players[1]);
            }
        }

        this.guis[0].close();
        this.guis[1].close();

        Profile p0 = TradeSystem.getProfile(this.players[0]);
        Profile p1 = TradeSystem.getProfile(this.players[1]);

        p0.setMoney(p0.getMoney() - this.money[0] + this.money[1]);
        p1.setMoney(p1.getMoney() - this.money[1] + this.money[0]);

        this.players[0].sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished"));
        this.players[1].sendMessage(Lang.getPrefix() + Lang.get("Trade_Was_Finished"));

        this.players[0].playSound(this.players[0].getLocation(), Sound.LEVEL_UP.bukkitSound(), 0.6F, 1);
        this.players[1].playSound(this.players[1].getLocation(), Sound.LEVEL_UP.bukkitSound(), 0.6F, 1);
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

    List<Integer> getSlots() {
        return slots;
    }

    List<Integer> getOtherSlots() {
        return otherSlots;
    }

    public boolean isParticipant(Player player) {
        return this.players != null && (this.players[0] == player || this.players[1] == player);
    }
}
