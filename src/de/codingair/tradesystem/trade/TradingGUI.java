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
            public void onInvClickEvent(InventoryClickEvent e) {
                Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), trade::update, 1);
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
                Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), trade::update, 1);
            }

            @Override
            public void onMoveToTopInventory(ItemStack item, int oldRawSlot, List<Integer> newRawSlots) {
                Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), trade::update, 1);
            }

            @Override
            public void onCollectToCursor(ItemStack item, List<Integer> oldRawSlots, int newRawSlot) {
                Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), trade::update, 1);
            }
        });
    }

    @Override
    public void initialize(Player p) {
        for(int i = 0; i < 54; i++) {
            if(trade.getSlots().contains(i) || trade.getOtherSlots().contains(i)) continue;
            setItem(i, new ItemStack(Material.AIR));
        }

        for(Item item : this.layout.getItems()) {
            handleItem(item);
        }
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
                    ItemBuilder moneyBuilder = new ItemBuilder(item.getItem()).setName("§e" + Lang.get("Money_Amount") + ": §7" + trade.getMoney()[id] + " Coin" + (trade.getMoney()[id] == 1 ? "" : "s")).addLore("", "§7» " + Lang.get("Click_To_Change"));
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
                                        e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount"));
                                        return;
                                    }

                                    int max;
                                    if(amount > (max = TradeSystem.getProfile(e.getPlayer()).getMoney())) {
                                        e.getPlayer().sendMessage(Lang.getPrefix() + (max == 1 ? Lang.get("Only_1_Coin").replace("%COIN%", max + "") : Lang.get("Only_X_Coins").replace("%COINS%", max + "")));
                                        return;
                                    }

                                    e.setClose(true);
                                }

                                @Override
                                public void onClose(AnvilCloseEvent e) {
                                    e.setPost(() -> {
                                        if(amount >= 0) {
                                            trade.getMoney()[id] = amount;
                                            trade.update();
                                        }

                                        pause = false;
                                        open();
                                    });
                                }
                            }, new ItemBuilder(Material.PAPER).setName(Lang.get("Money_Amount") + "...").getItem());
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
                if(!AdapterType.canEnable()) setItem(item.getSlot(), new ItemBuilder(item.getItem()).getItem());
                break;
            }

            case PICK_STATUS_NONE: {
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
                boolean canFinish = false;
                for(Integer slot : trade.getSlots()) {
                    if(getItem(slot) != null && !getItem(slot).getType().equals(Material.AIR)) canFinish = true;
                }
                if(trade.getMoney()[id] != 0) canFinish = true;

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
                boolean canFinish = false;
                for(Integer slot : trade.getSlots()) {
                    if(getItem(slot) != null && !getItem(slot).getType().equals(Material.AIR)) canFinish = true;
                }
                if(trade.getMoney()[id] != 0) canFinish = true;

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
            }

            case SHOW_STATUS_READY: {
                ItemBuilder ready = new ItemBuilder(item.getItem());
                if(trade.getReady()[trade.getOtherId(id)]) ready.setColor(DyeColor.LIME).setName("§7" + Lang.get("Status") + ": §a" + Lang.get("Ready"));
                else return;
                setItem(item.getSlot(), ready.getItem());
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
