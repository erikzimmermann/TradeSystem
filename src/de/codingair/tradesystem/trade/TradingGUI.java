package de.codingair.tradesystem.trade;

import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.GUIListener;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.layout.Item;
import de.codingair.tradesystem.trade.layout.utils.Pattern;
import de.codingair.tradesystem.utils.Lang;
import de.codingair.tradesystem.utils.money.AdapterType;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradingGUI extends GUI {
    private Trade trade;
    private int id;
    public boolean pause = false;
    private Pattern layout;

    public TradingGUI(Player p, int id, Trade trade) {
        super(p, Lang.get("GUI_Title").replace("%PLAYER%", trade.getOther(p).getName()), 54, TradeSystem.getInstance(), false);

        this.layout = TradeSystem.getInstance().getLayoutManager().getActive();
        this.trade = trade;
        this.id = id;

        setCanDropItems(true);
        setMoveOwnItems(true);
        setEditableSlots(true, trade.getSlots());

        initialize(p);

        addListener(new GUIListener() {
            @Override
            public void onDropItem(InventoryClickEvent e) {
                if(!TradeSystem.getInstance().getTradeManager().isDropItems()) {
                    //check for cursor
                    trade.getWaitForPickup()[trade.getId(getPlayer())] = true;
                    Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
                        trade.getCursor()[trade.getId(getPlayer())] = e.getCursor() != null && e.getCursor().getType() != Material.AIR;
                        trade.getWaitForPickup()[trade.getId(getPlayer())] = false;
                    }, 1);
                }
            }

            @Override
            public void onClickBottomInventory(InventoryClickEvent e) {
                if(!TradeSystem.getInstance().getTradeManager().isDropItems()) {
                    //check for cursor
                    trade.getWaitForPickup()[trade.getId(getPlayer())] = true;
                    Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
                        trade.getCursor()[trade.getId(getPlayer())] = e.getCursor() != null && e.getCursor().getType() != Material.AIR;
                        trade.getWaitForPickup()[trade.getId(getPlayer())] = false;
                    }, 1);

                    Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> trade.cancelOverflow(trade.getOther(getPlayer())), 1);
                }
            }

            @Override
            public void onInvClickEvent(InventoryClickEvent e) {
                boolean fits = true;

                if(!TradeSystem.getInstance().getTradeManager().isDropItems()) {
                    //check for cursor
                    trade.getWaitForPickup()[trade.getId(getPlayer())] = true;
                    Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
                        trade.getCursor()[trade.getId(getPlayer())] = e.getCursor() != null && e.getCursor().getType() != Material.AIR;
                        trade.getWaitForPickup()[trade.getId(getPlayer())] = false;
                    }, 1);

                    //check if fits
                    switch(e.getAction().name()) {
                        case "PLACE_ONE": {
                            ItemStack item = e.getCurrentItem();
                            List<Integer> remove = new ArrayList<>();

                            if(item != null && item.getType() != Material.AIR) {
                                item = item.clone();
                                item.setAmount(item.getAmount() + 1);
                                remove.add(e.getSlot());
                            } else {
                                item = e.getCursor().clone();
                                item.setAmount(1);
                            }

                            if(!trade.fitsTrade(getPlayer(), remove, item)) fits = false;
                            break;
                        }

                        case "PLACE_SOME": {
                            ItemStack item = e.getCurrentItem().clone();
                            item.setAmount(item.getMaxStackSize());

                            List<Integer> remove = new ArrayList<>();
                            remove.add(e.getSlot());

                            if(!trade.fitsTrade(getPlayer(), remove, item)) fits = false;
                            break;
                        }

                        case "PLACE_ALL":
                            if(!trade.fitsTrade(getPlayer(), e.getCursor().clone())) fits = false;
                            break;

                        case "HOTBAR_SWAP": {
                            ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                            if(item != null && !trade.fitsTrade(getPlayer(), item.clone())) fits = false;
                            break;
                        }

                        case "HOTBAR_MOVE_AND_READD": {
                            ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                            List<Integer> remove = new ArrayList<>();
                            remove.add(e.getSlot());

                            if(item != null && !trade.fitsTrade(getPlayer(), remove, item.clone())) fits = false;
                            else {
                                e.setCancelled(true);
                                ItemStack top = e.getView().getTopInventory().getItem(e.getSlot()).clone();
                                ItemStack bottom = e.getView().getBottomInventory().getItem(e.getHotbarButton()).clone();

                                e.getView().getTopInventory().setItem(e.getSlot(), bottom);
                                e.getView().getBottomInventory().setItem(e.getHotbarButton(), top);
                            }
                            break;
                        }

                        case "SWAP_WITH_CURSOR": {
                            List<Integer> remove = new ArrayList<>();
                            remove.add(e.getSlot());

                            if(!trade.fitsTrade(getPlayer(), remove, e.getCursor().clone())) fits = false;
                            break;
                        }

                        case "DROP_ALL_CURSOR":
                        case "DROP_ALL_SLOT":
                        case "DROP_ONE_CURSOR":
                        case "DROP_ONE_SLOT":
                            if(!trade.fitsTrade(getPlayer(), e.getCurrentItem().clone())) fits = false;
                            break;

                        default:
                            fits = true;
                            break;
                    }
                }

                if(!fits) {
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space"));
                    Sound.NOTE_BASS.playSound(getPlayer(), 0.8F, 0.6F);
                    e.setCancelled(true);
                } else Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), trade::update, 1);
            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {
            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                if(!pause) trade.cancel();
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {
                if(!trade.fitsTrade(getPlayer(), e.getNewItems().values().toArray(new ItemStack[0]))) {
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space"));
                    Sound.NOTE_BASS.playSound(getPlayer(), 0.8F, 0.6F);
                    e.setCancelled(true);
                } else Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), trade::update, 1);
            }

            @Override
            public void onMoveToTopInventory(ItemStack item, int oldRawSlot, List<Integer> newRawSlots) {

            }

            @Override
            public boolean onMoveToTopInventory(int oldRawSlot, List<Integer> newRawSlots, ItemStack item) {
                if(!trade.fitsTrade(getPlayer(), item)) {
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space"));
                    Sound.NOTE_BASS.playSound(getPlayer(), 0.8F, 0.6F);
                    return true;
                } else Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), trade::update, 1);
                return false;
            }

            @Override
            public void onCollectToCursor(ItemStack item, List<Integer> oldRawSlots, int newRawSlot) {
                Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), trade::update, 1);
            }
        });
    }

    @Override
    public void initialize(Player p) {
        setBuffering(true);

        for(int i = 0; i < 54; i++) {
            if(trade.getSlots().contains(i) || trade.getOtherSlots().contains(i)) continue;
            setItem(i, new ItemStack(Material.AIR));
        }

        for(Item item : this.layout.getItems()) {
            handleItem(item);
        }

        release();
    }

    private void handleItem(Item item) {
        if(item == null || item.getFunction() == null) return;

        ItemButtonOption option = new ItemButtonOption();
        option.setOnlyLeftClick(true);
        option.setClickSound(Sound.CLICK.bukkitSound());

        switch(item.getFunction()) {
            case DECORATION: {
                setItem(item.getSlot(), new ItemBuilder(item.getItem()).setHideName(true).getItem());
                break;
            }

            case PICK_MONEY: {
                if(AdapterType.canEnable()) {
                    ItemBuilder moneyBuilder = new ItemBuilder(item.getItem()).setName("§e" + Lang.get("Money_Amount") + ": §7" + trade.getMoney()[id] + " " + (trade.getMoney()[id] == 1 ? Lang.get("Coin") : Lang.get("Coins"))).addLore("", "§7» " + Lang.get("Click_To_Change"));
                    if(trade.getMoney()[id] > 0) moneyBuilder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

                    addButton(new ItemButton(item.getSlot(), moneyBuilder.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            if(TradeSystem.getProfile(getPlayer()).getMoney() <= 0) {
                                getPlayer().sendMessage(Lang.getPrefix() + Lang.get("No_Money"));
                                return;
                            }

                            pause = true;
                            AnvilGUI.openAnvil(TradeSystem.getInstance(), getPlayer(), new AnvilListener() {
                                private int amount = -999;

                                @Override
                                public void onClick(AnvilClickEvent e) {
                                    e.setCancelled(true);
                                    e.setClose(false);

                                    if(!e.getSlot().equals(AnvilSlot.OUTPUT)) return;

                                    try {
                                        amount = Integer.parseInt(e.getInput());
                                    } catch(Exception ignored) {
                                    }

                                    if(amount < 0) {
                                        amount = -999;
                                        e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount"));
                                        return;
                                    }

                                    int max;
                                    if(amount > (max = TradeSystem.getProfile(e.getPlayer()).getMoney())) {
                                        amount = -999;
                                        e.getPlayer().sendMessage(Lang.getPrefix() + (max == 1 ? Lang.get("Only_1_Coin").replace("%COIN%", max + "") : Lang.get("Only_X_Coins").replace("%COINS%", max + "")));
                                        return;
                                    }

                                    e.setClose(true);
                                }

                                @Override
                                public void onClose(AnvilCloseEvent e) {
                                    e.setPost(() -> {
                                        if(trade.isFinished()) return;

                                        if(amount >= 0) {
                                            trade.getMoney()[id] = amount;
                                            trade.update();
                                        }

                                        pause = false;
                                        open();
                                    });
                                }
                            }, new ItemBuilder(Material.PAPER).setName(trade.getMoney()[id] == 0 ? (Lang.get("Money_Amount") + "...") : trade.getMoney()[id] + "").getItem());
                        }
                    }.setOption(option));
                }
                break;
            }

            case SHOW_MONEY: {
                ItemBuilder moneyBuilder = new ItemBuilder(item.getItem()).setName("§e" + Lang.get("Money_Amount") + ": §7" + trade.getMoney()[trade.getOtherId(id)] + " " + (trade.getMoney()[trade.getOtherId(id)] == 1 ? Lang.get("Coin") : Lang.get("Coins")));
                if(trade.getMoney()[trade.getOtherId(id)] > 0) moneyBuilder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

                if(AdapterType.canEnable()) setItem(item.getSlot(), moneyBuilder.getItem());
                break;
            }

            case MONEY_REPLACEMENT: {
                if(!AdapterType.canEnable()) setItem(item.getSlot(), new ItemBuilder(item.getItem()).setHideName(true).setHideEnchantments(true).setHideStandardLore(true).getItem());
                break;
            }

            case PICK_STATUS_NONE: {
                if(!TradeSystem.getInstance().getTradeManager().isTradeBoth() && !trade.emptyTrades()) break;

                boolean canFinish = false;
                for(Integer slot : trade.getSlots()) {
                    if(getItem(slot) != null && !getItem(slot).getType().equals(Material.AIR)) canFinish = true;
                }
                if(trade.getMoney()[id] != 0) canFinish = true;

                ItemBuilder ready = new ItemBuilder(item.getItem());
                if(!canFinish) {
                    ready.setText("§c" + Lang.get("Trade_Only_With_Objects"), TextAlignment.LEFT, 150);
                    setItem(item.getSlot(), ready.getItem());
                }
                break;
            }

            case PICK_STATUS_NOT_READY: {
                boolean canFinish = !TradeSystem.getInstance().getTradeManager().isTradeBoth() && !trade.emptyTrades();

                if(!canFinish) {
                    for(Integer slot : trade.getSlots()) {
                        if(getItem(slot) != null && !getItem(slot).getType().equals(Material.AIR)) canFinish = true;
                    }
                    if(trade.getMoney()[id] != 0) canFinish = true;
                }

                ItemBuilder ready = new ItemBuilder(item.getItem());
                if(canFinish) {
                    if(trade.getReady()[id]) return;
                    else ready.setName("§7" + Lang.get("Status") + ": §c" + Lang.get("Not_Ready"));

                    addButton(new ItemButton(item.getSlot(), ready.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            trade.getReady()[id] = !trade.getReady()[id];
                            trade.update();
                        }
                    }.setOption(option));
                }
                break;
            }

            case PICK_STATUS_READY: {
                boolean canFinish = !TradeSystem.getInstance().getTradeManager().isTradeBoth() && !trade.emptyTrades();

                if(!canFinish) {
                    for(Integer slot : trade.getSlots()) {
                        if(getItem(slot) != null && !getItem(slot).getType().equals(Material.AIR)) canFinish = true;
                    }
                    if(trade.getMoney()[id] != 0) canFinish = true;
                }

                ItemBuilder ready = new ItemBuilder(item.getItem());
                if(canFinish) {
                    if(trade.getReady()[id]) ready.setName("§7" + Lang.get("Status") + ": §a" + Lang.get("Ready")).addLore("", "§7" + Lang.get("Wait_For_Other_Player"));
                    else return;

                    addButton(new ItemButton(item.getSlot(), ready.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            trade.getReady()[id] = !trade.getReady()[id];
                            trade.update();
                        }
                    }.setOption(option));
                }
                break;
            }

            case SHOW_STATUS_NOT_READY: {
                ItemBuilder ready = new ItemBuilder(item.getItem());
                if(trade.getReady()[trade.getOtherId(id)]) return;
                else ready.setName("§7" + Lang.get("Status") + ": §c" + Lang.get("Not_Ready"));
                setItem(item.getSlot(), ready.getItem());
                break;
            }

            case SHOW_STATUS_READY: {
                ItemBuilder ready = new ItemBuilder(item.getItem());
                if(trade.getReady()[trade.getOtherId(id)]) ready.setColor(DyeColor.LIME).setName("§7" + Lang.get("Status") + ": §a" + Lang.get("Ready"));
                else return;
                setItem(item.getSlot(), ready.getItem());
                break;
            }

            case CANCEL: {
                addButton(new ItemButton(item.getSlot(), new ItemBuilder(item.getItem()).setName("§c" + Lang.get("Cancel_Trade")).getItem()) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        trade.cancel();
                    }
                }.setOption(option));
                break;
            }
        }
    }
}
