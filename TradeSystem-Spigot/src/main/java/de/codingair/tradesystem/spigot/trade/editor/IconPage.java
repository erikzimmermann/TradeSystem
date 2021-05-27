package de.codingair.tradesystem.spigot.trade.editor;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.tools.Call;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.editor.utils.InventoryEditorButton;
import de.codingair.tradesystem.spigot.trade.editor.utils.ItemStackEnterGUI;
import de.codingair.tradesystem.spigot.trade.layout.registration.EditorInfo;
import de.codingair.tradesystem.spigot.trade.layout.registration.IconHandler;
import de.codingair.tradesystem.spigot.trade.layout.registration.MultiEditorInfo;
import de.codingair.tradesystem.spigot.trade.layout.registration.Type;
import de.codingair.tradesystem.spigot.trade.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.basic.DecorationIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.basic.TradeSlot;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.basic.TradeSlotOther;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IconPage extends Page {
    protected final List<EditorInfo> icons;
    private final Type type;
    protected Editor editor;

    public IconPage(Editor editor, Type type, Page basic) {
        super(editor, basic);
        this.editor = editor;
        this.type = type;
        this.icons = IconHandler.getIcons(this.type);
    }

    @Override
    public void buildItems() {
        int slot = 1;
        for (EditorInfo icon : this.icons) {
            addButton(slot, 2, buildButton(slot + 18, icon));
            if (++slot == 7) break;
        }
    }

    private Button buildButton(int slot, EditorInfo editorInfo) {
        return new InventoryEditorButton() {
            //start at 1 since the first ItemStack will be set when we select this item
            int id = 1;

            @Override
            public @Nullable ItemStack buildItem() {
                boolean alreadySet = !editor.getLayoutInventory().isEmpty() && editor.getIcons().containsValue(editorInfo.getTradeIcon());

                ItemBuilder builder = editorInfo.getEditorIcon(editor);

                //add marker
                boolean addMarker = false;
                boolean done = alreadySet;

                //do we need more decoration items?
                if (editorInfo.getTradeIcon().equals(DecorationIcon.class)) {
                    if (editor.needsMoreDecorationItems()) {
                        addMarker = true;
                    } else {
                        done = true;
                    }
                } else {
                    if (editorInfo.isNecessary()) {
                        //do we have all necessary items?
                        if (!alreadySet && !editor.needsMoreDecorationItems()) {
                            addMarker = true;
                        }
                    }

                    //if we already selected one item to this icon
                    if (!addMarker && alreadySet) {
                        if (MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                            //did we configure all variants?

                            MultiEditorInfo multiEditorInfo = (MultiEditorInfo) editorInfo;
                            ItemStack[] variants = editor.getVariants(editorInfo.getTradeIcon(), null);

                            int configured = 0;
                            if (variants != null) {
                                for (ItemStack i : variants) {
                                    if (i != null) configured++;
                                }
                            }

                            boolean variantsNotDone = configured != multiEditorInfo.getIconName().length;
                            if (variantsNotDone) addMarker = true;
                            done = !variantsNotDone;
                        }

                        //something that needs another item
                        if (!editor.needsMoreDecorationItems()) {
                            if (Transition.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo.getTransitionTarget() != null) {
                                //did we configure the transition target?

                                boolean transitionTargetNotSet = !editor.getIcons().containsValue(editorInfo.getTransitionTarget());
                                if (transitionTargetNotSet) {
                                    addMarker = true;
                                }

                                done = !transitionTargetNotSet;
                            }
                        }
                    }
                }

                if (addMarker) {
                    builder.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                    builder.setHideEnchantments(true);
                }

                builder.setName(Editor.ITEM_TITLE_COLOR + editorInfo.getName() + (done ? "§r §a✔" : ""));
                builder.addLore("");

                //add click description
                if (editorInfo.getTradeIcon().equals(DecorationIcon.class)) {
                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": " + (alreadySet ? "§7" : "§a") + Lang.get("Edit"));
                } else if (TradeSlot.class.isAssignableFrom(editorInfo.getTradeIcon())) {
                    int tradeSlots = (int) editor.getIcons().values().stream().filter(i -> i.equals(TradeSlot.class)).count();
                    int otherTradeSlots = (int) editor.getIcons().values().stream().filter(i -> i.equals(TradeSlotOther.class)).count();

                    if (tradeSlots != otherTradeSlots) {
                        builder.removeLore();
                        builder.addText("§7" + Lang.get("Layout_Hint_Same_Slot_Amount", gui.getPlayer()), 150);
                        builder.addLore("");
                    }

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": " + (alreadySet ? "§7" : "§a") + Lang.get("Edit"));
                } else {
                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": " + (alreadySet ? "§7" : "§a") + Lang.get("Select"));
                }

                if (alreadySet) {
                    //icon cannot be a transition AND a multi trade icon
                    if (Transition.class.isAssignableFrom(editorInfo.getTradeIcon())) {
                        boolean hasTarget = editor.getIcons().containsValue(editorInfo.getTransitionTarget());

                        builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Rightclick") + ": " + (hasTarget ? "§7" : "§a") + Lang.get("Select_Foreign_Preview"));
                    } else if (MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                        MultiEditorInfo multiEditorInfo = (MultiEditorInfo) editorInfo;
                        ItemStack[] variants = editor.getVariants(editorInfo.getTradeIcon(), null);

                        boolean variantSet = variants != null && variants[id] != null;
                        int configured = 0;
                        if (variants != null) {
                            for (ItemStack i : variants) {
                                if (i != null) configured++;
                            }
                        }
                        String statusColor = configured == multiEditorInfo.getIconName().length ? "§a" : "§c";

                        builder.addLore("§8§m                    ");
                        builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Variant") + ": " + (variantSet ? "§a" : "§c") + (id + 1) + ". " + multiEditorInfo.getIconName()[id] + " §8(" + statusColor + configured + "§8/" + statusColor + multiEditorInfo.getIconName().length + "§8)");
                        builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Rightclick") + ": §7" + Lang.get("Edit_Variant"));
                        builder.addLore("§8" + Lang.get("Shift_Leftclick") + ": §8←");
                        builder.addLore("§8" + Lang.get("Shift_Rightclick") + ": §8→");
                    }
                }

                return builder.getItem();
            }

            @Override
            public boolean canClick(ClickType clickType) {
                boolean alreadySet = !editor.getLayoutInventory().isEmpty() && editor.getIcons().containsValue(editorInfo.getTradeIcon());

                if (editorInfo.getTradeIcon().equals(DecorationIcon.class)) return true;
                else if (editorInfo.getTradeIcon().equals(TradeSlot.class) || editorInfo.getTradeIcon().equals(TradeSlotOther.class)) return true;
                else if (Transition.class.isAssignableFrom(editorInfo.getTradeIcon())) return true;
                else if (alreadySet && MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) return true;
                else return clickType == ClickType.LEFT;
            }

            @Override
            public void onClick(GUI gui, InventoryClickEvent e) {
                boolean alreadySet = !editor.getLayoutInventory().isEmpty() && editor.getIcons().containsValue(editorInfo.getTradeIcon());

                if (alreadySet && MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                    MultiEditorInfo multiEditorInfo = (MultiEditorInfo) editorInfo;

                    if (e.getClick() == ClickType.SHIFT_LEFT) {
                        id--;
                        if (id < 0) id = multiEditorInfo.getIconName().length - 1;
                        updateItem(slot);
                    } else if (e.getClick() == ClickType.SHIFT_RIGHT) {
                        id++;
                        if (id == multiEditorInfo.getIconName().length) id = 0;
                        updateItem(slot);
                    }
                }
            }

            @Override
            public boolean canSwitch(ClickType clickType) {
                boolean alreadySet = !editor.getLayoutInventory().isEmpty() && editor.getIcons().containsValue(editorInfo.getTradeIcon());

                if (alreadySet && MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                    if (clickType == ClickType.SHIFT_LEFT) return false;
                    else return clickType != ClickType.SHIFT_RIGHT;
                }

                return true;
            }

            @Override
            public boolean open(ClickType clickType, GUI gui, Call call) {
                boolean alreadySet = !editor.getLayoutInventory().isEmpty() && editor.getIcons().containsValue(editorInfo.getTradeIcon());

                if (editorInfo.getTradeIcon().equals(DecorationIcon.class))
                    //special cases
                    editor.openLayoutInventory(null, null);

                else if (editorInfo.getTradeIcon().equals(TradeSlot.class) || editorInfo.getTradeIcon().equals(TradeSlotOther.class))
                    editor.openLayoutInventory(editorInfo.getTradeIcon(), null);

                else if (clickType == ClickType.LEFT)
                    //default behavior
                    editor.openLayoutInventory(editorInfo.getTradeIcon(), call);

                else if (Transition.class.isAssignableFrom(editorInfo.getTradeIcon()))
                    //transition icon
                    editor.openLayoutInventory(IconHandler.getTransitionTarget(editorInfo.getTradeIcon()), call);

                else if (alreadySet && MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                    //multi icon
                    MultiEditorInfo multiEditorInfo = (MultiEditorInfo) editorInfo;

                    if (clickType == ClickType.RIGHT) {
                        try {
                            ItemStack[] variants = editor.getVariants(editorInfo.getTradeIcon(), null);
                            ItemStack previous = variants == null ? null : variants[id];

                            new ItemStackEnterGUI(gui.getPlayer(), "§8" + editorInfo.getName() + ": §9" + multiEditorInfo.getIconName()[id], previous, new Callback<ItemStack>() {
                                @Override
                                public void accept(ItemStack itemStack) {
                                    editor.getVariants(editorInfo.getTradeIcon(), multiEditorInfo.getIconName().length)[id] = itemStack;
                                    if (itemStack != null) {
                                        if (id == 0) {
                                            //update item in layout
                                            editor.getLayoutInventory().setItem(editor.getSlotOf(editorInfo.getTradeIcon()), itemStack);
                                        }

                                        id++;
                                        if (multiEditorInfo.getIconName().length == id) id = 0;
                                    }

                                    updateItem(slot);
                                    getBasic().updateItems();
                                }
                            }).open();
                        } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
                            e.printStackTrace();
                        }

                        //ignore close listener in API
                        return false;
                    }
                }

                //reopen when closed
                return true;
            }
        };
    }

    public Type getType() {
        return type;
    }
}
