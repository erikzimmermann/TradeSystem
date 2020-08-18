package de.codingair.tradesystem.trade.editor.guis;

import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.GUIListener;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.layout.Function;
import de.codingair.tradesystem.trade.layout.Item;
import de.codingair.tradesystem.utils.Lang;
import org.bukkit.Bukkit;
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

public class GEditor extends GUI {
    private final GMenu menu;
    private final Function function;
    private boolean changed = false;

    private final Callback<List<Item>> callback;

    public GEditor(Player p, GMenu menu, Callback<List<Item>> callback) {
        this(p, menu, null, callback);
    }

    public GEditor(Player p, GMenu menu, Function function, Callback<List<Item>> callback) {
        super(p, "§c" + Lang.get("Layout_Editor_Set_Items", p), 54, TradeSystem.getInstance(), false);
        this.menu = menu;
        this.callback = callback;
        this.function = function;

        addListener(new GUIListener() {
            @Override
            public void onInvClickEvent(InventoryClickEvent e) {
                if(!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

                if(function != null) {
                    switch(function) {
                        case EMPTY_FIRST_TRADER:
                        case EMPTY_SECOND_TRADER:
                            if(e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR) || menu.getActionIcon(e.getSlot(), false).getFunction() == Function.EMPTY_FIRST_TRADER || menu.getActionIcon(e.getSlot(), false).getFunction() == Function.EMPTY_SECOND_TRADER) {
                                e.setCancelled(false);

                                switch(e.getAction()) {
                                    case COLLECT_TO_CURSOR:
                                        for(Item item : menu.getItems()) {
                                            if(item.getFunction() == function) item.setFunction(null);
                                        }
                                        break;

                                    case PLACE_ALL:
                                    case PLACE_ONE:
                                    case PLACE_SOME:
                                        menu.getActionIcon(e.getSlot(), false).setFunction(function);
                                        break;

                                    case PICKUP_ALL:
                                    case PICKUP_HALF:
                                    case PICKUP_ONE:
                                    case PICKUP_SOME:
                                        menu.getActionIcon(e.getSlot(), false).setFunction(null);
                                        break;

                                    default:
                                        e.setCancelled(true);
                                        break;
                                }
                            } else {
                                getPlayer().sendMessage(Lang.getPrefix() + "§c" + Lang.get("Editor_Not_Empty", p));
                                e.setCancelled(true);
                            }
                            break;

                        default:
                            Item item = menu.getActionIcon(e.getSlot(), false);

                            if(item.getItem().getType() == Material.AIR) {
                                getPlayer().sendMessage(Lang.getPrefix() + "§c" + Lang.get("Editor_No_Item", p));
                                return;
                            }

                            Item old = menu.getItem(function);
                            if(old != null) old.setFunction(null);

                            item.setFunction(function);
                            menu.reinitialize();
                            menu.setChanged(true);
                            changeGUI(menu);
                            break;
                    }
                } else {
                    if((e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) || (e.getCursor() != null && e.getCursor().getType() != Material.AIR) && !e.isCancelled())
                        changed = true;
                }
            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {
                if(function != null) {
                    switch(function) {
                        case EMPTY_FIRST_TRADER:
                            int amount = 25 - menu.getAmountOf(Function.EMPTY_FIRST_TRADER);

                            if(amount > 0) {
                                e.getView().setCursor(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setAmount(amount).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideName(true).setHideEnchantments(true).getItem());
                                getPlayer().updateInventory();
                            }
                            break;
                        case EMPTY_SECOND_TRADER:
                            amount = 25 - menu.getAmountOf(Function.EMPTY_SECOND_TRADER);

                            if(amount > 0) {
                                e.getView().setCursor(new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE).setAmount(amount).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideName(true).setHideEnchantments(true).getItem());
                                getPlayer().updateInventory();
                            }
                            break;
                    }
                }
            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                if(function == null) {
                    List<Item> items = new ArrayList<>();

                    for(int i = 0; i < 54; i++) {
                        items.add(new Item(i, getItem(i), null));
                    }

                    Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
                        if(changed) callback.accept(items);
                        else callback.accept(null);
                    });
                } else if(function == Function.EMPTY_FIRST_TRADER || function == Function.EMPTY_SECOND_TRADER) {
                    e.getView().setCursor(new ItemStack(Material.AIR));

                    Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
                        menu.reinitialize();
                        menu.setChanged(true);
                        changeGUI(menu);
                    });
                } else if(!GEditor.this.isClosingByOperation() && !GEditor.this.isClosingByButton()) {
                    Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
                        menu.reinitialize();
                        changeGUI(menu);
                    });
                }
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {
                if(function != null) {
                    switch(function) {
                        case EMPTY_FIRST_TRADER:
                        case EMPTY_SECOND_TRADER:
                            boolean correct = true;
                            for(Integer i : e.getRawSlots()) {
                                if(i > 53) {
                                    correct = false;
                                    break;
                                }
                            }

                            e.setCancelled(true);
                            if(!correct) return;
                            e.setCancelled(false);

                            for(Integer i : e.getRawSlots()) {
                                Item item = menu.getActionIcon(i, false);
                                if(item == null) continue;
                                item.setFunction(function);
                            }

                            break;
                    }
                } else changed = true;
            }

            @Override
            public void onMoveToTopInventory(ItemStack item, int oldRawSlot, List<Integer> newRawSlots) {
            }

            @Override
            public void onCollectToCursor(ItemStack item, List<Integer> oldRawSlots, int newRawSlot) {
            }
        });

        initialize(p);
    }

    @Override
    public void initialize(Player p) {
        setEditableItems(function == null || function == Function.EMPTY_FIRST_TRADER || function == Function.EMPTY_SECOND_TRADER);
        setMoveOwnItems(function == null);
        setCanDropItems(false);

        for(Item item : menu.getItems()) {
            if(function == null) {
                if(item.getFunction() != null && (item.getFunction() == Function.EMPTY_FIRST_TRADER || item.getFunction() == Function.EMPTY_SECOND_TRADER || item.getFunction().isAmbiguous()))
                    continue;
                setItem(item.getSlot(), item.getItem());
            } else {
                if(item.getFunction() == null) {
                    setItem(item.getSlot(), item.getItem());
                } else {
                    switch(item.getFunction()) {
                        case DECORATION:
                            setItem(item.getSlot(), new ItemBuilder(item.getItem()).setHideName(true).getItem());
                            break;

                        case EMPTY_FIRST_TRADER:
                            setItem(item.getSlot(), new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true).setHideName(true).getItem());
                            break;

                        case EMPTY_SECOND_TRADER:
                            setItem(item.getSlot(), new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideName(true).setHideEnchantments(true).getItem());
                            break;

                        case PICK_MONEY:
                            ItemBuilder moneyBuilder = new ItemBuilder(item.getItem()).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true).setName("§8" + Lang.get("Trader", p) + " ~ §e" + Lang.get("Money_Amount", p) + ": §70 " + Lang.get("Coins", p));
                            setItem(item.getSlot(), moneyBuilder.getItem());
                            break;

                        case SHOW_MONEY:
                            moneyBuilder = new ItemBuilder(item.getItem()).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true).setName("§8" + Lang.get("Other", p) + " ~ §e" + Lang.get("Money_Amount", p) + ": §70 " + Lang.get("Coins", p));
                            setItem(item.getSlot(), moneyBuilder.getItem());
                            break;

                        case PICK_STATUS_NONE:
                            ItemBuilder ready = new ItemBuilder(item.getItem()).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);
                            ready.setName("§8" + Lang.get("Trader", p) + " ~ §7" + Lang.get("Status", p) + ": §c" + Lang.get("Not_Ready", p));
                            setItem(item.getSlot(), ready.getItem());
                            break;

                        case SHOW_STATUS_NOT_READY:
                            ready = new ItemBuilder(item.getItem()).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);
                            ready.setName("§8" + Lang.get("Other", p) + " ~ §7" + Lang.get("Status", p) + ": §c" + Lang.get("Not_Ready", p));
                            setItem(item.getSlot(), ready.getItem());
                            break;

                        case CANCEL:
                            setItem(item.getSlot(), new ItemBuilder(item.getItem()).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true).setName("§c" + Lang.get("Cancel_Trade", p)).getItem());
                            break;
                    }
                }
            }
        }
    }
}
