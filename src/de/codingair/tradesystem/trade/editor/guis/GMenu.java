package de.codingair.tradesystem.trade.editor.guis;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.anvil.AnvilListener;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.GUIListener;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.layout.Function;
import de.codingair.tradesystem.trade.layout.Item;
import de.codingair.tradesystem.trade.layout.utils.AbstractPattern;
import de.codingair.tradesystem.utils.Head;
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

public class GMenu extends GUI {
    private Menu menu;
    private boolean changed = false;

    private AbstractPattern editing;
    private String name = null;
    private List<Item> items = new ArrayList<>();

    public GMenu(Player p) {
        this(p, null, null);
    }

    public GMenu(Player p, AbstractPattern pattern) {
        this(p, pattern, null);
    }

    public GMenu(Player p, Menu page) {
        this(p, null, page);
    }

    public GMenu(Player p, AbstractPattern pattern, Menu page) {
        super(p, Lang.get("Layout_Editor"), 27, TradeSystem.getInstance(), false);

        this.menu = page == null ? Menu.MAIN : page;
        setMoveOwnItems(true);

        if(pattern != null) {
            this.editing = pattern;
            this.name = pattern.getName();
            this.items = new ArrayList<>(pattern.getItems());
        } else {
            for(int i = 0; i < 54; i++) {
                this.items.add(new Item(i, new ItemStack(Material.AIR), null));
            }
        }

        addListener(new GUIListener() {
            @Override
            public void onInvClickEvent(InventoryClickEvent e) {

            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {

            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                if(isClosingByButton() || isClosingByOperation() || !changed) return;

                Sound.CLICK.playSound(getPlayer());
                if(menu != Menu.CLOSE) menu = menu == Menu.MAIN ? Menu.CLOSE : Menu.MAIN;

                reinitialize(menu == Menu.CLOSE ? Lang.get("Layout_Confirm_Close") : getTitle());
                Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> open(), 1);
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {

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
        ItemButtonOption option = new ItemButtonOption();
        option.setOnlyLeftClick(true);
        option.setClickSound(Sound.CLICK.bukkitSound());

        ItemStack black = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true).getItem();
        ItemStack gray = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).setHideName(true).getItem();
        ItemStack leaves = new ItemBuilder(XMaterial.OAK_LEAVES).setHideName(true).getItem();

        setItem(0, 0, leaves);
        setItem(8, 0, leaves);

        setItem(1, 0, black);
        setItem(0, 1, black);
        setItem(0, 2, black);
        setItem(7, 0, black);
        setItem(8, 1, black);
        setItem(8, 2, black);

        setItem(2, 0, gray);
        setItem(1, 1, gray);
        setItem(1, 2, gray);
        setItem(6, 0, gray);
        setItem(7, 1, gray);
        setItem(7, 2, gray);

        if(menu == Menu.MAIN) {
            boolean ready = ready();

            ItemBuilder builder = new ItemBuilder(ready ? XMaterial.LIME_TERRACOTTA : XMaterial.RED_TERRACOTTA)
                    .setText((ready ? "§a" : "§c") + "§n" + Lang.get("Status"));

            builder.addText("§7" + Lang.get("Items") + ": " + (itemsReady() ? "§a" + Lang.get("Set") : "§c" + Lang.get("Not_Set")));
            builder.addText("§7" + Lang.get("Name") + ": " + (nameReady() ? "§a" + name : "§c" + Lang.get("Not_Set")));
            builder.addText("§7" + Lang.get("Functions") + ": " + (functionsReady() ? "§a" + Lang.get("Set") : "§c" + Lang.get("Not_Set")));
            builder.addText("§7" + Lang.get("Ambiguous_Functions") + ": " + (ambiguousFunctionsReady() ? "§a" + Lang.get("Set") : "§c" + Lang.get("Not_Set")));

            builder.addText("");

            if(ready) {
                builder.addText("§8» §a" + Lang.get("Save"));

                addButton(new ItemButton(4, builder.getItem()) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        for(Item item : items) {
                            if(item.getFunction() == null) item.setFunction(Function.DECORATION);
                        }

                        if(editing == null) {
                            AbstractPattern ap = new AbstractPattern(items, name);
                            TradeSystem.getInstance().getLayoutManager().addPattern(ap);
                            TradeSystem.getInstance().getLayoutManager().setAvailable(name, true);
                            p.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Layout_Finished"));
                        } else {
                            editing.setName(name);
                            editing.getItems().clear();
                            editing.getItems().addAll(items);
                            p.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Layout_Edited"));
                        }
                    }
                }.setOption(option).setCloseOnClick(true));
            } else {
                builder.addText("§8» " + Lang.get("Not_Ready_For_Saving"));
                setItem(4, builder.getItem());
            }
        } else if(menu != Menu.CLOSE) {
            addButton(new ItemButton(4, new ItemBuilder(Head.CYAN_ARROW_LEFT.getItem()).setName("§7» §b" + Lang.get("Back")).setHideName(false).getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if(menu == Menu.AMBIGUOUS_FUNCTIONS) {
                        int slot = 11;
                        if(GMenu.this.getItem(slot) != null && GMenu.this.getItem(slot).getType() != Material.AIR) {
                            Item item = GMenu.this.getItem(Function.PICK_STATUS_NOT_READY);
                            if(item != null) items.remove(item);
                            items.add(new Item(GMenu.this.getItem(Function.PICK_STATUS_NONE).getSlot(), GMenu.this.getItem(slot), Function.PICK_STATUS_NOT_READY));
                            changed = true;
                        }

                        slot = 15;
                        if(GMenu.this.getItem(slot) != null && GMenu.this.getItem(slot).getType() != Material.AIR) {
                            Item item = GMenu.this.getItem(Function.MONEY_REPLACEMENT);
                            if(item != null) items.remove(item);
                            items.add(new Item(GMenu.this.getItem(Function.PICK_MONEY).getSlot(), GMenu.this.getItem(slot), Function.MONEY_REPLACEMENT));

                            item = GMenu.this.getItem(Function.MONEY_REPLACEMENT);
                            if(item != null) items.remove(item);
                            items.add(new Item(GMenu.this.getItem(Function.PICK_MONEY).getSlot(), GMenu.this.getItem(slot), Function.MONEY_REPLACEMENT));
                            changed = true;
                        }

                        slot = 20;
                        if(GMenu.this.getItem(slot) != null && GMenu.this.getItem(slot).getType() != Material.AIR) {
                            Item item = GMenu.this.getItem(Function.PICK_STATUS_READY);
                            if(item != null) items.remove(item);
                            items.add(new Item(GMenu.this.getItem(Function.PICK_STATUS_NONE).getSlot(), GMenu.this.getItem(slot), Function.PICK_STATUS_READY));
                            changed = true;
                        }

                        slot = 24;
                        if(GMenu.this.getItem(slot) != null && GMenu.this.getItem(slot).getType() != Material.AIR) {
                            Item item = GMenu.this.getItem(Function.SHOW_STATUS_READY);
                            if(item != null) items.remove(item);
                            items.add(new Item(GMenu.this.getItem(Function.SHOW_STATUS_NOT_READY).getSlot(), GMenu.this.getItem(slot), Function.SHOW_STATUS_READY));
                            changed = true;
                        }
                    }

                    menu = Menu.MAIN;
                    reinitialize();
                    p.updateInventory();
                }
            }.setOption(option));
        }

        setEditableSlots(false, 11, 15, 20, 24);

        switch(menu) {
            case MAIN:
                ItemBuilder builder = new ItemBuilder(XMaterial.BEACON).setText("§8» §b" + Lang.get("Layout_Set_Items"));
                if(!itemsReady()) builder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

                addButton(new ItemButton(2, 2, builder.getItem()) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        changeGUI(new GEditor(p, GMenu.this, new Callback<List<Item>>() {
                            @Override
                            public void accept(List<Item> list) {
                                if(list != null && !list.isEmpty()) {
                                    for(int i = 0; i < 54; i++) {
                                        Item base = getItemOf(GMenu.this.items, i);
                                        Item toCompare = GMenu.this.getItemOf(list, i);

                                        if((base.getItem() == null && toCompare.getItem() != null) || !base.getItem().isSimilar(toCompare.getItem())) {
                                            GMenu.this.items.remove(base);
                                            GMenu.this.items.add(toCompare);
                                            changed = true;
                                        }
                                    }
                                }

                                reinitialize();
                                open();
                            }
                        }));
                    }
                }.setOption(option));

                builder = new ItemBuilder(XMaterial.NAME_TAG).setText("§8» §b" + Lang.get("Layout_Set_Name"));
                if(!nameReady()) builder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

                addButton(new ItemButton(3, 2, builder.getItem()) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        AnvilGUI.openAnvil(TradeSystem.getInstance(), p, new AnvilListener() {
                            @Override
                            public void onClick(AnvilClickEvent e) {
                                e.setCancelled(true);
                                e.setClose(false);

                                if(TradeSystem.getInstance().getLayoutManager().getPattern(e.getInput()) != null) {
                                    p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Layout_Name_Already_Exists"));
                                    return;
                                }

                                if(e.getInput().contains(" ")) {
                                    p.sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Name_Space"));
                                    return;
                                }

                                TradeSystem.getInstance().getLayoutManager().setAvailable(e.getInput(), false);
                                name = e.getInput();
                                changed = true;
                                e.setClose(true);
                            }

                            @Override
                            public void onClose(AnvilCloseEvent e) {
                                if(e.getSubmittedText() != null) name = e.getSubmittedText();
                                reinitialize();
                                e.setPost(GMenu.this::open);
                            }
                        }, new ItemBuilder(XMaterial.PAPER).setName(name == null ? Lang.get("Name") + "..." : name).getItem());
                    }
                }.setOption(option).setCloseOnClick(true));

                builder = new ItemBuilder(XMaterial.REDSTONE).setText("§8» §b" + Lang.get("Layout_Set_Functions"));
                if(itemsReady() && !functionsReady()) builder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

                if(itemsReady()) {
                    addButton(new ItemButton(5, 2, builder.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            menu = Menu.FUNCTIONS;
                            reinitialize();
                            p.updateInventory();
                        }
                    }.setOption(option));
                } else {
                    setItem(5, 2, builder.getItem());
                }

                builder = new ItemBuilder(XMaterial.COMPARATOR).setText("§8» §b" + Lang.get("Layout_Set_Ambiguous_Functions"));
                if(functionsReady() && !ambiguousFunctionsReady()) builder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

                if(functionsReady()) {
                    addButton(new ItemButton(6, 2, builder.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            menu = Menu.AMBIGUOUS_FUNCTIONS;
                            reinitialize();
                            p.updateInventory();
                        }
                    }.setOption(option));
                } else {
                    setItem(6, 2, builder.getItem());
                }

                break;

            case FUNCTIONS:
                setItem(1, 1, new ItemStack(Material.AIR));
                setItem(7, 1, new ItemStack(Material.AIR));
                setItem(1, 2, new ItemStack(Material.AIR));
                setItem(7, 2, new ItemStack(Material.AIR));

                int i = 0;
                for(Function value : Function.values()) {
                    if(value.isAmbiguous() || !value.isFunction()) continue;

                    switch(value) {
                        case PICK_MONEY:
                            builder = new ItemBuilder(XMaterial.SUNFLOWER).setName("§8» §b" + Lang.get("Layout_Set_Money"));
                            break;

                        case SHOW_MONEY:
                            builder = new ItemBuilder(XMaterial.SUNFLOWER).setName("§8» §b" + Lang.get("Layout_Set_Other_Money"));
                            break;

                        case PICK_STATUS_NONE:
                            builder = new ItemBuilder(XMaterial.LIGHT_GRAY_TERRACOTTA).setName("§8» §b" + Lang.get("Layout_Set_Status"));
                            break;

                        case SHOW_STATUS_NOT_READY:
                            builder = new ItemBuilder(XMaterial.RED_TERRACOTTA).setName("§8» §b" + Lang.get("Layout_Set_Other_Not_Ready"));
                            break;

                        case CANCEL:
                            builder = new ItemBuilder(XMaterial.BARRIER).setName("§8» §b" + Lang.get("Layout_Set_Cancel"));
                            break;

                        case EMPTY_FIRST_TRADER:
                            boolean sameAmount = getAmountOf(Function.EMPTY_FIRST_TRADER) == getAmountOf(Function.EMPTY_SECOND_TRADER);
                            builder = new ItemBuilder(XMaterial.DROPPER).setName("§8» §b" + Lang.get("Layout_Set_Own_Slots"));
                            builder.addText("");
                            builder.addText("§7" + Lang.get("Current_Amount") + ": " + (sameAmount ? "§a" : "§c") + getAmountOf(Function.EMPTY_FIRST_TRADER));
                            builder.addText("");
                            builder.addText("§7" + Lang.get("Layout_Hint_Same_Slot_Amount"), 150);
                            break;

                        case EMPTY_SECOND_TRADER:
                            sameAmount = getAmountOf(Function.EMPTY_FIRST_TRADER) == getAmountOf(Function.EMPTY_SECOND_TRADER);
                            builder = new ItemBuilder(XMaterial.DROPPER).setName("§8» §b" + Lang.get("Layout_Set_Other_Slots"));
                            builder.addText("");
                            builder.addText("§7" + Lang.get("Current_Amount") + ": " + (sameAmount ? "§a" : "§c") + getAmountOf(Function.EMPTY_SECOND_TRADER));
                            builder.addText("");
                            builder.addText("§7" + Lang.get("Layout_Hint_Same_Slot_Amount"), 150);
                            break;

                        default:
                            continue;
                    }

                    if(getItem(value) == null) {
                        builder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);
                    } else if(value == Function.EMPTY_FIRST_TRADER || value == Function.EMPTY_SECOND_TRADER) {
                        if(getAmountOf(Function.EMPTY_FIRST_TRADER) != getAmountOf(Function.EMPTY_SECOND_TRADER)) {
                            builder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);
                        }
                    }

                    addButton(new ItemButton(1 + i, 2, builder.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            changeGUI(new GEditor(p, GMenu.this, value, new Callback<List<Item>>() {
                                @Override
                                public void accept(List<Item> list) {
                                    if(list != null && !list.isEmpty()) {
                                        GMenu.this.items.clear();
                                        GMenu.this.items.addAll(list);
                                        changed = true;
                                    }
                                    GMenu.this.reinitialize();
                                    GMenu.this.open();
                                }
                            }));
                        }
                    }.setOption(option));

                    i++;
                }
                break;

            case AMBIGUOUS_FUNCTIONS:
                ItemStack headR = new ItemBuilder(new ItemStack(Head.GRAY_ARROW_RIGHT.getItem())).setHideName(true).getItem();
                ItemStack headL = new ItemBuilder(new ItemStack(Head.GRAY_ARROW_LEFT.getItem())).setHideName(true).getItem();

                setItem(2, 0, new ItemStack(Material.AIR));
                setItem(6, 0, new ItemStack(Material.AIR));
                addLine(0, 1, 8, 1, new ItemStack(Material.AIR), true);
                addLine(0, 2, 8, 2, new ItemStack(Material.AIR), true);
                addLine(1, 1, 1, 2, headR, true);
                addLine(7, 1, 7, 2, headL, true);

                setItem(0, 1, new ItemBuilder(XMaterial.RED_TERRACOTTA).setName("§8" + Lang.get("Trader") + " ~ §7" + Lang.get("Status") + ": §c" + Lang.get("Not_Ready")).getItem());
                setItem(0, 2, new ItemBuilder(XMaterial.LIME_TERRACOTTA).setName("§8" + Lang.get("Trader") + " ~ §7" + Lang.get("Status") + ": §a" + Lang.get("Ready")).addLore("", "§7" + Lang.get("Wait_For_Other_Player")).getItem());

                setItem(8, 1, new ItemBuilder(XMaterial.SUNFLOWER).setText(TextAlignment.lineBreak(Lang.get("Layout_Set_Replacement_For_Money"), 100)).getItem());
                setItem(8, 2, new ItemBuilder(XMaterial.LIME_TERRACOTTA).setName("§8" + Lang.get("Other") + " ~ §7" + Lang.get("Status") + ": §a" + Lang.get("Ready")).getItem());

                setEditableSlots(true, 11, 15, 20, 24);

                Item item = getItem(Function.PICK_STATUS_NOT_READY);
                if(item != null) setItem(11, item.getItem());
                item = getItem(Function.PICK_STATUS_READY);
                if(item != null) setItem(20, item.getItem());
                item = getItem(Function.MONEY_REPLACEMENT);
                if(item != null) setItem(15, item.getItem());
                item = getItem(Function.SHOW_STATUS_READY);
                if(item != null) setItem(24, item.getItem());
                break;

            case CLOSE:
                setItem(4, 0, new ItemBuilder(XMaterial.NETHER_STAR.parseMaterial()).setText(TextAlignment.lineBreak(Lang.get("Sure_That_You_Want_To_Loose_Your_Data"), 100)).getItem());

                addButton(new ItemButton(2, 2, new ItemBuilder(Head.CYAN_ARROW_LEFT.getItem()).setHideName(false).setName("§8» §b" + Lang.get("Back")).getItem()) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        menu = Menu.MAIN;
                        reinitialize(Lang.get("Layout_Editor"));
                        p.updateInventory();
                    }
                }.setOption(option));

                addButton(new ItemButton(6, 2, new ItemBuilder(XMaterial.BARRIER).setName("§8» §c" + Lang.get("Close")).getItem()) {
                    @Override
                    public void onClick(InventoryClickEvent e) {

                    }
                }.setOption(option).setCloseOnClick(true));
                break;
        }
    }

    private Item getItemOf(List<Item> list, int slot) {
        for(Item item : list) {
            if(item.getFunction() != null && item.getFunction().isAmbiguous()) continue;
            if(item.getSlot() == slot) return item;
        }

        return null;
    }

    public enum Menu {
        MAIN, FUNCTIONS, AMBIGUOUS_FUNCTIONS, CLOSE
    }

    private boolean itemsReady() {
        int physically = 0;

        for(Item item : this.items) {
            if(item.getItem() != null && item.getItem().getType() != Material.AIR) physically++;
        }

        return physically >= 5;
    }

    private boolean functionsReady() {
        for(Function f : Function.values()) {
            if(!f.isFunction() || f.isAmbiguous()) continue;
            if(getItem(f) == null) {
                return false;
            }
        }

        return getAmountOf(Function.EMPTY_FIRST_TRADER) == getAmountOf(Function.EMPTY_SECOND_TRADER);
    }

    private boolean ambiguousFunctionsReady() {
        for(Function f : Function.values()) {
            if(!f.isAmbiguous()) continue;
            if(getItem(f) == null) return false;
        }

        return true;
    }

    private boolean nameReady() {
        return this.name != null;
    }

    private boolean ready() {
        if(!itemsReady()) return false;
        if(!nameReady()) return false;
        if(!ambiguousFunctionsReady()) return false;
        else return functionsReady();
    }

    public int getAmountOf(Function f) {
        int i = 0;

        for(Item item : this.items) {
            if(item.getFunction() == f) i++;
        }

        return i;
    }

    public List<Item> getItems() {
        return this.items;
    }

    public Item getItem(Function function) {
        for(Item item : this.items) {
            if(item.getFunction() == function) return item;
        }

        return null;
    }

    public Item getActionIcon(int slot, boolean ambiguous) {
        for(Item item : this.items) {
            if(ambiguous) {
                if(item.getFunction() != null && item.getFunction().isAmbiguous()) {
                    if(item.getSlot() == slot) return item;
                }
            } else {
                if(item.getFunction() == null || !item.getFunction().isAmbiguous()) {
                    if(item.getSlot() == slot) return item;
                }
            }
        }

        return null;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
